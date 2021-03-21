package com.dkgoody.dtimer

import android.app.*
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.dkgoody.dtimer.DTimerNotifications
import com.dkgoody.dtimer.DTimerService
import com.dkgoody.dtimer.R

// receives notifications issues by the DTimerServer
class DTimerBroadcastReceiver: BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {

        val notificationManager =
            context.getSystemService(AppCompatActivity.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.cancelAll()

        val notificationId = intent.getIntExtra(context.getString(R.string.action_id),
            DTimerNotifications.Action.Default
        )

        Log.i("DTimer", "received " + notificationId)

        when (notificationId) {

            DTimerNotifications.Action.Alarm -> {
                DTimerService.done(context)
            }
            DTimerNotifications.Action.Stop -> {
                DTimerService.stop(context)
            }
            DTimerNotifications.Action.Next -> {
                DTimerService.next(context)
            }
        }
    }
}