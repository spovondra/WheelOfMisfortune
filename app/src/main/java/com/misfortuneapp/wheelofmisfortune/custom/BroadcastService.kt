// BroadcastService.kt
package com.misfortuneapp.wheelofmisfortune.custom

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.os.CountDownTimer
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.misfortuneapp.wheelofmisfortune.R
import com.misfortuneapp.wheelofmisfortune.model.TaskDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class BroadcastService : Service() {

    companion object {
        const val CHANNEL_ID = "WheelOfMisfortune_id"
        const val COUNTDOWN_BR = "com.misfortuneapp.wheelofmisfortune.custom.countdown_br"
        const val EXTRA_TASK_ID = "extra_wheel_id"
    }

    private val TAG = "BroadcastService"
    private lateinit var br: CountDownTimer
    private lateinit var bi: Intent
    private lateinit var context: Context
    private var taskId: Int = -1

    override fun onCreate() {
        super.onCreate()
        Log.i(TAG, "Creating service...")
        bi = Intent(COUNTDOWN_BR)
        context = this

        createNotificationChannel()

        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Foreground Service")
            .setContentText("Countdown Timer is running.")
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .build()

        startForeground(321, notification)

        Log.i(TAG, "Starting timer...")
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)
        taskId = intent?.getIntExtra(EXTRA_TASK_ID, -1) ?: -1

        if (taskId != -1) {
            startTimer(taskId)
        } else {
            Log.e(TAG, "Task ID not provided. Service will be stopped.")
            stopForeground(true)
            stopSelf()
        }

        return START_STICKY
    }

    private fun startTimer(taskId: Int) {
        val taskDatabase = TaskDatabase.getDatabase(context)

        br = object : CountDownTimer(Long.MAX_VALUE, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                GlobalScope.launch(Dispatchers.Main) {
                    val remainingTime = withContext(Dispatchers.IO) {
                        taskDatabase.taskDao().getTaskById(taskId)?.endTime?.minus(System.currentTimeMillis())
                    }
                    if (remainingTime != null && remainingTime > 0) {
                        bi.putExtra("countdown", remainingTime)
                        bi.putExtra("countdownTimerRunning", true)
                        bi.putExtra("countdownTimerFinished", false)
                        sendBroadcast(bi)

                        val (hours, minutes, seconds) = calculateRemainingTime(remainingTime)
                        Log.d(TAG, "Remaining Time: days, $hours hours, $minutes minutes, $seconds seconds")
                    }
                }
            }

            override fun onFinish() {
                GlobalScope.launch(Dispatchers.Main) {
                    bi.putExtra("countdownTimerFinished", true)
                    sendBroadcast(bi)
                    Log.d(TAG, "Timer finished")
                    stopForeground(true)
                    stopSelf()
                }
            }
        }

        br.start()
    }

    override fun onDestroy() {
        if (::br.isInitialized) {
            br.cancel()
            Log.i(TAG, "Timer canceled")
        }
        stopForeground(true)
        super.onDestroy()
    }


    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "ForegroundServiceChannel"
            val descriptionText = "Foreground Service Channel for Countdown Timer"
            val importance = NotificationManagerCompat.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                description = descriptionText
            }
            val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    private fun calculateRemainingTime(remainingTimeMillis: Long): Triple<Long, Long, Long> {
        val remainingSeconds = remainingTimeMillis / 1000
        val remainingMinutes = remainingSeconds / 60
        val remainingHours = remainingMinutes / 60
        val remainingDays = remainingHours / 24
        return Triple(remainingHours % 24, remainingMinutes % 60, remainingSeconds % 60)
    }
}
