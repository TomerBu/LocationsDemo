package edu.tomerbu.locationdemos

import android.app.Application
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.GlobalScope

// Required for Hilt dependency injection
@HiltAndroidApp
class LocationApplication : Application() {
}