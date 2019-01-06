package com.example.mickey.redalert.activities.chat_activities

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import android.view.Menu
import android.view.MenuItem
import com.example.mickey.redalert.R
import com.example.mickey.redalert.models.Message
import com.example.mickey.redalert.models.User
import com.example.mickey.redalert.view_holders.LatestMessagesViewHolder
import com.example.mickey.redalert.view_holders.UsersViewHolder
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QuerySnapshot
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.ViewHolder
import kotlinx.android.synthetic.main.activity_latest_messages.*

class LatestMessagesActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_latest_messages)

        var adapter = GroupAdapter<ViewHolder>()

        recylerView_lastestMessagesActivityRecylerView.layoutManager =
                LinearLayoutManager(this)
        ////////////////////
        val currentUser = FirebaseAuth.getInstance().currentUser
        val db = FirebaseFirestore.getInstance().collection("Messages")

        db.addSnapshotListener { querySnapshot, firebaseFirestoreException ->
            if (querySnapshot!=null) {
                //clears the recycler view first before adding new data
                adapter.clear()
                var messagesArray = ArrayList<Message>()
                for (result in querySnapshot) {
                    val document = result.toObject(Message::class.java)
                    messagesArray.add(document)
                }

                if (messagesArray.size != 0) {
                    //removes irrelevant users and messages
                    var trimmedArrayList = ArrayList<Message>()

                    for (i in 0 until messagesArray.size) {
                        Log.d("Index at", i.toString())
                        if (messagesArray[i].message_senderID == currentUser!!.uid
                            || messagesArray[i].message_recieverID == currentUser.uid
                        ) {
                            trimmedArrayList.add(messagesArray[i])
                        }
                    }

                    for (i in 0 until trimmedArrayList.size) {
                    Log.d("TrimmedArrayContents", trimmedArrayList[i].message_messageContent)
                    }

                    var uniqueUserArray = ArrayList<Message>()
                    //removes irrelevant users and messages
                    for (i in 0 until trimmedArrayList.size) {
                        if (uniqueUserArray.size != 0) {
                            for (j in 0 until uniqueUserArray.size) {
                                if ((uniqueUserArray[j].message_recieverID == trimmedArrayList[i].message_recieverID
                                            || uniqueUserArray[j].message_senderID == trimmedArrayList[i].message_recieverID)
                                    && (uniqueUserArray[j].message_recieverID == trimmedArrayList[i].message_senderID
                                            || uniqueUserArray[j].message_senderID ==trimmedArrayList[i].message_senderID)
                                    && uniqueUserArray[j].message_timeStamp!! <= trimmedArrayList[i].message_timeStamp!!)
                                {
                                    Log.d("trimmed array timestamp", trimmedArrayList[i].message_timeStamp.toString())
                                    Log.d("unique array timestamp", uniqueUserArray[j].message_timeStamp.toString())
                                    Log.d("trimmed array message", trimmedArrayList[i].message_messageContent.toString())
                                    Log.d("unique array message", uniqueUserArray[j].message_messageContent.toString())

                                    uniqueUserArray[j] = trimmedArrayList[i]
                                }else{
                                    if (uniqueUserArray[j].message_timeStamp!! > trimmedArrayList[i].message_timeStamp!!){
                                        Log.d("trimmed array timestamp", trimmedArrayList[i].message_timeStamp.toString())
                                        Log.d("unique array timestamp", uniqueUserArray[j].message_timeStamp.toString())
                                        Log.d("trimmed array message", trimmedArrayList[i].message_messageContent.toString())
                                        Log.d("unique array message", uniqueUserArray[j].message_messageContent.toString())
                                    }else {
                                        uniqueUserArray.add(trimmedArrayList[i])
                                    }
                                }
                            }
                        }else{
                            uniqueUserArray.add(messagesArray[i])
                        }
                    }

                    for (i in 0 until uniqueUserArray.size) {
                        Log.d("UniqueArrayContents", uniqueUserArray[i].message_messageContent)
                    }

                    for (message in uniqueUserArray) {
                        adapter.add(LatestMessagesViewHolder(message))
                    }

                    uniqueUserArray.clear()
                    recylerView_lastestMessagesActivityRecylerView.adapter = adapter
                }
            }else{
                Log.e("LastestMessages", firebaseFirestoreException.toString())
            }
        }

        //launch chatlog activity
        adapter.setOnItemClickListener { item, view ->
            val messageItem = item as LatestMessagesViewHolder
            val intent = Intent(view.context, ChatLogActivity::class.java)

            if (messageItem.message.message_senderID == currentUser!!.uid) {
                Log.d("LatestMessages", "User UID: ${messageItem.message.message_recieverID}")
                //pass on entire user object
                FirebaseFirestore.getInstance()
                    .collection("Client")
                    .document(FirebaseAuth.getInstance().currentUser!!.uid)
                    .get()
                    .addOnCompleteListener { task: Task<DocumentSnapshot> ->
                        if (task.isSuccessful) {
                            val document = task.result!!.toObject(com.example.mickey.redalert.models.User::class.java)
                            FirebaseFirestore.getInstance()
                                .collection("Client")
                                .document(messageItem.message.message_recieverID!!)
                                .get()
                                .addOnCompleteListener {
                                    task: Task<DocumentSnapshot> ->
                                    if (task.isSuccessful){
                                        val recieverDocument = task.result!!.toObject(com.example.mickey.redalert.models.User::class.java)
                                        intent.putExtra("sendingUser", document)
                                        intent.putExtra("receivingUser", recieverDocument)
                                        startActivity(intent)
                                    }
                                }

                        }
                    }
                }else{
                    Log.d("LatestMessages", "User UID: ${messageItem.message.message_senderID}")
                    //pass on entire user object
                    FirebaseFirestore.getInstance()
                        .collection("Client")
                        .document(messageItem.message.message_senderID!!)
                        .get()
                        .addOnCompleteListener { task: Task<DocumentSnapshot> ->
                            if (task.isSuccessful) {
                                val document = task.result!!.toObject(com.example.mickey.redalert.models.User::class.java)
                                FirebaseFirestore.getInstance()
                                    .collection("Client")
                                    .document(messageItem.message.message_recieverID!!)
                                    .get()
                                    .addOnCompleteListener {
                                            task: Task<DocumentSnapshot> ->
                                        if (task.isSuccessful){
                                            val recieverDocument = task.result!!.toObject(com.example.mickey.redalert.models.User::class.java)
                                            intent.putExtra("sendingUser", recieverDocument)
                                            intent.putExtra("receivingUser", document)
                                            startActivity(intent)
                                        }
                                    }

                            }
                        }

            }

        }
        ////////////////////

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
