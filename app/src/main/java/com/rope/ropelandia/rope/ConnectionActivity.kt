package com.rope.ropelandia.rope

import android.bluetooth.*
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanFilter
import android.bluetooth.le.ScanResult
import android.bluetooth.le.ScanSettings
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.ParcelUuid
import android.util.Log
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.rope.ropelandia.PermissionChecker
import com.rope.ropelandia.R
import com.rope.ropelandia.databinding.ActivityConnectionBinding
import com.rope.ropelandia.log.Logger
import model.Task
import viewmodel.ConnectionViewModel
import java.util.*

class ConnectionActivity : AppCompatActivity() {

    private val TAG = ConnectionActivity::class.java.simpleName

    // Todo: Hide bluetooth code in rope finder
    private val bluetoothAdapter: BluetoothAdapter? by lazy(LazyThreadSafetyMode.NONE) {
        val bluetoothManager = getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        bluetoothManager.adapter
    }
    private lateinit var bluetoothGatt: BluetoothGatt
    private lateinit var characteristic: BluetoothGattCharacteristic

    private lateinit var binding: ActivityConnectionBinding

    private var scanning = false
    private var self = this

    private val permissionChecker = PermissionChecker()

    private val myScanCallback = MyScanCallback()
    private val myGattCallback = MyGattCallback()

    private lateinit var logger: Logger
    private lateinit var model: ConnectionViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_connection)

        model = ConnectionViewModel()
        model.tasks = listOf(
            Task("Atividade livre inicial"),
            Task("Posterior 1 avanço"),
            Task("Posterior 2 avanços"),
            Task("Posterior avanço e direita"),

            Task("Lateral 1 avanço"),
            Task("Lateral 2 avanços"),
            Task("Lateral avanço e direita"),

            Task("Frontal 1 avanço"),
            Task("Frontal 2 avanços"),
            Task("Frontal avanço e direita"),
        )

        binding.model = model

        findViewById<Button>(R.id.nextTask).setOnClickListener {
            model.nextTask()
            binding.invalidateAll()
        }

        findViewById<Button>(R.id.previousTask).setOnClickListener {
            model.previousTask()
            binding.invalidateAll()
        }

        logger = Logger(this)

        scan(myScanCallback)
    }

    private fun requestEnableBluetoothOrStartScan() {
        bluetoothAdapter?.let {
            if (!it.isEnabled) {
                requestEnableBluetooth()
            } else {
                this.scan(myScanCallback)
            }
        }
    }

    private fun requestEnableBluetooth() {
        val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
        startActivityForResult(enableBtIntent, REQUEST_ENABLE_BLUETOOTH)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {

        super.onActivityResult(requestCode, resultCode, data)

        when (requestCode) {
            REQUEST_ENABLE_BLUETOOTH -> {
                if (resultCode == RESULT_OK) {
                    scan(myScanCallback)
                } else {
                    Toast.makeText(this, "Falha ao ativar Bluetooth", Toast.LENGTH_SHORT).show()
                }
            }
        }

    }

    private inner class MyScanCallback : ScanCallback() {
        override fun onScanResult(callbackType: Int, result: ScanResult?) {
            result?.let {
                bluetoothGatt = it.device.connectGatt(self, true, myGattCallback)
            }
            scanning = false
        }

        override fun onScanFailed(errorCode: Int) {
            super.onScanFailed(errorCode)
            val cause = when (errorCode) {
                SCAN_FAILED_ALREADY_STARTED -> {
                    "Scaneamento em execução"
                }
                else -> {
                    scanning = false
                    "Falha de conexão"
                }
            }
            Toast.makeText(self, cause, Toast.LENGTH_SHORT).show()
        }
    }

    private inner class MyGattCallback : BluetoothGattCallback() {

        private val DISCONNECTED = 0
        private val CONNECTED = 2

        override fun onConnectionStateChange(gatt: BluetoothGatt?, status: Int, newState: Int) {
            when (newState) {
                DISCONNECTED -> {
                    model.rope.connected = false
                    binding.invalidateAll()
                    Toast.makeText(self, "Desconectado", Toast.LENGTH_SHORT).show()
                }
                CONNECTED -> {
                    model.rope.connected = true
                    binding.invalidateAll()
                    gatt?.discoverServices()
                }
            }
        }

        override fun onServicesDiscovered(gatt: BluetoothGatt?, status: Int) {

            if (BluetoothGatt.GATT_SUCCESS == status) {
                val serviceUUID = UUID.fromString("0000ffe0-0000-1000-8000-00805f9b34fb")
                gatt?.getService(serviceUUID)?.let {
                    val characteristicUUID = UUID.fromString("0000ffe1-0000-1000-8000-00805f9b34fb")
                    characteristic = it.getCharacteristic(characteristicUUID)
                    send("cmds:e")
                    enableNotifications()
                }
            }
        }

        private fun enableNotifications() {
            bluetoothGatt.setCharacteristicNotification(characteristic, true)
            try {
                characteristic.descriptors.forEach { descriptor ->
                    descriptor.value = BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE
                    bluetoothGatt.writeDescriptor(descriptor)
                }
            } catch (e: Throwable) {
                Log.e(TAG, e.message ?: "Error when enabling notifications")
            }
        }

        override fun onCharacteristicChanged(
            gatt: BluetoothGatt,
            characteristic: BluetoothGattCharacteristic
        ) {
            val tag = model.task.description
            val stringValue = characteristic.getStringValue(0)
            logger.log(tag, stringValue)
        }
    }

    private fun send(command: String) {
        characteristic.value = command.toByteArray()
        bluetoothGatt.writeCharacteristic(characteristic)
    }

    private fun scan(scanCallback: ScanCallback) {
        if (!scanning) {
            requestPermissionAndScan(scanCallback)
        }
    }

    private fun requestPermissionAndScan(scanCallback: ScanCallback) {
        if (Build.VERSION.SDK_INT >= 23) {
            permissionChecker.executeOrRequestPermission(
                this,
                PERMISSIONS,
                REQUEST_BLUETOOTH_PERMISSION_CODE
            ) {
                val filter = createScanFilter()
                val settings = createScanSettings()
                scanning = true
                bluetoothAdapter?.bluetoothLeScanner?.startScan(filter, settings, scanCallback)
            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        if (requestCode == REQUEST_BLUETOOTH_PERMISSION_CODE) {
            permissionChecker.executeOrCry(this, PERMISSIONS) {
                scan(myScanCallback)
            }
        }
    }

    companion object {
        private const val REQUEST_BLUETOOTH_PERMISSION_CODE = 11
        private const val REQUEST_ENABLE_BLUETOOTH = 12
        private val PERMISSIONS = arrayOf(
            android.Manifest.permission.ACCESS_COARSE_LOCATION,
            android.Manifest.permission.BLUETOOTH,
            android.Manifest.permission.BLUETOOTH_ADMIN,
            android.Manifest.permission.WRITE_EXTERNAL_STORAGE,
            android.Manifest.permission.READ_EXTERNAL_STORAGE
        )
    }

    private fun createScanSettings(): ScanSettings? {
        return ScanSettings.Builder()
            .setScanMode(ScanSettings.SCAN_MODE_LOW_POWER)
            .build()
    }

    private fun createScanFilter(): List<ScanFilter> {
        val scanFilter = ScanFilter.Builder()
            .setDeviceName("-['.']- RoPE")
            .setServiceUuid(getParcelUUID())
            .build()
        return listOf(scanFilter)
    }

    private fun getParcelUUID() =
        ParcelUuid(UUID.fromString("0000ffe0-0000-1000-8000-00805f9b34fb"))

}