package com.rope.ropelandia.rope

import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanFilter
import android.bluetooth.le.ScanResult
import android.bluetooth.le.ScanSettings
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.ParcelUuid
import androidx.appcompat.app.AppCompatActivity
import com.rope.ropelandia.PermissionChecker
import java.util.*

class RoPEFinder(private val activity: Activity) {

    object BlePermissions {
        const val requestCode = 11
        val list = arrayOf(
            android.Manifest.permission.ACCESS_COARSE_LOCATION,
            android.Manifest.permission.BLUETOOTH,
            android.Manifest.permission.BLUETOOTH_ADMIN
        )
    }

    object BleEnabling {
        const val requestCode = 12
    }

    private lateinit var onRoPEFound: (rope: RoPE) -> Unit
    private lateinit var onRequestEnableConnection: (EnableBluetoothRequest) -> Unit
    private lateinit var onEnableConnectionRefused: (resultCode: Int) -> Unit

    private val bluetoothAdapter: BluetoothAdapter? by lazy(LazyThreadSafetyMode.NONE) {
        val bluetoothManager =
            activity.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        bluetoothManager.adapter
    }

    private val permissionChecker = PermissionChecker()
    private val myScanCallback = MyScanCallback()

    fun findRoPE() {
        requestEnableBluetoothOrStartScan()
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
        val action = BluetoothAdapter.ACTION_REQUEST_ENABLE
        val intent = Intent(action)
        val code = BleEnabling.requestCode
        val enableBleRequest = EnableBluetoothRequest(intent, code)
        this.onRequestEnableConnection(enableBleRequest)
    }

    private fun scan(scanCallback: ScanCallback) {
        requestPermissionAndScan(scanCallback)
    }

    private fun requestPermissionAndScan(scanCallback: ScanCallback) {
        if (Build.VERSION.SDK_INT >= 23) {
            permissionChecker.executeOrRequestPermission(
                activity,
                BlePermissions.list,
                BlePermissions.requestCode
            ) {
                val filter = createScanFilter()
                val settings = createScanSettings()
                bluetoothAdapter?.bluetoothLeScanner?.startScan(filter, settings, scanCallback)
            }
        }
    }

    private fun createScanSettings(): ScanSettings? {
        return ScanSettings.Builder()
            .setScanMode(ScanSettings.SCAN_MODE_LOW_POWER)
            .build()
    }

    private fun createScanFilter(): List<ScanFilter> {
        val ropeUuid = ParcelUuid(UUID.fromString("0000ffe0-0000-1000-8000-00805f9b34fb"))
        val scanFilter = ScanFilter.Builder()
            .setDeviceName("-['.']- RoPE")
            .setServiceUuid(ropeUuid)
            .build()
        return listOf(scanFilter)
    }

    fun onRequestEnableConnection(onRequestEnableConnection: (EnableBluetoothRequest) -> Unit) {
        this.onRequestEnableConnection = onRequestEnableConnection
    }

    fun handleRequestEnableConnectionResult(requestCode: Int, resultCode: Int) {
        val bluetoothRequest = requestCode == BleEnabling.requestCode

        if (!bluetoothRequest)
            return

        val allowedBluetooth = resultCode == AppCompatActivity.RESULT_OK

        if (allowedBluetooth) {
            scan(myScanCallback)
        } else {
            this.onEnableConnectionRefused(resultCode)
        }
    }

    fun onEnableConnectionRefused(function: (resultCode: Int) -> Unit) {
        this.onEnableConnectionRefused = function
    }

    fun onRoPEFound(function: (rope: RoPE) -> Unit) {
        this.onRoPEFound = function
    }

    fun handleRequestConnectionPermissionResult(requestCode: Int) {
        if (requestCode == BlePermissions.requestCode) {
            permissionChecker.executeOrCry(activity, BlePermissions.list) {
                scan(myScanCallback)
            }
        }
    }

    private fun createRoPE(scanResult: ScanResult): RoPE {
        return RoPE()
    }

    private inner class MyScanCallback : ScanCallback() {
        override fun onScanResult(callbackType: Int, scanResult: ScanResult?) {
            scanResult?.let {
                val rope = createRoPE(scanResult)
                onRoPEFound(rope)
            }
//            result?.let {
//                bluetoothGatt = it.device.connectGatt(self, true, myGattCallback)
//            }
        }

        override fun onScanFailed(errorCode: Int) {
            super.onScanFailed(errorCode)
            val cause = when (errorCode) {
                SCAN_FAILED_ALREADY_STARTED -> {
                    "Scaneamento em execução"
                }
                else -> {
                    "Falha de conexão"
                }
            }
        }
    }

    data class EnableBluetoothRequest(val intent: Intent, val code: Int)
}