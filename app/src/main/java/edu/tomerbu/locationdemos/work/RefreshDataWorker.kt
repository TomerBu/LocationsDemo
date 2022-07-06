package edu.tomerbu.locationdemos.work

import android.content.Context
import android.location.LocationManager
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.ForegroundInfo
import androidx.work.WorkerParameters
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import edu.tomerbu.locationdemos.location.SharedLocationManager
import edu.tomerbu.locationdemos.toText
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.lastOrNull

@HiltWorker
class RefreshDataWorker @AssistedInject constructor(
    @Assisted val appContext: Context,
    @Assisted params: WorkerParameters,
    private val locationManager: SharedLocationManager
) :
    CoroutineWorker(appContext, params) {
    @OptIn(ExperimentalCoroutinesApi::class)
    override suspend fun doWork(): Result {
        val location = locationManager.locationFlow().firstOrNull {
            println("TOMERBU $it")
            true
        }
        println(location)
        return Result.success()
    }

    override suspend fun getForegroundInfo(): ForegroundInfo {
        return super.getForegroundInfo()
    }
}