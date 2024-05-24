package com.example.safebrowser.activity

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.safebrowser.R

class DarkCount : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dark_count)



    }
    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        super.onBackPressed()
        finish()
    }

}