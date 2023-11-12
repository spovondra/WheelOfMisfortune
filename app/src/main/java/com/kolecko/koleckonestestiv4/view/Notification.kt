package com.kolecko.koleckonestestiv4

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.TaskStackBuilder
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.core.app.NotificationCompat

interface Notification {
    fun showNotification()
    fun createNotificationChannel(notificationManager: NotificationManager)
}

class NotificationHandler(private val context: Context) : Notification {

    override fun showNotification() {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        createNotificationChannel(notificationManager)

        // Intent for launching MainViewImp
        val intent = Intent(context, MainViewImp::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK

        // Create a TaskStackBuilder
        val stackBuilder = TaskStackBuilder.create(context)

        // Add the main activity to the back stack
        stackBuilder.addParentStack(MainViewImp::class.java)

        // Add the intent to the back stack
        stackBuilder.addNextIntent(intent)

        // Get the PendingIntent from the TaskStackBuilder
        val pendingIntent = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_IMMUTABLE)

        // Build the notification
        val notificationBuilder = NotificationCompat.Builder(context, "info_channel")
            .setSmallIcon(R.drawable.medal_2441759)
            .setContentTitle("Čas na zatočení si s kolečkem!")
            .setContentText("Kolo neštěstí na Tebe už čeká.")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)

        // Send the notification
        notificationManager.notify(1, notificationBuilder.build())
    }

    override fun createNotificationChannel(notificationManager: NotificationManager) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                "info_channel",
                "Upozornit na zatočení si kolečkem",
                NotificationManager.IMPORTANCE_HIGH
            )
            notificationManager.createNotificationChannel(channel)
        }
    }
}
