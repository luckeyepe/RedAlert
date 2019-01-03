package com.example.mickey.redalert.activities.chat_activities

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import com.example.mickey.redalert.R
import kotlinx.android.synthetic.main.activity_latest_messages.*

class LatestMessagesActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_latest_messages)

        recylerView_lastestMessagesActivityRecylerView.adapter
        recylerView_lastestMessagesActivityRecylerView.layoutManager = LinearLayoutManager(this)
    }
}
