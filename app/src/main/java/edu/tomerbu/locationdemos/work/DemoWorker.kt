package edu.tomerbu.locationdemos.work

import android.content.Context
import android.util.Log
import androidx.work.Worker
import androidx.work.WorkerParameters

class DemoWorker(context: Context,workerParams: WorkerParameters) :
    Worker(context, workerParams) {

    override fun doWork(): Result {
        for (i in 0..99999) {
            Log.i(TAG, "doWork: $i")
        }
        return Result.success()
    }

    companion object {
        private  var TAG = "${DemoWorker::class.java.`package`?.name}.WorkManager"
    }
}