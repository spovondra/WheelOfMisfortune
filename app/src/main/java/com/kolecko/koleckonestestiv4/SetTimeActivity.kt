package com.kolecko.koleckonestestiv4

import android.app.AlertDialog
import android.app.Dialog
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.TimePicker

class SetTimeActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_set_time)

        val saveButton: Button = findViewById(R.id.saveButton)

        saveButton.setOnClickListener {
            // Create a dialog
            val dialog = createDialog()
            dialog.show()
        }
    }

    private fun createDialog(): Dialog {
        val dialogView = layoutInflater.inflate(R.layout.activity_set_time, null)
        val timePicker: TimePicker = dialogView.findViewById(R.id.timePicker)
        val saveButton: Button = dialogView.findViewById(R.id.saveButton)

        // Nastavení TimePicker na 24hodinový režim
        timePicker.setIs24HourView(true)

        val dialog = AlertDialog.Builder(this)
            .setView(dialogView)
            .create()

        saveButton.setOnClickListener {
            // Získání vybraného času z TimePickeru
            val hour = timePicker.hour
            val minute = timePicker.minute

            // Převedení vybraného času na řetězec
            val selectedTime = String.format("%02d:%02d", hour, minute)

            // Vytvoření intentu pro přenos vybraného času zpět do hlavní aktivity
            val resultIntent = Intent()
            resultIntent.putExtra("selectedTime", selectedTime)

            // Nastavení výsledku aktivity na RESULT_OK a předání intentu
            setResult(RESULT_OK, resultIntent)

            // Ukončení dialogu
            dialog.dismiss()
        }

        return dialog
    }
}
