package com.kolecko.koleckonestesti

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.TaskStackBuilder
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat

// Rozhraní pro zobrazení notifikace
interface Notification {
    fun showNotification()
    fun createNotificationChannel(notificationManager: NotificationManager)
}

// Implementace rozhraní Notification pro zobrazování notifikací
class NotificationHandler(private val context: Context) : Notification {

    // Metoda pro zobrazení notifikace
    override fun showNotification() {
        // Získání systémové služby pro správu notifikací
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // Vytvoření notifikačního kanálu
        createNotificationChannel(notificationManager)

        // Intent pro spuštění MainViewImp
        val intent = Intent(context, MainViewImp::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK

        // Vytvoření TaskStackBuilder
        val stackBuilder = TaskStackBuilder.create(context)

        // Přidání hlavní aktivity na zásobník zpět
        stackBuilder.addParentStack(MainViewImp::class.java)

        // Přidání intentu na zásobník zpět
        stackBuilder.addNextIntent(intent)

        // Získání PendingIntent z TaskStackBuilder
        val pendingIntent = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_IMMUTABLE)

        // Vytvoření notifikace
        val notificationBuilder = NotificationCompat.Builder(context, "info_channel")
            .setSmallIcon(R.drawable.medal_2441759)
            .setContentTitle("Čas na zatočení si s kolečkem!")
            .setContentText("Kolo neštěstí na Tebe už čeká.")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)

        // Odeslání notifikace
        notificationManager.notify(1, notificationBuilder.build())
    }

    // Metoda pro vytvoření notifikačního kanálu
    override fun createNotificationChannel(notificationManager: NotificationManager) {
        // Podmínka pro kontrolu verze Androidu
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // Vytvoření notifikačního kanálu
            val channel = NotificationChannel(
                "info_channel",
                "Upozornit na zatočení si kolečkem",
                NotificationManager.IMPORTANCE_HIGH
            )
            // Přidání kanálu do systému
            notificationManager.createNotificationChannel(channel)
        }
    }
}
