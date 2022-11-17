package com.example.android.politicalpreparedness

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Geocoder
import android.location.Location
import android.provider.Settings
import android.util.Log
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.example.android.politicalpreparedness.network.models.Address
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.material.snackbar.Snackbar
import java.util.*

private lateinit var fusedLocationProviderClient: FusedLocationProviderClient

private const val LOCATION_PERMISSION_REQUEST_CODE = 34

fun Fragment.initializeLocationClient() {
    fusedLocationProviderClient =
        LocationServices.getFusedLocationProviderClient(requireActivity())
}

fun Fragment.checkLocationPermissions(): Boolean {
    return if (isPermissionGranted()) {
        true
    } else {
        requestPermissions(
            arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
            LOCATION_PERMISSION_REQUEST_CODE
        )
        Log.e("Request location", "permissions")
        false
    }
}

fun Fragment.isPermissionGranted(): Boolean {
    return ContextCompat.checkSelfPermission(
        requireContext(),
        Manifest.permission.ACCESS_FINE_LOCATION
    ) == PackageManager.PERMISSION_GRANTED
}

fun Fragment.getLocation(callback: (input: Address) -> Unit) {
    try {
        val locationResult = fusedLocationProviderClient.lastLocation
        locationResult.addOnCompleteListener { task ->
            if (task.isSuccessful && task.result != null) {
                val address = geoCodeLocation(task.result!!, requireContext())
                callback(address)
            } else {
                Snackbar.make(
                    requireView(),
                    R.string.error_location_required, Snackbar.LENGTH_INDEFINITE
                ).setAction(R.string.location_settings) {
                    startActivity(Intent().apply {
                        action = Settings.ACTION_LOCATION_SOURCE_SETTINGS
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    })
                }.show()
            }
        }
    } catch (e: SecurityException) {
        Snackbar.make(
            requireView(),
            e.message ?: "Security Exception",
            Snackbar.LENGTH_LONG
        ).show()
        Log.e("Exception: %s", e.message, e)
    }
}

fun Fragment.onRequestPermissionResultForLocation(requestCode: Int, grantResults: IntArray, callback: (input: Address) -> Unit) {
    if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
        if (grantResults.isNotEmpty() && (grantResults.first() == PackageManager.PERMISSION_GRANTED)) {
            getLocation(callback)
        } else {
            Snackbar.make(
                requireView(),
                R.string.error_permission_denied_explanation,
                Snackbar.LENGTH_LONG
            ).show()
        }
    }
}

private fun geoCodeLocation(location: Location, context: Context): Address {
    val geocoder = Geocoder(context, Locale.getDefault())
    return geocoder.getFromLocation(location.latitude, location.longitude, 1)
        .map { address ->
            Address(
                address.thoroughfare,
                address.subThoroughfare,
                address.locality,
                address.adminArea,
                address.postalCode
            )
        }
        .first()
}

