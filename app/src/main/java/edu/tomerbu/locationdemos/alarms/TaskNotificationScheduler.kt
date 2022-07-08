package edu.tomerbu.locationdemos.alarms

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.util.Log
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

/**
 * Alarm manager to schedule a event based on the due date from a Task.
 */

class TaskNotificationScheduler @Inject constructor(@ApplicationContext private val context: Context) {
    /**
     * Schedules a task notification based on the due date.
     *
     * @param taskId task id to be scheduled
     * @param timeInMillis the time to the alarm be scheduled
     */
    fun scheduleTaskAlarm(taskId: Long, timeInMillis: Long) {
        //TODO: save the alarm to db so we can reschedule it after boot complete
        val receiverIntent = Intent(context, TaskReceiver::class.java).apply {
            action = TaskReceiver.ALARM_ACTION
            putExtra(TaskReceiver.EXTRA_TASK, taskId)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            taskId.toInt(),
            receiverIntent,
            PendingIntent.FLAG_CANCEL_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        Log.d(TAG, "Scheduling notification for '$taskId' at '$timeInMillis'")
        context.setExactAlarm(timeInMillis, pendingIntent)
    }

    /**
     * Cancels a task notification based on the task id.
     *
     * @param taskId task id to be canceled
     */
    fun cancelTaskAlarm(taskId: Long) {
        val receiverIntent = Intent(context, TaskReceiver::class.java)
        receiverIntent.action = TaskReceiver.ALARM_ACTION

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            taskId.toInt(),
            receiverIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        Log.d(TAG, "Canceling notification with id '$taskId'")
        context.cancelAlarm(pendingIntent)
    }

    companion object {
        const val TAG = "edu.tomerbu.locationdemos.alarms"
    }
}