package com.example.mickey.redalert.activities.chat_activities

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.LinearLayout
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.mickey.redalert.R
import com.example.mickey.redalert.models.User
import com.example.mickey.redalert.view_holders.ChatLogRecieveItemViewHolder
import com.example.mickey.redalert.view_holders.ChatLogSendItemViewHolder
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.ViewHolder
import kotlinx.android.synthetic.main.activity_chat_log.*

class ChatLogActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat_log)
        val receivingUser = intent.getParcelableExtra<User>("receivingUser")
        supportActionBar?.title = "${receivingUser.user_firstName} ${receivingUser.user_lastName}"
//        supportActionBar!!.title = receivingUser.user_id

        val adapter = GroupAdapter<ViewHolder>()
        adapter.add(ChatLogRecieveItemViewHolder())
        adapter.add(ChatLogSendItemViewHolder())
        adapter.add(ChatLogSendItemViewHolder())
        adapter.add(ChatLogSendItemViewHolder())
        adapter.add(ChatLogRecieveItemViewHolder())
        adapter.add(ChatLogSendItemViewHolder())
        adapter.add(ChatLogSendItemViewHolder())
        adapter.add(ChatLogRecieveItemViewHolder())
        adapter.add(ChatLogSendItemViewHolder())
        adapter.add(ChatLogSendItemViewHolder())
        adapter.add(ChatLogRecieveItemViewHolder())
        adapter.add(ChatLogSendItemViewHolder())

        recyclerView_chatLogActivityChats.layoutManager = LinearLayoutManager(this)
        recyclerView_chatLogActivityChats.adapter = adapter
    }
}
