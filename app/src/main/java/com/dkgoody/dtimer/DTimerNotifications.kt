package com.dkgoody.dtimer

import android.app.*
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.navigation.NavDeepLinkBuilder

import com.dkgoody.dtimer.ui.main.MainActivity


class DTimerNotifications {

    class RequestCode {
        companion object {
            val OneAndOnly = 1
        }
    }

    class Action {
        companion object {
            val Default = 0
            val Next = 128
            val Stop = 129
            val Alarm = 130
        }
    }

    companion object {

        fun createNotificationChannels(context: Context) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val AttentionChannel = NotificationChannel(
                    context.getString(R.string.attention_channel_id),
                    context.getString(R.string.attention_channel_desc),
                    NotificationManager.IMPORTANCE_HIGH
                )
                val BackgroundChannel = NotificationChannel(
                    context.getString(R.string.background_channel_id),
                    context.getString(R.string.background_channel_desc),
                    NotificationManager.IMPORTANCE_LOW
                )
                var notificationManager =
                    context.getSystemService(AppCompatActivity.NOTIFICATION_SERVICE) as NotificationManager
                notificationManager.createNotificationChannel(AttentionChannel)
                notificationManager.createNotificationChannel(BackgroundChannel)
            }
        }


        fun getCycleColor(cycle : Int) : Int  {
            return when (cycle) {
                0 -> R.color.colorTimer0
                else -> R.color.colorTimer1
            }
        }

        fun getCycleDestination(cycle : Int) : Int  {
            return  when (cycle) {
                0 -> R.id.DTimerRunningFragment0
                else -> R.id.DTimerRunningFragment1
            }
        }

        fun getRunningIntent(context: Context, cycle: Int): PendingIntent {
            return NavDeepLinkBuilder(context)
                .setComponentName(MainActivity::class.java)
                .setGraph(R.navigation.navigation)
                .setDestination(getCycleDestination(cycle))
                .createPendingIntent()
        }

        // Used for the first and continues updates of running message
        fun getRunningNotification(context: Context, cycle : Int, title : String, millis : Long) : Notification? {

            return  NotificationCompat.Builder(
                context,
                context.getString(R.string.background_channel_id))
                .setSmallIcon(R.drawable.ic_notify_foreground)
                .setContentTitle(title)
                .setContentText(context.getString(R.string.in_progress))
                .setColor(context.getColor(getCycleColor(cycle)))
                .setWhen(System.currentTimeMillis() + millis)
                .setUsesChronometer(true)
                .setTimeoutAfter(millis)
                .setContentIntent(getRunningIntent(context, cycle))
                .setAutoCancel(true)
                .build()
        }
        
        fun sendRunningNotification(context: Context, cycle : Int, millis : Long, text : String) {
            with(NotificationManagerCompat.from(context)) {
                notify(RequestCode.OneAndOnly,  getRunningNotification(context, cycle, text, millis)!!)
            }
        }

        fun sendDoneNotification(context: Context, cycle: Int, title : String) {
            val action_start: NotificationCompat.Action = NotificationCompat.Action.Builder(
                R.drawable.ic_notify_foreground,
                context.getString(R.string.dialog_next),
                PendingIntent.getBroadcast(
                    context,
                    2,
                    Intent(context, DTimerBroadcastReceiver::class.java)
                        .putExtra(context.getString(R.string.action_id), Action.Next),
                    PendingIntent.FLAG_CANCEL_CURRENT
                )
            ).build()

            val action_stop: NotificationCompat.Action = NotificationCompat.Action.Builder(
                R.drawable.ic_notify_foreground,
                context.getString(R.string.dialog_stop),
                PendingIntent.getBroadcast(
                    context,
                    3,
                    Intent(context, DTimerBroadcastReceiver::class.java)
                        .putExtra(context.getString(R.string.action_id), Action.Stop),
                    PendingIntent.FLAG_CANCEL_CURRENT
                )
            ).build()

            val notification = NotificationCompat.Builder(context,
                context.getString(R.string.attention_channel_id))
                .setSmallIcon(R.drawable.ic_notify_foreground)
                .setContentTitle(title)
                .setContentText(context.getString(R.string.is_over))
                .setColor(context.getColor(getCycleColor(cycle)))
                .setContentIntent(getRunningIntent(context, cycle))
                .setAutoCancel(true)
                .addAction(action_start)
                .addAction(action_stop)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setDefaults(Notification.DEFAULT_SOUND)
                .build()

            with(NotificationManagerCompat.from(context)) {
                notify(RequestCode.OneAndOnly, notification)
            }
        }

        fun sendPauseNotification(context: Context, cycle: Int, title : String) {

            val notification = NotificationCompat.Builder(context,
                context.getString(R.string.background_channel_id))
                .setSmallIcon(R.drawable.ic_notify_foreground)
                .setContentTitle(title)
                .setContentText(context.getString(R.string.is_paused))
                .setColor(context.getColor(getCycleColor(cycle)))
                .setWhen(System.currentTimeMillis())
                .setUsesChronometer(true)
                .setContentIntent(getRunningIntent(context, cycle))
                .setAutoCancel(true)
                .build()

            with(NotificationManagerCompat.from(context)) {
                notify(RequestCode.OneAndOnly, notification)
            }
        }
    }
}