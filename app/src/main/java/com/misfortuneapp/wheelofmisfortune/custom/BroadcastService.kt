package com.misfortuneapp.wheelofmisfortune.custom

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.NotificationManager.IMPORTANCE_DEFAULT
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.CountDownTimer
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import com.misfortuneapp.wheelofmisfortune.R
import com.misfortuneapp.wheelofmisfortune.model.TaskDatabase
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

// Třída reprezentující službu pro odpočet času
class BroadcastService : Service() {

    // Konstanty pro ID kanálu, akci vysílání a extra úkol ID
    companion object {
        const val CHANNEL_ID = "WheelOfMisfortune_id"
        const val COUNTDOWN_BR = "com.misfortuneapp.wheelofmisfortune.custom.countdown_br"
        const val EXTRA_TASK_ID = "extra_wheel_id"
    }

    // Značka pro logování
    private val tag = "BroadcastService"

    // Proměnné pro CountDownTimer a Intent
    private lateinit var br: CountDownTimer
    private lateinit var bi: Intent

    // Kontext a ID úkolu
    private lateinit var context: Context
    private var taskId: Int = -1

    // Metoda volaná při vytvoření služby
    override fun onCreate() {
        super.onCreate()
        Log.i(tag, "Vytvářím službu...")

        // Inicializace Intentu, kontextu a vytvoření kanálu oznámení
        bi = Intent(COUNTDOWN_BR)
        context = this
        createNotificationChannel()

        // Vytvoření a spuštění předního oznámení
        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Wheel of Misfortune")
            .setContentText("Odpočet spuštěn.")
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .build()

        startForeground(321, notification)
    }

    // Metoda volaná při spuštění služby
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)

        // Získání ID úkolu z intentu
        taskId = intent?.getIntExtra(EXTRA_TASK_ID, -1) ?: -1

        // Pokud je poskytnuto platné ID úkolu, spusťte časovač; jinak zastavte službu
        if (taskId != -1) {
            startTimer(taskId)
        } else {
            stopForeground(STOP_FOREGROUND_REMOVE)
            stopSelf()
        }

        return START_STICKY
    }

    // Funkce pro spuštění odpočítávání
    @OptIn(DelicateCoroutinesApi::class)
    private fun startTimer(taskId: Int) {
        // Přístup k databázi úkolů
        val taskDatabase = TaskDatabase.getDatabase(context)

        // Inicializace CountDownTimeru
        br = object : CountDownTimer(Long.MAX_VALUE, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                GlobalScope.launch(Dispatchers.Main) {
                    // Získání zbývajícího času úkolu z databáze
                    val remainingTime = withContext(Dispatchers.IO) {
                        taskDatabase.taskDao().getTaskById(taskId)?.endTime?.minus(System.currentTimeMillis())
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

            // Voláno po skončení časovače
            override fun onFinish() {
                GlobalScope.launch(Dispatchers.Main) {
                    // Odešlete vysílání oznamující, že časovač skončil
                    bi.putExtra("countdownTimerFinished", true)
                    sendBroadcast(bi)
                    Log.d(tag, "Časovač skončil")

                    // Zastavte přední službu a samotnou službu
                    stopForeground(STOP_FOREGROUND_REMOVE)
                    stopSelf()
                }
            }
        }

        // Spuštění odpočítávacího časovače
        br.start()
    }

    // Voláno při zrušení služby
    override fun onDestroy() {
        // Pokud je časovač inicializován, zrušte jej
        if (::br.isInitialized) {
            br.cancel()
            Log.i(tag, "Časovač zrušen")
        }
        // Zastavte přední službu a volání nadřazené onDestroy
        stopForeground(STOP_FOREGROUND_REMOVE)
        super.onDestroy()
    }

    // Funkce pro vytvoření kanálu oznámení
    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "WheelOfMisfortuneService"
            val descriptionText = "Kanál pro foreground službu Wheel Of Misfortune"

            // Vytvoření kanálu oznámení s výchozí důležitostí
            val channel = NotificationChannel(CHANNEL_ID, name, IMPORTANCE_DEFAULT).apply {
                description = descriptionText
            }

            // Získání správce oznámení a vytvoření kanálu
            val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    // Voláno při propojění služby s aktivitou
    override fun onBind(intent: Intent?): IBinder? {
        return null // Zatím nedefinováno
    }
}