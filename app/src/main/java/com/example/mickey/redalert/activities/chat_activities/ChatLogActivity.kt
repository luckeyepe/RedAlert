package com.example.mickey.redalert.activities.chat_activities

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.mickey.redalert.R

class ChatLogActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat_log)

        supportActionBar?.title = "Chat Log"
    }
}
