package com.usbapps.misfortunewheel.custom

import android.annotation.SuppressLint
import android.app.ActivityManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.NotificationManager.IMPORTANCE_DEFAULT
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.CountDownTimer
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.misfortuneapp.wheelofmisfortune.R
import com.usbapps.misfortunewheel.model.TimeDatabase
import com.usbapps.misfortunewheel.view.NotificationHandler
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * Služba reprezentující odpočet času.
 */
class BroadcastService : Service() {

    /**
     * Konstanty pro ID kanálu, akci vysílání a extra úkol ID.
     */
    companion object {
        /**
         * Identifikátor kanálu pro oznámení.
         */
        const val CHANNEL_ID: String = "WheelOfMisfortune_notification_channel"

        /**
         * Akce vysílání pro odpočet času.
         */
        const val COUNTDOWN_BR: String =
            "com.misfortuneapp.wheelofmisfortune.custom.countdown_broadcast"

        /**
         * Extra klíč pro předání ID úkolu.
         */
        const val EXTRA_TIME_ID: String = "extra_wheel_of_misfortune_task_id"
    }


    // Proměnné pro CountDownTimer a Intent
    private lateinit var br: CountDownTimer
    private lateinit var bi: Intent

    // Kontext a ID úkolu
    private lateinit var context: Context
    private var timeId: Int = -1

    // Variable to track the currently running timer ID
    private var currentRunningTimerId: Int? = null

    /**
     * Metoda volaná při vytvoření služby.
     */
    @SuppressLint("ForegroundServiceType")
    override fun onCreate() {
        super.onCreate()

        // Inicializace Intentu, kontextu a vytvoření kanálu oznámení
        bi = Intent(COUNTDOWN_BR)
        context = this
        createNotificationChannel()

        // Intent pro spuštění aplikace po kliknutí na notifikaci
        val notificationIntent = packageManager.getLaunchIntentForPackage(packageName)
        val contentIntent = PendingIntent.getActivity(
            this, 0,
            notificationIntent, PendingIntent.FLAG_IMMUTABLE
        )

        // Vytvoření a spuštění předního oznámení s PendingIntent
        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle(getString(R.string.app_name))
            .setContentText(getString(R.string.countdown_start))
            .setSmallIcon(R.drawable.medal)
            .setContentIntent(contentIntent)  // Přidání PendingIntent pro akci kliknutí
            .build()

        startForeground(321, notification)
    }

    /**
     * Metoda volaná při spuštění služby.
     */
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)

        // Získání ID úkolu z intentu
        timeId = intent?.getIntExtra(EXTRA_TIME_ID, -1) ?: -1

        // Check if a timer is already running
        if (currentRunningTimerId != null) {
            // Stop the existing timer
            stopTimer(currentRunningTimerId!!)
        }

        // Pokud je poskytnuto platné ID úkolu, spusťte časovač; jinak zastavte službu
        if (timeId != -1) {
            startTimer(timeId)
            // Set the currently running timer ID
            currentRunningTimerId = timeId
        } else {
            stopForeground(STOP_FOREGROUND_REMOVE)
            stopSelf()
        }

        return START_STICKY
    }

    /**
     * Funkce pro spuštění odpočítávání.
     */
    @OptIn(DelicateCoroutinesApi::class)
    private fun startTimer(timeId: Int) {
        // Přístup k databázi úkolů
        val timeDatabase = TimeDatabase.getDatabase(context)

        // Inicializace CountDownTimeru
        br = object : CountDownTimer(Long.MAX_VALUE, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                GlobalScope.launch(Dispatchers.Main) {
                    // Získání zbývajícího času úkolu z databáze
                    val remainingTime = withContext(Dispatchers.IO) {
                        timeDatabase.timeRecordDao().getTimeById(timeId)?.endTime?.let {
                            it - System.currentTimeMillis()
                        }
                    }

                    // Pokud zbývá čas, odešlete vysílání s informacemi o odpočítávání
                    if (remainingTime != null && remainingTime > 0) {
                        bi.putExtra("countdown", remainingTime)
                        bi.putExtra("countdownTimerRunning", true)
                        bi.putExtra("countdownTimerFinished", false)
                        sendBroadcast(bi)
                    } else {
                        // Pokud zbývající čas je 0 nebo záporný, spusťte onFinish pro zastavení časovače
                        onFinish()
                    }
                }
            }

            /**
             * Voláno po skončení časovače.
             */
            override fun onFinish() {
                GlobalScope.launch(Dispatchers.Main) {
                    // Odešlete vysílání oznamující, že časovač skončil
                    bi.putExtra("countdownTimerFinished", true)
                    sendBroadcast(bi)

                    // Zavolejte notifikaci pomocí NotificationHandler
                    if (!isAppOnForeground(context)) {
                        val notificationHandler = NotificationHandler(context)
                        notificationHandler.showNotification()
                    }

                    // Zastavte přední službu a samotnou službu
                    stopForeground(STOP_FOREGROUND_REMOVE)
                    stopSelf()
                    onDestroy()
                }
            }
        }

        // Spuštění odpočítávacího časovače
        br.start()
    }

    /**
     * Funkce na zastavení časovače.
     */
    private fun stopTimer(timerId: Int) {
        if (::br.isInitialized) {
            br.cancel()
        }

        // Clear the currently running timer ID if it matches the stopped timer
        if (currentRunningTimerId == timerId) {
            currentRunningTimerId = null
        }
    }

    /**
     * Voláno pro zrušení služby.
     */
    override fun onDestroy() {
        // Pokud je časovač inicializován, zruší se
        if (::br.isInitialized) {
            br.cancel()
        }
        // Clear the currently running timer ID
        currentRunningTimerId = null
        // Zastavte přední službu a volání nadřazené onDestroy
        stopForeground(STOP_FOREGROUND_REMOVE)
        super.onDestroy()
    }

    /**
     * Funkce pro vytvoření kanálu oznámení.
     */
    private fun createNotificationChannel() {
        val name = "WheelOfMisfortuneService"
        val descriptionText = "Kanál pro foreground službu Wheel Of Misfortune"

        // Vytvoření kanálu oznámení s výchozí důležitostí
        val channel = NotificationChannel(CHANNEL_ID, name, IMPORTANCE_DEFAULT).apply {
            description = descriptionText
        }

        // Získání správce oznámení a vytvoření kanálu
        val notificationManager =
            getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(channel)
    }

    /**
     * Kontrola, zda aplikace běží na popředí (aby se nevyhazovaly zbytečně notifikace).
     */
    internal fun isAppOnForeground(context: Context): Boolean {
        val activityManager =
            context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        val appProcesses = activityManager.runningAppProcesses ?: return false
        val packageName = context.packageName
        for (appProcess in appProcesses) {
            if (appProcess.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND && appProcess.processName == packageName) {
                cancelAllNotifications()
                return true
            }
        }
        return false
    }

    /**
     * Funkce na smazání starých oznámení.
     */
    private fun cancelAllNotifications() {
        val notificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.cancelAll()
    }

    /**
     * Voláno při propojení služby s aktivitou.
     */
    override fun onBind(intent: Intent?): IBinder? {
        return null // Zatím nedefinováno
    }
}
