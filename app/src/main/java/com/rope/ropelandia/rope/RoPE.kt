package com.rope.ropelandia.rope

import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCallback
import android.bluetooth.BluetoothGattCharacteristic
import android.content.Context
import java.util.*

interface RoPE {
    fun moveForward()
}

class RoPEBluetooth(context: Context, private val device: BluetoothDevice) : RoPE {

    //var bluetoothGatt: BluetoothGatt = device.connectGatt(context, false, GattCallback)
    //private lateinit var bluetoothGattCharacteristic: BluetoothGattCharacteristic

    override fun moveForward() {
        if (device.bondState == BluetoothDevice.BOND_NONE) {
            //bluetoothGattCharacteristic.value = "cmds:fe".toByteArray()
            //bluetoothGatt.writeCharacteristic(bluetoothGattCharacteristic)
        }
    }

    object GattCallback : BluetoothGattCallback() {
        override fun onConnectionStateChange(gatt: BluetoothGatt?, status: Int, newState: Int) {
            gatt?.services?.forEach { service ->
                service.getCharacteristic(UUID.fromString("0000ffe1-0000-1000-8000-00805f9b34fb"))
            }
        }

        override fun onCharacteristicRead(
            gatt: BluetoothGatt?,
            characteristic: BluetoothGattCharacteristic?,
            status: Int
        ) {
            super.onCharacteristicRead(gatt, characteristic, status)
        }
    }
}