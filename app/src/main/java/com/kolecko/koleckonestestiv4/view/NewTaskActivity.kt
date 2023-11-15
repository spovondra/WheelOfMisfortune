// NewTaskActivity.kt
package com.kolecko.koleckonestestiv4

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity

class NewTaskActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_new_task)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.elevation = 0f

        // You can add your NewTaskActivity-specific code here
    }
    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }
}

