package edu.tomerbu.locationdemos.ui

import android.Manifest
import android.app.NotificationManager
import android.content.*
import android.content.pm.PackageManager
import android.location.Location
import android.net.Uri
import android.os.Bundle
import android.os.IBinder
import android.provider.Settings
import android.util.Log
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import edu.tomerbu.locationdemos.BuildConfig
import edu.tomerbu.locationdemos.R
import edu.tomerbu.locationdemos.SharedPreferenceUtil
import edu.tomerbu.locationdemos.helpers.NotificationHelper
import edu.tomerbu.locationdemos.location.LocationRepository
import edu.tomerbu.locationdemos.services.ForegroundOnlyLocationService
import edu.tomerbu.locationdemos.toText
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import javax.inject.Inject

internal const val TAG = "LocationActivity"
internal const val REQUEST_FOREGROUND_ONLY_PERMISSIONS_REQUEST_CODE = 34

@AndroidEntryPoint
class LocationActivity : AppCompatActivity(), SharedPreferences.OnSharedPreferenceChangeListener {
    private var foregroundOnlyLocationServiceBound = false
    private val isMonitoring
        get() =
            sharedPreferences.getBoolean(SharedPreferenceUtil.KEY_FOREGROUND_ENABLED, false)

    // Provides location updates for while-in-use feature.
    private var foregroundOnlyLocationService: ForegroundOnlyLocationService? = null
        set(value) {
            field = value
            if (isMonitoring) {
                field?.subscribeToLocationUpdates()
            }
        }

    private lateinit var sharedPreferences: SharedPreferences

    private lateinit var foregroundOnlyLocationButton: Button

    private lateinit var outputTextView: TextView

    // Monitors connection to the while-in-use service.
    private val foregroundOnlyServiceConnection = object : ServiceConnection {

        override fun onServiceConnected(name: ComponentName, service: IBinder) {
            val binder = service as ForegroundOnlyLocationService.LocalBinder
            foregroundOnlyLocationService = binder.service
            foregroundOnlyLocationServiceBound = true
        }

        override fun onServiceDisconnected(name: ComponentName) {
            foregroundOnlyLocationService = null
            foregroundOnlyLocationServiceBound = false
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_location)

        sharedPreferences =
            getSharedPreferences(getString(R.string.preference_file_key), Context.MODE_PRIVATE)

        foregroundOnlyLocationButton = findViewById(R.id.foreground_only_location_button)
        outputTextView = findViewById(R.id.output_text_view)


        //the button says stop receiving when we are receiving
        foregroundOnlyLocationButton.setOnClickListener {
            //check if we are currently receiving:
            val enabled = sharedPreferences.getBoolean(
                SharedPreferenceUtil.KEY_FOREGROUND_ENABLED, false
            )

            //check if we are currently receiving: if so cancel (toggle)
            if (enabled) {
                unsubscribeToLocationUpdates()
            } else {
                //else -> start receiving
                if (foregroundPermissionApproved()) {
                    subscribeToLocationUpdates()
                } else {
                    requestForegroundPermissions()
                }
            }
        }
    }

    override fun onStart() {
        super.onStart()

        updateButtonState(
            isMonitoring
        )
        sharedPreferences.registerOnSharedPreferenceChangeListener(this)

        val serviceIntent = Intent(this, ForegroundOnlyLocationService::class.java)
        bindService(serviceIntent, foregroundOnlyServiceConnection, Context.BIND_AUTO_CREATE)
    }

    override fun onStop() {
        if (foregroundOnlyLocationServiceBound) {
            unbindService(foregroundOnlyServiceConnection)
            foregroundOnlyLocationServiceBound = false
        }
        sharedPreferences.unregisterOnSharedPreferenceChangeListener(this)

        super.onStop()
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences, key: String) {
        // Updates button states if new while in use location is added to SharedPreferences.
        if (key == SharedPreferenceUtil.KEY_FOREGROUND_ENABLED) {
            updateButtonState(
                sharedPreferences.getBoolean(
                    SharedPreferenceUtil.KEY_FOREGROUND_ENABLED, false
                )
            )
        }
    }

    private fun foregroundPermissionApproved(): Boolean {
        return PackageManager.PERMISSION_GRANTED == ActivityCompat.checkSelfPermission(
            this,
            Manifest.permission.ACCESS_FINE_LOCATION
        )
    }

    private fun requestForegroundPermissions() {
        val provideRationale = foregroundPermissionApproved()

        // If the user denied a previous request, but didn't check "Don't ask again", provide
        // additional rationale.
        if (provideRationale) {
            Snackbar.make(
                findViewById(R.id.activity_main),
                R.string.permission_rationale,
                Snackbar.LENGTH_LONG
            )
                .setAction(R.string.ok) {
                    // Request permission
                    ActivityCompat.requestPermissions(
                        this@LocationActivity,
                        arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                        REQUEST_FOREGROUND_ONLY_PERMISSIONS_REQUEST_CODE
                    )
                }
                .show()
        } else {
            Log.d(TAG, "Request foreground only permission")
            ActivityCompat.requestPermissions(
                this@LocationActivity,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                REQUEST_FOREGROUND_ONLY_PERMISSIONS_REQUEST_CODE
            )
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        Log.d(TAG, "onRequestPermissionResult")

        when (requestCode) {
            REQUEST_FOREGROUND_ONLY_PERMISSIONS_REQUEST_CODE -> when {
                grantResults.isEmpty() ->
                    // If user interaction was interrupted, the permission request
                    // is cancelled and you receive empty arrays.
                    Log.d(TAG, "User interaction was cancelled.")

                grantResults[0] == PackageManager.PERMISSION_GRANTED ->
                    // Permission was granted.
                    subscribeToLocationUpdates()

                else -> {
                    // Permission denied.
                    updateButtonState(false)

                    Snackbar.make(
                        findViewById(R.id.activity_main),
                        R.string.permission_denied_explanation,
                        Snackbar.LENGTH_LONG
                    )
                        .setAction(R.string.settings) {
                            // Build intent that displays the App settings screen.
                            val intent = Intent()
                            intent.action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
                            val uri = Uri.fromParts(
                                "package",
                                BuildConfig.APPLICATION_ID,
                                null
                            )
                            intent.data = uri
                            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                            startActivity(intent)
                        }
                        .show()
                }
            }
        }
    }

    private fun updateButtonState(trackingLocation: Boolean) {
        if (trackingLocation) {
            foregroundOnlyLocationButton.text =
                getString(R.string.stop_location_updates_button_text)
        } else {
            foregroundOnlyLocationButton.text =
                getString(R.string.start_location_updates_button_text)
        }
    }

    private fun logResultsToScreen(output: String) {
        val outputWithPreviousLogs = "$output\n${outputTextView.text}"
        outputTextView.text = outputWithPreviousLogs
    }

    //receive location updates via Flow
    @Inject
    lateinit var repository: LocationRepository

    // Get a reference to the Job from the Flow so we can stop it from UI events
    private var locationFlow: Job? = null


    private fun subscribeToLocationUpdates() {
        // Observe locations via Flow as they are generated by the repository
        locationFlow = lifecycleScope.launchWhenStarted {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                repository.getLocations().onEach {
                    Log.d(TAG, "subscribeToLocationUpdates: Location: ${it.toText()}")
                }.collect{
                    logResultsToScreen("Activity location: ${it.toText()}")
                }
            }
        }
        foregroundOnlyLocationService?.subscribeToLocationUpdates()
    }

    private fun unsubscribeToLocationUpdates() {
        locationFlow?.cancel()
        foregroundOnlyLocationService?.unsubscribeToLocationUpdates()
    }
}