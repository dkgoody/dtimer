package com.dkgoody.dtimer

import android.app.*
import android.content.Context
import android.content.Intent
import android.os.IBinder
import androidx.core.content.ContextCompat


// Foreground(sic!) Service (is must for advanced android versions)
// Sends out notifications and sets alarm
// Reads/Writes persistant state object

class DTimerService : Service() {

    companion object {

        fun run(context: Context) {
            DTimerNotifications.createNotificationChannels(context)

            var ps = DTimerPersistentState(context, null)

            val startIntent = Intent(context, DTimerService::class.java)
            startIntent.putExtra("cycle", ps.cycle())
            startIntent.putExtra("millis", ps.freeze())
            startIntent.putExtra("text", ps.timer_title(ps.cycle()))
            ContextCompat.startForegroundService(context, startIntent)
            DTimerAlarm.send(context, ps.wakeup())
        }

        // Switch to App
        fun cancel(context: Context) {
            DTimerAlarm.cancel(context)
            val stopIntent = Intent(context, DTimerService::class.java)
            context.stopService(stopIntent)
        }

        // Quit or Put App to Idle
        fun stop(context: Context) {
            val stopIntent = Intent(context, DTimerService::class.java)
            context.stopService(stopIntent)
            var ps = DTimerPersistentState(context, null)
            ps.save(ps.cycle(), 0, 0, DTimerState.PAUSED.ordinal)
        }

        // Start new cycle
        fun next (context: Context) {

            var ps = DTimerPersistentState(context, null)

            val next_cycle = ps.next_cycle(ps.cycle())
            val millis = ps.timer_value_ms(next_cycle)
            val wakeUpTime = System.currentTimeMillis() + millis
            ps.save(next_cycle, 0L, wakeUpTime, DTimerState.RUNNING.ordinal)
            DTimerAlarm.send(context, wakeUpTime)
            DTimerNotifications.sendRunningNotification(context, next_cycle, millis, ps.timer_title(next_cycle))
        }

        fun done(context: Context) {
            var ps = DTimerPersistentState(context, null)

            DTimerNotifications.sendDoneNotification(
                    context,
                    ps.cycle(),
                    ps.timer_title(ps.cycle())
                )
                ps.save(ps.cycle(), 0L, 0, DTimerState.RUNNING.ordinal)
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {

        val cycle = intent?.getIntExtra("cycle", 0)
        val millis = intent?.getLongExtra("millis", 0L)
        val text = intent?.getStringExtra("text")

        startForeground(1,
            DTimerNotifications.getRunningNotification(this, cycle!!, text!!, millis!!))

        return START_STICKY
    }

    override fun onBind(intent: Intent): IBinder? {
        return null
    }
}