package com.usbapps.misfortunewheel.view

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.TaskStackBuilder
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import com.usbapps.misfortunewheel.R

/**
 * Rozhraní pro zobrazení notifikace. Definuje metody pro zobrazení notifikace a vytvoření notifikačního kanálu.
 */
interface Notification {
    /**
     * Metoda pro zobrazení notifikace.
     */
    fun showNotification()

    /**
     * Metoda pro vytvoření notifikačního kanálu.
     *
     * @param notificationManager Instance `NotificationManager` pro správu notifikací.
     */
    fun createNotificationChannel(notificationManager: NotificationManager)
}

/**
 * Implementace rozhraní `Notification` pro zobrazování notifikací.
 *
 * @property context Kontext aktivity nebo aplikace.
 * @constructor Inicializuje třídu `NotificationHandler` s daným kontextem.
 */
class NotificationHandler(private val context: Context) : Notification {

    /**
     * Metoda pro zobrazení notifikace. Vytvoří notifikační kanál, intent pro spuštění hlavní aktivity
     * a odesílá notifikaci s odpovídajícím obsahem.
     */
    override fun showNotification() {
        // Získání systémové služby pro správu notifikací
        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

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
            .setSmallIcon(R.drawable.medal)
            .setContentTitle(context.getString(R.string.time_to_spin))
            .setContentText(context.getString(R.string.wheel_waiting))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)

        // Odeslání notifikace
        notificationManager.notify(1, notificationBuilder.build())
    }

    /**
     * Metoda pro vytvoření notifikačního kanálu. Vytvoří notifikační kanál s definovanými vlastnostmi a přidá ho do systému.
     *
     * @param notificationManager Instance `NotificationManager` pro správu notifikací.
     */
    override fun createNotificationChannel(notificationManager: NotificationManager) {
        // Podmínka pro kontrolu verze Androidu
        // Vytvoření notifikačního kanálu
        val channel = NotificationChannel(
            "info_channel",
            "Wheel Spin",
            NotificationManager.IMPORTANCE_HIGH
        )
        // Přidání kanálu do systému
        notificationManager.createNotificationChannel(channel)
    }
}
