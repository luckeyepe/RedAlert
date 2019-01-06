package com.example.mickey.redalert.activities.chat_activities

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.mickey.redalert.R
import com.example.mickey.redalert.User
import com.example.mickey.redalert.view_holders.UsersViewHolder
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QuerySnapshot
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.ViewHolder
import kotlinx.android.synthetic.main.activity_new_messages.*

class NewMessagesActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_new_messages)
        supportActionBar?.title = "Select User"

        fetchAvailableUsers()
    }

    private fun fetchAvailableUsers() {
        val adapter = GroupAdapter<ViewHolder>()
        val currentUser = FirebaseAuth.getInstance().currentUser
        val db = FirebaseFirestore.getInstance().collection("Client")

        recylerView_newMessages.layoutManager = LinearLayoutManager(this)

        db.get().addOnCompleteListener {
            task: Task<QuerySnapshot> ->
            if (task.isSuccessful){
                for(document in task.result!!){
                    val user = document.toObject(com.example.mickey.redalert.models.User::class.java)
                    if (user.user_email != currentUser!!.email)
                    adapter.add(UsersViewHolder(user))
                }

                recylerView_newMessages.adapter = adapter

                //launch chatlog activity
                adapter.setOnItemClickListener { item, view ->
                    val intent = Intent(view.context, ChatLogActivity::class.java)
                    startActivity(intent)
                    finish()
                }
            }
        }
    }
}
