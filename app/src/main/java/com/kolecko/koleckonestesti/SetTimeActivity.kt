package com.kolecko.koleckonestesti

import android.app.AlertDialog
import android.app.Dialog
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.TimePicker

// Aktivita pro nastavení času
class SetTimeActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_set_time)

        // Nalezení tlačítka pro uložení
        val saveButton: Button = findViewById(R.id.saveButton)

        // Nastavení posluchače kliknutí na tlačítko
        saveButton.setOnClickListener {
            // Vytvoření a zobrazení dialogu s TimePickerem
            val dialog = createDialog()
            dialog.show()
        }
    }

    // Metoda pro vytvoření dialogu s TimePickerem
    private fun createDialog(): Dialog {
        // Nafukování layoutu dialogu
        val dialogView = layoutInflater.inflate(R.layout.activity_set_time, null)

        // Nalezení TimePickeru a tlačítka pro uložení v layoutu dialogu
        val timePicker: TimePicker = dialogView.findViewById(R.id.timePicker)
        val saveButton: Button = dialogView.findViewById(R.id.saveButton)

        // Nastavení TimePickeru na 24hodinový režim
        timePicker.setIs24HourView(true)

        // Vytvoření AlertDialogu s nastavením layoutu
        val dialog = AlertDialog.Builder(this)
            .setView(dialogView)
            .create()

        // Nastavení posluchače kliknutí na tlačítko pro uložení
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
