package com.rope.droideasy

import android.app.Activity
import android.content.pm.PackageManager
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

class PermissionChecker {
    fun executeOrRequestPermission(
        activity: Activity,
        requiredPermissions: Array<String>,
        requestCode: Int,
        function: () -> Unit
    ) {
        if (allPermissionsGranted(activity, requiredPermissions)) {
            function()
        } else {
            requestPermissions(activity, requiredPermissions, requestCode)
        }
    }

    private fun allPermissionsGranted(
        activity: Activity,
        requiredPermissions: Array<String>
    ) = requiredPermissions.all {
        ContextCompat.checkSelfPermission(
            activity, it
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun requestPermissions(
        activity: Activity,
        requiredPermissions: Array<String>,
        requestCode: Int
    ) {
        ActivityCompat.requestPermissions(
            activity, requiredPermissions, requestCode
        )
    }

    fun executeOrCry(
        activity: Activity,
        requiredPermissions: Array<String>,
        function: () -> Unit
    ) {
        if(allPermissionsGranted(activity, requiredPermissions)){
            function()
        } else {
            cry(activity)
        }
    }

    private fun cry(activity: Activity){
        Log.e("PERMISSIONS", "Permission not granted")
    }
}
