package com.example.candlescanner

import android.os.Bundle
import android.widget.Button
import android.widget.SeekBar
import androidx.appcompat.app.AppCompatActivity

class SettingsActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(androidx.appcompat.R.layout.abc_action_bar_title_item)
        // In a full app, add a proper settings layout. For this prototype, we keep settings in SharedPreferences via simple UI.
    }
}
