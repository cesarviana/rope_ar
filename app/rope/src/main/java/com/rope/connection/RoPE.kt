package com.rope.connection

import android.bluetooth.*
import android.content.Context
import android.util.Log
import android.util.Log.d
import java.util.*

class RoPE(private val context: Context, private val device: BluetoothDevice) {

    private object Listeners {
        val onExecutionFinished: MutableList<RoPEExecutionFinishedListener> = mutableListOf()
        val onExecutionStarted: MutableList<RoPEExecutionStartedListener> = mutableListOf()
        val onActionExecution: MutableList<RoPEActionListener> = mutableListOf()
        val onStartPressed: MutableList<RoPEStartPressedListener> = mutableListOf()
        val onMessage: MutableList<(message: String) -> Unit> = mutableListOf()
        var onConnected: (() -> Unit)? = null
        val onDisconnected: MutableList<RoPEDisconnectedListener> = mutableListOf()
    }

    var actionIndex = 0
    private val callback = MyGattCallback()

    private lateinit var bluetoothGatt: BluetoothGatt

    private lateinit var characteristic: BluetoothGattCharacteristic

    enum class State {
        EXECUTING, STOPPED
    }

    private var state = State.STOPPED

    enum class Action {
        BACKWARD {
            override val stringSequence: String
                get() = "b"
        },
        FORWARD {
            override val stringSequence: String
                get() = "f"
        },
        LEFT {
            override val stringSequence: String
                get() = "l"
        },
        RIGHT {
            override val stringSequence: String
                get() = "r"
        },
        EXECUTE {
            override val stringSequence: String
                get() = "e"
        },
        SOUND_OFF {
            override val stringSequence: String
                get() = "s"
        },
        NULL {
            override val stringSequence: String
                get() = ""
        };

        abstract val stringSequence: String
    }

    init {
        setupSelfListeners()
    }

    private fun setupSelfListeners() {
        this.onMessage { message ->
            if (message.contains("memory is")) { // TODO update firmware to send something like <required:start> message
                Listeners.onStartPressed.forEach { it.startPressed(this) }
            }
        }
        this.onMessage { message ->
            val isStartMessage = message.contains("start")
            val canStart = isStartMessage && isStopped()
            if (canStart) {
                state = State.EXECUTING
                Listeners.onExecutionStarted.forEach { it.executionStarted(this) }
            }
        }
        this.onMessage { message ->
            val regex = "<executed:(?<actionIndex>\\d+)>\r\n".toRegex()
            if (regex.containsMatchIn(message)) {
                val matches = regex.matchEntire(message)
                val numberGroup =
                    1 // the group 0 is the complete string, group 1 is the desired info
                val actionIndex = matches!!.groups[numberGroup]!!.value
                actionIndex.toIntOrNull()?.let { index ->
                    this.actionIndex = index
                    Listeners.onActionExecution.forEach { it.actionFinished(this) }
                }
            }
        }
        this.onMessage { message ->
            if (message.contains("terminated")) {
                state = State.STOPPED
                Listeners.onExecutionFinished.forEach { it.executionEnded(this) }
            }
        }
    }

    private val commandsPrefix = "cmds:"

    fun connect() {
        if (isConnected())
            Log.w("ROPE", "Device is already connected, not connecting again")
        else
            device.connectGatt(context, false, callback)
    }

    fun isConnected() = device.bondState == BluetoothDevice.BOND_BONDED
    fun isConnecting() = device.bondState == BluetoothDevice.BOND_BONDING

    private fun send(command: String) {
        characteristic.value = command.toByteArray()
        bluetoothGatt.writeCharacteristic(characteristic)
    }

    fun onDisconnected(ropeDisconnectListener: RoPEDisconnectedListener) {
        Listeners.onDisconnected.add(ropeDisconnectListener)
    }

    fun onConnected(function: () -> Unit) {
        Listeners.onConnected = function
    }

    private fun onMessage(function: (message: String) -> Unit) {
        Listeners.onMessage.add(function)
    }

    fun onStartedPressed(ropeStartPressedListener: RoPEStartPressedListener) {
        Listeners.onStartPressed.add(ropeStartPressedListener)
    }

    fun onActionFinished(ropeActionListener: RoPEActionListener) {
        Listeners.onActionExecution.add(ropeActionListener)
    }

    fun execute(vararg actions: Action) = execute(actions.asList())

    fun execute(actionList: List<Action>) {
        if (isStopped()) {
            val actions = actionList.joinToString("") { it.stringSequence }
            val executeSuffix = Action.EXECUTE.stringSequence
            val command = "$commandsPrefix$actions$executeSuffix"
            send(command)
        }
    }

    private fun isStopped() = state == State.STOPPED

    fun onExecutionStarted(executionStartedListener: RoPEExecutionStartedListener) {
        Listeners.onExecutionStarted.add(executionStartedListener)
    }

    fun onExecutionFinished(executionFinishedListener: RoPEExecutionFinishedListener) {
        Listeners.onExecutionFinished.add(executionFinishedListener)
    }

    fun disconnect() {
        bluetoothGatt.disconnect()
    }

    fun removeDisconnectedListener(listener: RoPEDisconnectedListener) {
        Listeners.onDisconnected.remove(listener)
    }

    fun removeStartPressedListener(listener: RoPEStartPressedListener) {
        Listeners.onStartPressed.remove(listener)
    }

    fun removeActionListener(listener: RoPEActionListener) {
        Listeners.onActionExecution.remove(listener)
    }

    fun removeExecutionStartedListener(listener: RoPEExecutionStartedListener) {
        Listeners.onExecutionStarted.remove(listener)
    }

    fun removeExecutionFinishedListener(listener: RoPEExecutionFinishedListener) {
        Listeners.onExecutionFinished.remove(listener)
    }

    private inner class MyGattCallback : BluetoothGattCallback() {

        override fun onConnectionStateChange(gatt: BluetoothGatt?, status: Int, newState: Int) {
            when (newState) {
                ConnectionState.DISCONNECTED -> Listeners.onDisconnected.forEach {
                    it.disconnected(
                        this@RoPE
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
                        Listeners.onConnected?.let { it() }
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
        const val CONNECTING = 1
        const val DISCONNECTED = 0
    }

    private object IDs {
        val serviceUUID: UUID = UUID.fromString("0000ffe0-0000-1000-8000-00805f9b34fb")
        val characteristicUUID: UUID = UUID.fromString("0000ffe1-0000-1000-8000-00805f9b34fb")
    }
}

interface RoPEDisconnectedListener {
    fun disconnected(rope: RoPE)
}

interface RoPEStartPressedListener {
    fun startPressed(rope: RoPE)
}

interface RoPEActionListener {
    fun actionFinished(rope: RoPE)
}

interface RoPEExecutionStartedListener {
    fun executionStarted(rope: RoPE)
}

interface RoPEExecutionFinishedListener {
    fun executionEnded(rope: RoPE)
}