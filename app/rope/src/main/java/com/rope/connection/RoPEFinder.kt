package com.rope.connection

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
import android.os.Handler
import android.os.Looper
import android.os.ParcelUuid
import androidx.appcompat.app.AppCompatActivity
import java.util.*

/**
 * Object to be used by other projects. It will find a instance of RoPE.
 * This implementation uses Bluetooth connection, but the interface don't exposes
 * Bluetooth.
 */
class RoPEFinder(var activity: Activity) {

    init {
        Listeners.clear()
    }

    var rope: RoPE? = null

    private object BlePermissions {
        const val requestCode = 11
        val list = arrayOf(
            android.Manifest.permission.ACCESS_COARSE_LOCATION,
            android.Manifest.permission.BLUETOOTH,
            android.Manifest.permission.BLUETOOTH_ADMIN
        )
    }

    private object BleEnabling {
        const val requestCode = 12
    }

    private object Listeners {

        fun clear() {
            onRoPEFound.clear()
            onRequestEnableConnection.clear()
            onEnableConnectionRefused.clear()
            onConnectionFailed.clear()
        }

        val onRoPEFound = mutableListOf<(rope: RoPE) -> Unit>()
        val onRequestEnableConnection = mutableListOf<(EnableBluetoothRequest) -> Unit>()
        val onEnableConnectionRefused = mutableListOf<(resultCode: Int) -> Unit>()
        val onConnectionFailed = mutableListOf<(errorCode: Int) -> Unit>()
    }

    private val bluetoothAdapter: BluetoothAdapter? by lazy(LazyThreadSafetyMode.NONE) {
        val bluetoothManager =
            activity.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        bluetoothManager.adapter
    }

    private val permissionChecker = com.rope.droideasy.PermissionChecker()
    private val myScanCallback = MyScanCallback()

    fun findRoPE() {
        requestEnableBluetoothOrStartScan()
    }

    private fun requestEnableBluetoothOrStartScan() {
        bluetoothAdapter?.let {
            if (!it.isEnabled) {
                requestEnableBluetooth()
            } else {
                scan(myScanCallback)
            }
        }
    }

    private fun requestEnableBluetooth() {
        val action = BluetoothAdapter.ACTION_REQUEST_ENABLE
        val intent = Intent(action)
        val code = BleEnabling.requestCode
        val enableBleRequest = EnableBluetoothRequest(intent, code)
        Listeners.onRequestEnableConnection.forEach { it(enableBleRequest) }
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
                Handler(Looper.getMainLooper()).postDelayed({
                    bluetoothAdapter?.bluetoothLeScanner?.startScan(filter, settings, scanCallback)
                }, 10000)
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
        Listeners.onRequestEnableConnection.add(onRequestEnableConnection)
    }

    fun handleRequestEnableConnectionResult(requestCode: Int, resultCode: Int) {
        val bluetoothRequest = requestCode == BleEnabling.requestCode

        if (!bluetoothRequest)
            return

        val allowedBluetooth = resultCode == AppCompatActivity.RESULT_OK

        if (allowedBluetooth) {
            scan(myScanCallback)
        } else {
            Listeners.onEnableConnectionRefused.forEach { it(resultCode) }
        }
    }

    fun onEnableConnectionRefused(function: (resultCode: Int) -> Unit) {
        Listeners.onEnableConnectionRefused.add(function)
    }

    fun onRoPEFound(function: (rope: RoPE) -> Unit) {
        Listeners.onRoPEFound.add(function)
    }

    fun handleRequestConnectionPermissionResult(requestCode: Int) {
        if (requestCode == BlePermissions.requestCode) {
            permissionChecker.executeOrCry(activity, BlePermissions.list) {
                scan(myScanCallback)
            }
        }
    }

    fun onConnectionFailed(function: (errorCode: Int) -> Unit) {
        Listeners.onConnectionFailed.add(function)
    }

    private fun createRoPE(scanResult: ScanResult): RoPE {
        return RoPE(activity.applicationContext, scanResult.device)
    }

    private inner class MyScanCallback : ScanCallback() {
        override fun onScanResult(callbackType: Int, scanResult: ScanResult?) {

            bluetoothAdapter?.bluetoothLeScanner?.stopScan(myScanCallback)

            /**
             * There is an error that causes this method to be called multiple times, even
             * the rope already being found. So, if rope is connected, we ignore further results.
             */
            if (rope != null) {
                Listeners.onRoPEFound.forEach { it(rope!!) }
            } else {
                scanResult?.let {
                    rope = createRoPE(scanResult)
                    Listeners.onRoPEFound.forEach { it(rope!!) }
                }
            }

        }

        override fun onScanFailed(errorCode: Int) {
            super.onScanFailed(errorCode)

            if (errorCode == SCAN_FAILED_ALREADY_STARTED) {
                bluetoothAdapter?.bluetoothLeScanner?.stopScan(myScanCallback)
            }

            Listeners.onConnectionFailed.forEach { it(errorCode) }
        }
    }

    data class EnableBluetoothRequest(val intent: Intent, val code: Int)
}