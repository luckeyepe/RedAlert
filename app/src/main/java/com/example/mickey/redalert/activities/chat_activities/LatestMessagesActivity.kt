package com.example.mickey.redalert.activities.chat_activities

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import android.view.Menu
import android.view.MenuItem
import com.example.mickey.redalert.R
import kotlinx.android.synthetic.main.activity_latest_messages.*

class LatestMessagesActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_latest_messages)

        recylerView_lastestMessagesActivityRecylerView.adapter
        recylerView_lastestMessagesActivityRecylerView.layoutManager =
                LinearLayoutManager(this)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_latest_messages, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when (item?.itemId){
            R.id.menuItem_menuLatestMessagesCreateMessage ->{
                startActivity(Intent(this, NewMessagesActivity::class.java))
            }
        }
        return super.onOptionsItemSelected(item)
    }
}
