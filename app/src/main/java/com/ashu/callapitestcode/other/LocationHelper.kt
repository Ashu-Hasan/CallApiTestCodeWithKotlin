package com.ashu.callapitestcode.other

import android.Manifest
import android.app.Activity
import android.content.pm.PackageManager
import androidx.annotation.RequiresPermission
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority

class LocationHelper(
    private val activity: Activity
) {

    private val fusedLocationClient: FusedLocationProviderClient =
        LocationServices.getFusedLocationProviderClient(activity)

    interface LocationCallback {
        fun onLocationReceived(lat: Double, lng: Double)
        fun onFailed(message: String?)
    }

    fun hasPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            activity,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    }

    @RequiresPermission(allOf = [Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION])
    fun fetchLocation(
        onSuccess: (Double, Double) -> Unit,
        onError: (String?) -> Unit
    ) {
        if (!hasPermission()) {
            onError("Location permission not granted")
            return
        }

        fusedLocationClient.getCurrentLocation(
            Priority.PRIORITY_HIGH_ACCURACY,
            null
        ).addOnSuccessListener { location ->
            location?.let {
                onSuccess(it.latitude, it.longitude)
            } ?: onError("Location null")
        }.addOnFailureListener {
            onError(it.message)
        }
    }
}