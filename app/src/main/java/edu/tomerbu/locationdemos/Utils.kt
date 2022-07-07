package edu.tomerbu.locationdemos

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.core.content.edit
import androidx.fragment.app.Fragment
import edu.tomerbu.locationdemos.ui.TAG

/**
 * Returns the `location` object as a human readable string.
 */
fun android.location.Location?.toText(): String {
    return if (this != null) {
        toString(latitude, longitude)
    } else {
        "Unknown location"
    }
}

fun toString(lat: Double, lon: Double): String {
    return "($lat, $lon)"
}

/**
 * Helper functions to simplify permission checks/requests.
 */
fun Context.hasPermission(permission: String): Boolean {

    // Background permissions didn't exit prior to Q, so it's approved by default.
    if (permission == Manifest.permission.ACCESS_BACKGROUND_LOCATION &&
        android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.Q
    ) {
        return true
    }

    return ActivityCompat.checkSelfPermission(this, permission) ==
            PackageManager.PERMISSION_GRANTED
}

fun Context.hasPreciseLocation(): Boolean =
    hasPermission(Manifest.permission.ACCESS_FINE_LOCATION)

fun Fragment.hasPreciseLocation(): Boolean =
    requireContext().hasPermission(Manifest.permission.ACCESS_FINE_LOCATION)

fun Context.hasCoarseLocation(): Boolean =
    hasPermission(Manifest.permission.ACCESS_COARSE_LOCATION)

fun Fragment.hasCoarseLocation(): Boolean =
    requireContext().hasPermission(Manifest.permission.ACCESS_COARSE_LOCATION)

fun Context.hasLocationPermission(): Boolean =
    hasPreciseLocation() or hasCoarseLocation()

fun Fragment.hasLocationPermission(): Boolean =
    requireContext().hasLocationPermission()

fun Context.hasBackgroundPermission(): Boolean =
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        hasPermission(Manifest.permission.ACCESS_BACKGROUND_LOCATION)
    } else {
        true
    }

fun Fragment.hasBackgroundPermission(): Boolean =
    requireContext().hasBackgroundPermission()


fun Fragment.requestPermissionLauncher(callback: (Boolean) -> Unit) =
    registerForActivityResult(ActivityResultContracts.RequestPermission(), callback)

fun Fragment.requestPermissionsLauncher(callback: (Map<String, Boolean>) -> Unit) =
    registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions(), callback)

/**
 * Provides access to SharedPreferences for location to Activities and Services.
 */
internal object SharedPreferenceUtil {

    const val KEY_FOREGROUND_ENABLED = "tracking_foreground_location"
    const val KEY_DID_REQUEST_PERMISSION_ALREADY = "DidRequestPermissionAlready"

    /**
     * Returns true if requesting location updates, otherwise returns false.
     *
     * @param context The [Context].
     */
    fun getLocationTrackingPref(context: Context): Boolean =
        context.getSharedPreferences(
            context.getString(R.string.preference_file_key), Context.MODE_PRIVATE
        )
            .getBoolean(KEY_FOREGROUND_ENABLED, false)

    /**
     * Stores the location updates state in SharedPreferences.
     * @param requestingLocationUpdates The location updates state.
     */
    fun saveLocationTrackingPref(context: Context, requestingLocationUpdates: Boolean) =
        context.getSharedPreferences(
            context.getString(R.string.preference_file_key),
            Context.MODE_PRIVATE
        ).edit {
            putBoolean(KEY_FOREGROUND_ENABLED, requestingLocationUpdates)
        }

    fun getDidRequestPermissionAlready(context: Context): Boolean =
        context.getSharedPreferences(
            context.getString(R.string.preference_file_key),
            Context.MODE_PRIVATE
        )
            .getBoolean(KEY_DID_REQUEST_PERMISSION_ALREADY, false)

    /**
     * Stores the location updates state in SharedPreferences.
     * @param requestingLocationUpdates The location updates state.
     */
    fun saveDidRequestPermissionAlready(context: Context, requestingLocationUpdates: Boolean) =
        context.getSharedPreferences(
            context.getString(R.string.preference_file_key),
            Context.MODE_PRIVATE
        ).edit {
            putBoolean(KEY_DID_REQUEST_PERMISSION_ALREADY, requestingLocationUpdates)
        }
}
