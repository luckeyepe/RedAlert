package com.example.mickey.redalert.activities.chat_activities

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.mickey.redalert.R
import com.example.mickey.redalert.models.Message
import com.example.mickey.redalert.models.User
import com.example.mickey.redalert.view_holders.ChatLogRecieveItemViewHolder
import com.example.mickey.redalert.view_holders.ChatLogSendItemViewHolder
import com.google.android.gms.tasks.Task
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.ViewHolder
import kotlinx.android.synthetic.main.activity_chat_log.*

class ChatLogActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat_log)
        val receivingUser = intent.getParcelableExtra<User>("receivingUser")
        val sendingUser = intent.getParcelableExtra<User>("sendingUser")
        supportActionBar?.title = "${receivingUser.user_firstName} ${receivingUser.user_lastName}"

        loadMessagesToRecyclerView(receivingUser, sendingUser)

        button_chatLogActivitySend.setOnClickListener {
            sendMessage(sendingUser, receivingUser)
        }
    }

    private fun loadMessagesToRecyclerView(
        receivingUser: User,
        sendingUser: User
    ) {
        val adapter = GroupAdapter<ViewHolder>()

        //database reference
        val db = FirebaseFirestore.getInstance()
            .collection("Messages").document(sendingUser.user_id!!).collection(receivingUser.user_id!!)

        db.addSnapshotListener { querySnapshot, firebaseFirestoreException ->
            if (querySnapshot != null) {
                //clears the recycler view first before adding new data
                adapter.clear()

                var messagesArray = ArrayList<Message>()
                //store the data
                for (result in querySnapshot) {
                    var document = result.toObject(Message::class.java)
                    messagesArray.add(document)
                }

                if (messagesArray.size != 0) {
                    //sort arraylist via time stamps
                    messagesArray.sortBy { selector(it) }

                    for (document in messagesArray) {
                        if (document.message_recieverID == receivingUser.user_id
                            && document.message_senderID == sendingUser.user_id
                        ) {
                            //if user sent a message
                            adapter.add(
                                ChatLogSendItemViewHolder(
                                    sendingUser.user_profilePictureURL.toString(),
                                    document.message_messageContent!!
                                )
                            )
                        } else {
                            if (document.message_recieverID == sendingUser.user_id
                                && document.message_senderID == receivingUser.user_id
                            ) {
                                //if user recieves a message
                                adapter.add(
                                    ChatLogRecieveItemViewHolder(
                                        receivingUser.user_profilePictureURL.toString(),
                                        document.message_messageContent!!
                                    )
                                )
                            }
                        }
                    }

                    //auto scroll to the last message sent/received
                    val layoutManager = LinearLayoutManager(this)
                    layoutManager.stackFromEnd = true

                    recyclerView_chatLogActivityChats.layoutManager = layoutManager
                    recyclerView_chatLogActivityChats.adapter = adapter
                }
            } else {
                Log.e("ChatLog", firebaseFirestoreException.toString())
            }
        }
    }

    private fun selector(it: Message): Long? = it.message_timeStamp

    private fun sendMessage(
        sendingUser: User,
        receivingUser: User
    ) {
        val text = editText_chatLogActivityMessage.text.toString().trim()
        if (!text.isNullOrEmpty()) {
            val message = Message()
            message.message_messageContent = text
            message.message_type = "text"
            message.message_senderID = sendingUser.user_id
            message.message_senderName = "${sendingUser.user_firstName} ${sendingUser.user_lastName}}"
            message.message_recieverID = receivingUser.user_id
            message.message_timeStamp = System.currentTimeMillis()

            val db = FirebaseFirestore.getInstance()
                .collection("Messages")
                .document(sendingUser.user_id!!)
                .collection(receivingUser.user_id!!)

            val reverseDb = FirebaseFirestore.getInstance()
                .collection("Messages")
                .document(receivingUser.user_id!!)
                .collection(sendingUser.user_id!!)

//            val latestMessage = FirebaseFirestore.getInstance()
//                .collection("Latest Massages")
//                .document(sendingUser.user_id!!)
//                .collection(receivingUser.user_id!!)
//                .document(receivingUser.user_id!!)

            val latestMessage = FirebaseFirestore.getInstance()
                .collection("Latest_Massages")
                .document("latest_messages")
                .collection(sendingUser.user_id!!)
                .document(receivingUser.user_id!!)

//            val reverseLatestMessage = FirebaseFirestore.getInstance()
//                .collection("Latest Massages")
//                .document(receivingUser.user_id!!)
//                .collection(sendingUser.user_id!!)
//                .document(sendingUser.user_id!!)

            val reverseLatestMessage = FirebaseFirestore.getInstance()
                .collection("Latest_Massages")
                .document("latest_messages")
                .collection(receivingUser.user_id!!)
                .document(sendingUser.user_id!!)

            db.add(message)
                .addOnCompleteListener { task: Task<DocumentReference> ->
                    if (task.isSuccessful) {
                        db.document(task.result!!.id).update("message_id", task.result!!.id)
                        message.message_id = task.result!!.id

                        latestMessage.set(message)
                        //clear the edit text for the next message
                        editText_chatLogActivityMessage.text = null
                    }
                }

            //create new document in firestore that saves the messages in the perspective of the receiver
            reverseDb.add(message)
                .addOnCompleteListener { task: Task<DocumentReference> ->
                    if (task.isSuccessful) {
                        reverseDb.document(task.result!!.id).update("message_id", task.result!!.id)
                        message.message_id = task.result!!.id

                        reverseLatestMessage.set(message)
                        //clear the edit text for the next message
                    }
                }
        }
    }

    private fun setupDummy(
        receivingUser: User,
        sendingUser: User
    ) {
        val adapter = GroupAdapter<ViewHolder>()
        adapter.add(ChatLogRecieveItemViewHolder(receivingUser.user_profilePictureURL.toString(), "Sup my man"))
        adapter.add(ChatLogSendItemViewHolder(sendingUser.user_profilePictureURL.toString(), "Yo my dude"))
        adapter.add(ChatLogRecieveItemViewHolder(receivingUser.user_profilePictureURL.toString(), "my man"))
        adapter.add(ChatLogSendItemViewHolder(sendingUser.user_profilePictureURL.toString(), "my dude"))
        adapter.add(ChatLogRecieveItemViewHolder(receivingUser.user_profilePictureURL.toString(), "man"))
        adapter.add(ChatLogSendItemViewHolder(sendingUser.user_profilePictureURL.toString(), "dude"))


        recyclerView_chatLogActivityChats.layoutManager = LinearLayoutManager(this)
        recyclerView_chatLogActivityChats.adapter = adapter
    }
}
