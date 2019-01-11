package com.example.mickey.redalert.activities.chat_activities

import android.app.Activity
import android.app.ProgressDialog
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.mickey.redalert.R
import com.example.mickey.redalert.models.Message
import com.example.mickey.redalert.models.User
import com.example.mickey.redalert.view_holders.ChatLogRecieveImageItemViewHolder
import com.example.mickey.redalert.view_holders.ChatLogRecieveItemViewHolder
import com.example.mickey.redalert.view_holders.ChatLogSendImageItemViewHolder
import com.example.mickey.redalert.view_holders.ChatLogSendItemViewHolder
import com.google.android.gms.tasks.Continuation
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.UploadTask
import com.squareup.picasso.Picasso
import com.theartofdev.edmodo.cropper.CropImage
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.ViewHolder
import kotlinx.android.synthetic.main.activity_account_details.*
import kotlinx.android.synthetic.main.activity_chat_log.*
import java.util.HashMap

class ChatLogActivity : AppCompatActivity() {
    var GALLERY_ID: Int = 1

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

        imageButton_chatLogAddImage.setOnClickListener {
            uploadImage()
        }
    }

    private fun uploadImage() {
        var gallerIntent = Intent()
        gallerIntent.type = "image/*"
        gallerIntent.action = Intent.ACTION_GET_CONTENT
        startActivityForResult(Intent.createChooser(gallerIntent, "Select Image"), GALLERY_ID)
    }

    private fun loadMessagesToRecyclerView(
        receivingUser: User,
        sendingUser: User
    ) {
        val adapter = GroupAdapter<ViewHolder>()

        //database reference
        val db = FirebaseFirestore.getInstance()
            .collection("Messages")
            .document(sendingUser.user_id!!)
            .collection(receivingUser.user_id!!).orderBy("message_timeStamp", Query.Direction.ASCENDING)

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
//                    //sort arraylist via time stamps
//                    messagesArray.sortBy { selector(it) }

                    for (document in messagesArray) {
                        if (document.message_recieverID == receivingUser.user_id
                            && document.message_senderID == sendingUser.user_id
                        ) {
                            //if user sent a message
                            //check what type of message did they send
                            when(document.message_type){
                                "image" ->{
                                    adapter.add(
                                        ChatLogSendImageItemViewHolder(
                                            sendingUser.user_profilePictureURL.toString(),
                                            document.message_messageContent!!, applicationContext)
                                    )

//                                    adapter.add(
//                                        ChatLogSendImageItemViewHolder(
//                                            sendingUser.user_profilePictureURL.toString(),
//                                            document.message_messageContent!!)
//                                    )
                                }

                                "text" ->{
                                    adapter.add(
                                        ChatLogSendItemViewHolder(
                                            sendingUser.user_profilePictureURL.toString(),
                                            document.message_messageContent!!)
                                    )
                                }

                                "emergency" ->{
                                    adapter.add(
                                        ChatLogSendItemViewHolder(
                                            sendingUser.user_profilePictureURL.toString(),
                                            document.message_messageContent!!)
                                    )
                                }
                            }

                        } else {
                            //if user receives a message
                            //check what type of message did they send
                            when(document.message_type){
                                "image" ->{
                                    adapter.add(
                                        ChatLogRecieveImageItemViewHolder(
                                            receivingUser.user_profilePictureURL.toString(),
                                            document.message_messageContent!!, applicationContext)
                                    )
//                                    adapter.add(
//                                        ChatLogRecieveImageItemViewHolder(
//                                            receivingUser.user_profilePictureURL.toString(),
//                                            document.message_messageContent!!)
//                                    )

                                }

                                "text" ->{
                                    adapter.add(
                                        ChatLogRecieveItemViewHolder(
                                            receivingUser.user_profilePictureURL.toString(),
                                            document.message_messageContent!!)
                                    )
                                }

                                "emergency" ->{
                                    adapter.add(
                                        ChatLogRecieveItemViewHolder(
                                            receivingUser.user_profilePictureURL.toString(),
                                            document.message_messageContent!!)
                                    )
                                }

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
            message.message_senderName = "${sendingUser.user_firstName} ${sendingUser.user_lastName}"
            message.message_recieverID = receivingUser.user_id
            message.message_timeStamp = System.currentTimeMillis()/1000

            val db = FirebaseFirestore.getInstance()
                .collection("Messages")
                .document(sendingUser.user_id!!)
                .collection(receivingUser.user_id!!)

            val reverseDb = FirebaseFirestore.getInstance()
                .collection("Messages")
                .document(receivingUser.user_id!!)
                .collection(sendingUser.user_id!!)

            val latestMessage = FirebaseFirestore.getInstance()
                .collection("Latest_Massages")
                .document("latest_messages")
                .collection(sendingUser.user_id!!)
                .document(receivingUser.user_id!!)

            val reverseLatestMessage = FirebaseFirestore.getInstance()
                .collection("Latest_Massages")
                .document("latest_messages")
                .collection(receivingUser.user_id!!)
                .document(sendingUser.user_id!!)

            val notificationMessages = FirebaseFirestore.getInstance()
                .collection("Notification_Massages")

            db.add(message)
                .addOnCompleteListener { task: Task<DocumentReference> ->
                    if (task.isSuccessful) {
                        db.document(task.result!!.id).update("message_id", task.result!!.id)
                        message.message_id = task.result!!.id

                        latestMessage.set(message)
                        notificationMessages.document(message.message_id!!).set(message)
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

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        val progress = ProgressDialog(this)

        if (requestCode == GALLERY_ID && resultCode == Activity.RESULT_OK) {
            val imageURI: Uri = data!!.data

            CropImage.activity(imageURI).setAspectRatio(1,1).start(this)
        }

        if (CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE === requestCode) {
            val result = CropImage.getActivityResult(data)

            if (resultCode === Activity.RESULT_OK) {
                //loading start
                progress.setTitle("Loading")
                progress.setMessage("Uploading Photo...")
                progress.setCancelable(false) // disable dismiss by tapping outside of the dialog
                progress.show()

                val resultURI = result.uri
                val mStorage = FirebaseStorage.getInstance().reference

                //////////////////////////
                val receivingUser = intent.getParcelableExtra<User>("receivingUser")
                val sendingUser = intent.getParcelableExtra<User>("sendingUser")

                val message = Message()
                message.message_messageContent = ""
                message.message_type = "image"
                message.message_senderID = sendingUser.user_id
                message.message_senderName = "${sendingUser.user_firstName} ${sendingUser.user_lastName}"
                message.message_recieverID = receivingUser.user_id
                message.message_timeStamp = System.currentTimeMillis()/1000

                val db = FirebaseFirestore.getInstance()
                    .collection("Messages")
                    .document(sendingUser.user_id!!)
                    .collection(receivingUser.user_id!!)

                val reverseDb = FirebaseFirestore.getInstance()
                    .collection("Messages")
                    .document(receivingUser.user_id!!)
                    .collection(sendingUser.user_id!!)

                val latestMessage = FirebaseFirestore.getInstance()
                    .collection("Latest_Massages")
                    .document("latest_messages")
                    .collection(sendingUser.user_id!!)
                    .document(receivingUser.user_id!!)

                val reverseLatestMessage = FirebaseFirestore.getInstance()
                    .collection("Latest_Massages")
                    .document("latest_messages")
                    .collection(receivingUser.user_id!!)
                    .document(sendingUser.user_id!!)

                val notificationMessages = FirebaseFirestore.getInstance()
                    .collection("Notification_Massages")

                db.add(message)
                    .addOnCompleteListener { task: Task<DocumentReference> ->
                        if (task.isSuccessful) {
                            db.document(task.result!!.id).update("message_id", task.result!!.id)
                            message.message_id = task.result!!.id

                            var thumbnailPath = mStorage!!.child("chat_images")
                                .child("${message.message_id}.jpg")

                            var uploadTask = thumbnailPath.putFile(resultURI)

                            var urlTask = uploadTask.continueWithTask(Continuation<UploadTask.TaskSnapshot, Task<Uri>> { task ->
                                if (!task.isSuccessful) {
                                    task.exception?.let {
                                        throw it
                                    }
                                }
                                return@Continuation thumbnailPath.downloadUrl
                            }).addOnCompleteListener {
                                    task ->
                                if(task.isSuccessful){
                                    Log.d("ChatLog", "Upload Image Success")

                                    val downloadUri = task.result
                                    Log.d("ChatLog", "Download URI $downloadUri")
                                    db.document(message.message_id!!).update("message_messageContent", downloadUri.toString())

                                    message.message_messageContent = downloadUri.toString()
                                    latestMessage.set(message)
                                    notificationMessages.document(message.message_id!!).set(message)

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
                                    //clear the edit text for the next message
                                    editText_chatLogActivityMessage.text = null
                                    progress.dismiss()
                                }else{
                                    //loading end
                                    progress.dismiss()
                                    Toast.makeText(applicationContext, "Upload error", Toast.LENGTH_LONG).show()
                                }
                            }
                        }
                    }
            }
        }
    }
}
