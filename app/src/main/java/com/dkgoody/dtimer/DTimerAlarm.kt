package com.dkgoody.dtimer

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.content.ContextCompat

// Control Alarms -
class DTimerAlarm {

    companion object {

        fun send(context: Context, wakeup: Long) {
            request(
                context,
                wakeup,
                false
            )
        }

        fun cancel(context: Context) {
            request(context, 0, true)
        }

        private fun request(context: Context, wakeup: Long, cancel : Boolean) {
            val pintent = PendingIntent.getBroadcast(
                context,
                1,
                Intent(context, DTimerBroadcastReceiver::class.java)
                    .putExtra(context.getString(R.string.action_id), DTimerNotifications.Action.Alarm),
                PendingIntent.FLAG_IMMUTABLE
            )

            with(ContextCompat.getSystemService(context, AlarmManager::class.java) as AlarmManager) {
                when (cancel) {
                    true -> cancel(pintent)
                    else -> setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, wakeup, pintent)
                }
            }
        }
    } // companion object
}