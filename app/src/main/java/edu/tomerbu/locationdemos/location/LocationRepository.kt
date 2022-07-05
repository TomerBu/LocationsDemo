package edu.tomerbu.locationdemos.location


import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

class LocationRepository @Inject constructor(
        private val sharedLocationManager: SharedLocationManager
) {
    /**
     * Observable flow for location updates
     */
    fun getLocations() = sharedLocationManager.locationFlow()
}