package edu.tomerbu.locationdemos.work

import android.content.Context
import android.util.Log
import androidx.hilt.work.HiltWorker
import androidx.work.*
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.*
import okhttp3.*
import ru.gildor.coroutines.okhttp.await
import java.io.*
import java.util.*

@HiltWorker
class DemoWorker @AssistedInject constructor(
    @Assisted val appContext: Context,
    @Assisted params: WorkerParameters
) :
    CoroutineWorker(appContext, params) {
    var writer: BufferedWriter? = null
    var call: Call? = null
    override suspend fun doWork(): Result {
        try {//fix 4 a resource failed to close when the job is forced stopped
        Log.d(TAG, "doWork: HEREHERE")
        val url = "https://worldtimeapi.org/api/timezone/Asia/Jerusalem"
        val client = OkHttpClient.Builder().build()
        val request: Request = Request.Builder()
            .url(url)
            .build()
        call = client.newCall(request)
        val result = call?.await()
            val writeResult = runCatching {
                val file = File.createTempFile("abc", ".txt", applicationContext.filesDir)

                writer = BufferedWriter(FileWriter(file, true))
                writer?.write(result?.body?.string() ?: "Nothing")
                writer?.newLine()
                writer?.write("${Date()}")
                writer?.newLine()
                writer?.close()
            }

            return if (writeResult.isSuccess)
                Result.success()
            else {
                val e = writeResult.exceptionOrNull()
                var msg = e?.message
                if (msg == null && e != null) {
                    msg = e::class.java.name
                }
                Log.d(TAG, "doWork: $msg")
                Result.retry()
            }
        } finally {
            kotlin.runCatching {
                writer?.close()
                call?.cancel()
            }
        }
    }

    override suspend fun getForegroundInfo(): ForegroundInfo {
        return super.getForegroundInfo()
    }

    companion object {
        internal const val TIMES = "Times"
        internal const val DEFAULT_TIMES = 99_999
        internal var RESULT_DEMO_WORKER = "${DemoWorker::class.java.`package`?.name}.DemoWorker"
        internal var UNIQUE_ID = "${DemoWorker::class.java.`package`?.name}.DemoWorkerUniqueId"
        private var TAG = "${DemoWorker::class.java.`package`?.name}.WorkManager"
    }
}
