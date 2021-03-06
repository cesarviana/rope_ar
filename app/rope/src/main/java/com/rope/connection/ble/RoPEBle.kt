package com.rope.connection.ble

import android.bluetooth.*
import android.content.Context
import android.os.Handler
import android.util.Log.d
import android.util.Log.w
import com.rope.connection.RoPE
import com.rope.connection.ble.*
import java.util.*

class RoPEBle(private val context: Context, private val device: BluetoothDevice, handler: Handler) : RoPE(handler) {

    private var connectionState: Int = -1
    private val callback = MyGattCallback()

    private lateinit var bluetoothGatt: BluetoothGatt

    private lateinit var characteristic: BluetoothGattCharacteristic

    enum class State {
        EXECUTING, STOPPED
    }

    private var state = State.STOPPED

    init {
        setupSelfListeners()
    }

    private fun setupSelfListeners() {
        this.onMessage {
            d("ROPE", "Message $it")
        }
        this.onMessage { message ->
            if (message.contains("<start_required>")) {
                this.state = State.STOPPED
                Listeners.onStartPressed.forEach { it.startPressed(this) }
            }
        }
        this.onMessage { message ->
            val isStartMessage = message.contains("<program:started>")
            val canStart = isStartMessage && isStopped()
            if (canStart) {
                state = State.EXECUTING
                notifyExecutionStarted()
            }
        }
        this.onMessage { message ->
            val regex = "<executed:(?<actionIndex>\\d+)>\r\n.*".toRegex()
            if (regex.containsMatchIn(message)) {
                val matches = regex.matchEntire(message)
                val numberGroup =
                    1 // the group 0 is the complete string, group 1 is the desired info
                val actionIndex = matches!!.groups[numberGroup]!!.value
                actionIndex.toIntOrNull()?.let { index ->
                    this.actionIndex = index
                    program.actionList.getOrNull(index)?.let {
                        notifyActionExecuted(it)
                    }
                }
            }
        }
        this.onMessage { message ->
            if (message.contains("terminated")) {
                state = State.STOPPED
                notifyExecutionEnded()
            }
        }
    }

    private val commandsPrefix = "cmds:"

    override fun connect() {
        if (isConnected())
            w("ROPE", "Device is already connected, not connecting again")
        else
            device.connectGatt(context, false, callback)
    }

    override fun isConnected() = connectionState == ConnectionState.CONNECTED
    override fun isConnecting() = device.bondState == BluetoothDevice.BOND_BONDING

    override fun send(command: String) {
        d("ROPE", command)
        characteristic.value = command.toByteArray()
        bluetoothGatt.writeCharacteristic(characteristic)
    }

    override fun onDisconnected(ropeDisconnectListener: RoPEDisconnectedListener) {
        Listeners.onDisconnected.add(ropeDisconnectListener)
    }

    override fun onConnected(function: () -> Unit) {
        Listeners.onConnected = function
    }

    override fun onMessage(function: (message: String) -> Unit) {
        Listeners.onMessage.add(function)
    }

    override fun onStartedPressed(ropeStartPressedListener: RoPEStartPressedListener) {
        Listeners.onStartPressed.add(ropeStartPressedListener)
    }

    override fun onActionExecuted(ropeActionListener: RoPEActionListener) {
        Listeners.onActionExecution.add(ropeActionListener)
    }

    override fun sendActions(actionList: List<Action>)
    {
        val actions = actionList.joinToString("") { it.stringSequence }
        val command = "$commandsPrefix$actions"
        send(command)
    }

    override fun execute(program: Program) {
        if (isStopped()) {
            this.program = program
            val actions = program.actionList.joinToString("") { it.stringSequence }
            val executeSuffix = Action.EXECUTE.stringSequence
            val command = "$commandsPrefix$actions$executeSuffix"
            send(command)
        }
    }

    override fun isStopped() = state == State.STOPPED

    override fun onExecutionStarted(executionStartedListener: RoPEExecutionStartedListener) {
        Listeners.onExecutionStarted.add(executionStartedListener)
    }

    override fun onExecutionFinished(executionFinishedListener: RoPEExecutionFinishedListener) {
        Listeners.onExecutionFinished.add(executionFinishedListener)
    }

    override fun disconnect() {
        bluetoothGatt.disconnect()
    }

    override fun removeDisconnectedListener(listener: RoPEDisconnectedListener) {
        Listeners.onDisconnected.remove(listener)
    }

    override fun removeStartPressedListener(listener: RoPEStartPressedListener) {
        Listeners.onStartPressed.remove(listener)
    }

    override fun removeActionListener(listener: RoPEActionListener) {
        Listeners.onActionExecution.remove(listener)
    }

    override fun removeExecutionStartedListener(listener: RoPEExecutionStartedListener) {
        Listeners.onExecutionStarted.remove(listener)
    }

    override fun removeExecutionFinishedListener(listener: RoPEExecutionFinishedListener) {
        Listeners.onExecutionFinished.remove(listener)
    }

    override fun stop() {
        this.state = State.STOPPED
    }

    private inner class MyGattCallback : BluetoothGattCallback() {

        override fun onConnectionStateChange(gatt: BluetoothGatt?, status: Int, newState: Int) {
            when (newState) {
                ConnectionState.DISCONNECTED -> Listeners.onDisconnected.forEach {
                    this@RoPEBle.connectionState = ConnectionState.DISCONNECTED
                    it.disconnected(
                        this@RoPEBle
                    )
                }
                ConnectionState.CONNECTED -> {
                    gatt?.let {
                        bluetoothGatt = it
                        bluetoothGatt.discoverServices()
                    }
                }
                else -> {
                    throw IllegalStateException("Bluetooth state changed to unhandled state")
                }
            }
        }

        override fun onServicesDiscovered(gatt: BluetoothGatt?, status: Int) {
            if (BluetoothGatt.GATT_SUCCESS == status) {
                gatt?.getService(IDs.serviceUUID)?.let {
                    characteristic = it.getCharacteristic(IDs.characteristicUUID)
                    try {
                        this@RoPEBle.connectionState = ConnectionState.CONNECTED
                        Listeners.onConnected?.invoke()
                    } catch (e: Exception) {
                        d("RoPE", e.message!!)
                    }
                    enableNotifications()
                }
            }
        }

        private fun enableNotifications() {
            bluetoothGatt.setCharacteristicNotification(characteristic, true)
            characteristic.descriptors.forEach { descriptor ->
                descriptor.value = BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE
                bluetoothGatt.writeDescriptor(descriptor)
            }
        }

        override fun onCharacteristicChanged(
            gatt: BluetoothGatt,
            characteristic: BluetoothGattCharacteristic
        ) {
            val characteristicValue = characteristic.getStringValue(0)
            Listeners.onMessage.forEach { it(characteristicValue) }
        }
    }

    private object ConnectionState {
        const val CONNECTED = 2
        const val DISCONNECTED = 0
    }

    private object IDs {
        val serviceUUID: UUID = UUID.fromString("0000ffe0-0000-1000-8000-00805f9b34fb")
        val characteristicUUID: UUID = UUID.fromString("0000ffe1-0000-1000-8000-00805f9b34fb")
    }
}