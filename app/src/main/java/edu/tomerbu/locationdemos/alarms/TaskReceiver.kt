package edu.tomerbu.locationdemos.alarms

import android.app.AlarmManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import dagger.hilt.android.AndroidEntryPoint
import edu.tomerbu.locationdemos.alarms.TaskNotificationScheduler.Companion.TAG
import edu.tomerbu.locationdemos.helpers.NotificationHelper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

/**
 * [BroadcastReceiver] to be notified by the [android.app.AlarmManager].
 */
@AndroidEntryPoint
class TaskReceiver : BroadcastReceiver() {
    @Inject
    lateinit var coroutineScope: CoroutineScope
    var context: Context? = null

    override fun onReceive(context: Context?, intent: Intent?) {
        Log.d(TAG, "onReceive() - intent ${intent?.action}")
        this.context = context
        coroutineScope.launch {
            handleIntent(intent)
        }
    }

    private suspend fun handleIntent(intent: Intent?) {
        when (intent?.action) {
            ALARM_ACTION -> getTaskId(intent)?.let {
                //TODO: mark it as complete in db
                context?.let {
                    NotificationHelper(it).generateNotificationAndSend(text = "Alarm")
                }
            }
            Intent.ACTION_BOOT_COMPLETED, AlarmManager.ACTION_SCHEDULE_EXACT_ALARM_PERMISSION_STATE_CHANGED -> {
                /*TODO : reschedule all alarms*/
            }
            else -> Log.e(TAG, "Action not supported")
        }
    }

    private fun getTaskId(intent: Intent?) = intent?.getLongExtra(EXTRA_TASK, 0)

    companion object {
        const val EXTRA_TASK = "extra_task"
        const val ALARM_ACTION = "edu.tomerbu.alarms.SET_ALARM"
    }
}