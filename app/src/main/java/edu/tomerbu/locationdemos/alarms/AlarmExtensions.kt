package edu.tomerbu.locationdemos.alarms

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.util.Log
import androidx.core.app.AlarmManagerCompat
import edu.tomerbu.locationdemos.alarms.TaskNotificationScheduler.Companion.TAG
import java.util.*

/**
 * Gets the [AlarmManager] system service.
 *
 * @return the [AlarmManager] system service
 */
fun Context.getAlarmManager() =
    getSystemService(Context.ALARM_SERVICE) as? AlarmManager

/**
 * Sets a alarm using [AlarmManagerCompat] to be triggered based on the given parameter.
 *
 * **This function can only be called if the permission `SCHEDULE_EXACT_ALARM` is granted to
 * the application.**
 *
 * @see [android.Manifest.permission.SCHEDULE_EXACT_ALARM]
 *
 * @param triggerAtMillis time in milliseconds that the alarm should go off, using the
 * appropriate clock (depending on the alarm type).
 * @param operation action to perform when the alarm goes off
 * @param type type to define how the alarm will behave
 */
fun Context.setExactAlarm(
    triggerAtMillis: Long,
    operation: PendingIntent?,
    type: Int = AlarmManager.RTC_WAKEUP
) {
    val currentTime = Calendar.getInstance().timeInMillis
    if (triggerAtMillis <= currentTime) {
        Log.w(TAG, "It is not possible to set alarm in the past")
        return
    }

    if (operation == null) {
        Log.e(TAG, "PendingIntent is null")
        return
    }

    val manager = getAlarmManager()
    manager?.let {
        AlarmManagerCompat
            .setExactAndAllowWhileIdle(it, type, triggerAtMillis, operation)
    }
}

/**
 * Cancels a alarm set on [AlarmManager], based on the given [PendingIntent].
 *
 * @param operation action to be canceled
 */
fun Context.cancelAlarm(operation: PendingIntent?) {
    if (operation == null) {
        Log.e(TAG, "PendingIntent is null")
        return
    }

    val manager = getAlarmManager()
    manager?.let { manager.cancel(operation) }
}

