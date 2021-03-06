package com.rope.connection.ble

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
import android.os.ParcelUuid
import android.util.Log
import com.rope.connection.RoPE
import com.rope.connection.RoPEFinder
import java.util.*

private const val TAG = "ROPE_FINDER_BLE"

/**
 * Object to be used by other projects. It will find a instance of RoPE.
 * This implementation uses Bluetooth connection, but the interface don't exposes
 * Bluetooth.
 */
class RoPEFinderBle(var activity: Activity, private val handler: Handler) : RoPEFinder {

    init {
        Listeners.clear()
    }

    var rope: RoPEBle? = null

    private object BlePermissions {
        const val requestCode = 11
        val list = arrayOf(
            android.Manifest.permission.ACCESS_COARSE_LOCATION,
            android.Manifest.permission.ACCESS_FINE_LOCATION,
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
            onConnectionFailed.clear()
        }

        val onRoPEFound = mutableListOf<(rope: RoPEBle) -> Unit>()
        val onRequestEnableConnection =
            mutableListOf<(RoPEFinder.EnableConnectionRequest) -> Unit>()
        val onConnectionFailed = mutableListOf<(errorCode: Int) -> Unit>()
    }

    private val bluetoothAdapter by lazy(LazyThreadSafetyMode.NONE) {
        val bluetoothManager =
            activity.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        bluetoothManager.adapter
    }

    private val permissionChecker = com.rope.droideasy.PermissionChecker()
    private val myScanCallback = MyScanCallback()

    override fun findRoPE() {
        if (isSearching()) {
            Log.w(TAG, "RoPE scanning already started")
            return
        }
        requestEnableBluetoothOrStartScan()
    }

    override fun isSearching() = bluetoothAdapter.isDiscovering

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
        val enableBleRequest = RoPEFinder.EnableConnectionRequest(intent, code)
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
                bluetoothAdapter.bluetoothLeScanner.startScan(filter, settings, scanCallback)
            }
        }
    }

    private fun createScanSettings(): ScanSettings? {
        return ScanSettings.Builder()
            .setScanMode(ScanSettings.SCAN_MODE_BALANCED)
            .build()
    }

    private fun createScanFilter(): MutableList<ScanFilter> {
        val ropeUuid = ParcelUuid(UUID.fromString("0000ffe0-0000-1000-8000-00805f9b34fb"))
        val scanFilter = ScanFilter.Builder()
            .setDeviceName("-['.']- RoPE")
            .setServiceUuid(ropeUuid)
            .build()
        return mutableListOf(scanFilter)
    }

    override fun onRequestEnableConnection(onRequestEnableConnection: (RoPEFinder.EnableConnectionRequest) -> Unit) {
        Listeners.onRequestEnableConnection.add(onRequestEnableConnection)
    }

    override fun handleConnectionAllowed(connectionAllowed: Boolean) {
        if (connectionAllowed) {
            scan(myScanCallback)
        }
    }

    override fun onRoPEFound(function: (rope: RoPE) -> Unit) {
        Listeners.onRoPEFound.add(function)
    }

    override fun handleRequestConnectionPermissionResult(requestCode: Int) {
        if (requestCode == BlePermissions.requestCode) {
            permissionChecker.executeOrCry(activity, BlePermissions.list) {
                scan(myScanCallback)
            }
        }
    }

    override fun onConnectionFailed(function: (errorCode: Int) -> Unit) {
        Listeners.onConnectionFailed.add(function)
    }

    private fun createRoPE(scanResult: ScanResult): RoPEBle {
        return RoPEBle(activity.applicationContext, scanResult.device, handler)
    }

    private inner class MyScanCallback : ScanCallback() {
        override fun onScanResult(callbackType: Int, scanResult: ScanResult?) {
            bluetoothAdapter?.bluetoothLeScanner?.stopScan(myScanCallback)

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
            if (errorCode == SCAN_FAILED_ALREADY_STARTED) {
                bluetoothAdapter?.bluetoothLeScanner?.stopScan(myScanCallback)
            }
            Listeners.onConnectionFailed.forEach { it(errorCode) }
        }
    }
}