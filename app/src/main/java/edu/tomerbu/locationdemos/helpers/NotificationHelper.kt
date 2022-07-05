package edu.tomerbu.locationdemos.helpers

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.location.Location
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import java.util.*
import android.media.AudioAttributes
import android.media.AudioManager.STREAM_MUSIC

import android.net.Uri
import android.text.BoringLayout
import android.util.Log
import edu.tomerbu.locationdemos.R
import edu.tomerbu.locationdemos.services.ForegroundOnlyLocationService
import edu.tomerbu.locationdemos.toText
import edu.tomerbu.locationdemos.ui.LocationActivity


class NotificationHelper(private val context: Context, private val sound: Boolean = false) {

    //uri of sound if sound = true.
    //change sound to true => delete the app and install again
    var uri: Uri =
        Uri.parse("android.resource://${context.packageName}/${R.raw.sos}")
    //SEALED class for choosing the sound uri

    //an object that sends the notifications:
    private val notificationManager: NotificationManager by lazy {
        context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    }

    fun generateNotificationAndSend(location: Location?) {
        notificationManager.notify(NOTIFICATION_ID, generateNotification(location))
    }

    /*
    * Generates a BIG_TEXT_STYLE Notification that represent latest location.
    */
    fun generateNotification(location: Location?): Notification {
        Log.d(TAG, "generateNotification()")

        val mainNotificationText =
            location?.toText() ?: context.getString(R.string.no_location_text)

        // 1. Create Notification Channel for O+ and beyond devices (26+).
        createNotificationChannel()
        val titleText = context.getString(R.string.app_name)
        // 2. Build the BIG_TEXT_STYLE.
        val bigTextStyle = NotificationCompat.BigTextStyle()
            .bigText(mainNotificationText)
            .setBigContentTitle(titleText)

        // 3. Set up main Intent/Pending Intents for notification.
        val launchActivityIntent = Intent(context, LocationActivity::class.java)

        val cancelIntent = Intent(context, ForegroundOnlyLocationService::class.java)
        cancelIntent.putExtra(EXTRA_CANCEL_LOCATION_TRACKING_FROM_NOTIFICATION, true)

        val servicePendingIntent = PendingIntent.getService(
            context,
            0,
            cancelIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val activityPendingIntent = PendingIntent.getActivity(
            context, 0, launchActivityIntent, PendingIntent.FLAG_IMMUTABLE
        )

        // 4. Build and issue the notification.
        // Notification Channel Id is ignored for Android pre O (26).
        val notificationCompatBuilder =
            NotificationCompat.Builder(
                context,
                NOTIFICATION_CHANNEL_ID
            )
        if (sound) {
            notificationCompatBuilder.setSound(uri, STREAM_MUSIC)
        }
        return notificationCompatBuilder
            .setStyle(bigTextStyle)
            .setContentTitle(titleText)
            .setContentText(mainNotificationText)
            .setSmallIcon(R.mipmap.ic_launcher)
            // .setDefaults(NotificationCompat.DEFAULT_ALL)
            .setOngoing(true)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .addAction(
                R.drawable.ic_launch, context.getString(R.string.launch_activity),
                activityPendingIntent
            )
            .addAction(
                R.drawable.ic_cancel,
                context.getString(R.string.stop_location_updates_button_text),
                servicePendingIntent
            )
            .build()
    }


    //create the notification channel:
    private fun createNotificationChannel() {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val titleText = context.getString(R.string.app_name)
            val notificationChannel = NotificationChannel(
                NOTIFICATION_CHANNEL_ID, titleText,
                NotificationManager.IMPORTANCE_HIGH /*popup*/
            )

            notificationChannel.description = NOTIFICATION_CHANNEL_DESCRIPTION

            //remove the if/else to get the default sound
            if (sound) {
                val audioAttributes = AudioAttributes.Builder()
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .setUsage(AudioAttributes.USAGE_NOTIFICATION)
                    .build()
                notificationChannel.setSound(uri, audioAttributes)
            } else {
                // No sound (need to delete app, or create a new channel for silent notifications)
                // once notification channel is created - it has a sound that cannot be changed
                notificationChannel.setSound(null, null)
            }
            // Adds NotificationChannel to system. Attempting to create an
            // existing notification channel with its original values performs
            // no operation, so it's safe to perform the below sequence.
            notificationManager.createNotificationChannel(notificationChannel)
        }
    }

    companion object {
        private const val PACKAGE_NAME = "edu.tomerbu.locationdemos"
        private const val TAG = "edu.tomerbu.locationdemos.tag"

        internal const val EXTRA_CANCEL_LOCATION_TRACKING_FROM_NOTIFICATION =
            "$PACKAGE_NAME.extra.CANCEL_LOCATION_TRACKING_FROM_NOTIFICATION"

        internal const val NOTIFICATION_ID = 12345678

        private const val NOTIFICATION_CHANNEL_ID = "while_in_use_channel_01"
        private const val NOTIFICATION_CHANNEL_DESCRIPTION = "Location Channel"
    }
}