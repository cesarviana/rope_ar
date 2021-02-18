package com.rope.connection

import android.bluetooth.*
import android.content.Context
import android.util.Log.d
import java.lang.IllegalStateException
import java.util.*

class RoPE(private val context: Context, private val device: BluetoothDevice) {

    private object Listeners {

        fun clear() {
            onExecutionFinished.clear()
            onExecutionStarted.clear()
            onActionExecution.clear()
            onStartPressed.clear()
            onMessage.clear()
            onConnected.clear()
            onDisconnected.clear()
        }

        val onExecutionFinished: MutableList<() -> Unit> = mutableListOf()
        val onExecutionStarted: MutableList<() -> Unit> = mutableListOf()
        val onActionExecution: MutableList<(actionIndex: Int) -> Unit> = mutableListOf()
        val onStartPressed: MutableList<() -> Unit> = mutableListOf()
        val onMessage: MutableList<(message: String) -> Unit> = mutableListOf()
        val onConnected: MutableList<() -> Unit> = mutableListOf()
        val onDisconnected: MutableList<() -> Unit> = mutableListOf()
    }

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
        Listeners.clear()
        setupSelfListeners()
    }

    private fun setupSelfListeners() {
        this.onMessage {
            if (it.contains("memory is")) { // TODO update firmware to send something like <required:start> message
                Listeners.onStartPressed.forEach { it() }
            }
        }
        this.onMessage { message ->
            val isStartMessage = message.contains("start")
            val canStart = isStartMessage && isStopped()
            if (canStart) {
                state = State.EXECUTING
                Listeners.onExecutionStarted.forEach { it() }
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
                    Listeners.onActionExecution.forEach { it(index) }
                }
            }
        }
        this.onMessage { message ->
            if (message.contains("terminated")) {
                state = State.STOPPED
                Listeners.onExecutionFinished.forEach { it() }
            }
        }
    }

    private val commandsPrefix = "cmds:"

    fun connect() {
        device.connectGatt(context, false, callback)
    }

    fun isConnected() = device.bondState == BluetoothDevice.BOND_BONDED
    fun isConnecting() = device.bondState == BluetoothDevice.BOND_BONDING

    private fun send(command: String) {
        characteristic.value = command.toByteArray()
        bluetoothGatt.writeCharacteristic(characteristic)
    }

    fun onDisconnected(function: () -> Unit) {
        Listeners.onDisconnected.add(function)
    }

    fun onConnected(function: () -> Unit) {
        Listeners.onConnected.add(function)
    }

    private fun onMessage(function: (message: String) -> Unit) {
        Listeners.onMessage.add(function)
    }

    fun onStartedPressed(function: () -> Unit) {
        Listeners.onStartPressed.add(function)
    }

    fun onActionFinished(function: (actionIndex: Int) -> Unit) {
        Listeners.onActionExecution.add(function)
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

    fun onExecutionStarted(function: () -> Unit) {
        Listeners.onExecutionStarted.add(function)
    }

    fun onExecutionFinished(function: () -> Unit) {
        Listeners.onExecutionFinished.add(function)
    }

    private inner class MyGattCallback : BluetoothGattCallback() {

        override fun onConnectionStateChange(gatt: BluetoothGatt?, status: Int, newState: Int) {
            when (newState) {
                ConnectionState.DISCONNECTED -> Listeners.onDisconnected.forEach { it() }
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
                        Listeners.onConnected.forEach { it() }
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