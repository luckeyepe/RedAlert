package com.example.mickey.redalert.view_holders

import com.example.mickey.redalert.R
import com.example.mickey.redalert.models.Message
import com.example.mickey.redalert.models.User
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.squareup.picasso.Picasso
import com.xwray.groupie.Item
import com.xwray.groupie.ViewHolder
import kotlinx.android.synthetic.main.row_user_with_last_message.view.*

class LatestMessagesViewHolder(val message: Message): Item<ViewHolder>() {
    override fun getLayout(): Int {
        return R.layout.row_user_with_last_message
    }

    override fun bind(viewHolder: ViewHolder, position: Int) {
        val currentUser = FirebaseAuth.getInstance().currentUser
        var profilePicture = viewHolder.itemView.circleImageView_userWithLastMessageRowProfilePicture
        var lastMessage = viewHolder.itemView.textViewuserWithLastMessageRowLastMessage
        val fullname = viewHolder.itemView.textView_userWithLastMessageRowFullName

        if (currentUser != null) {
            if (message.message_senderID != currentUser.uid){
                val userDB = FirebaseFirestore.getInstance()
                    .collection("Client")
                    .document(message.message_senderID!!)
                userDB.get()
                    .addOnCompleteListener {
                            task: Task<DocumentSnapshot> ->
                        if (task.isSuccessful){
                            val document = task.result!!.toObject(User::class.java)
                            if (document != null) {
                                Picasso.get().load(document.user_profilePictureURL).into(profilePicture)
                                fullname.text = "${document.user_firstName} ${document.user_lastName}"
                                lastMessage.text = message.message_messageContent
                            }
                        }
                    }
//                val db = FirebaseFirestore.getInstance()
//                    .collection("Messages")
//                    .document(message.message_id!!)
//                db.get().addOnCompleteListener {
//                    task: Task<DocumentSnapshot> ->
//                    if (task.isSuccessful){
//
//                    }
//                }
            }else{
                val userDB = FirebaseFirestore.getInstance()
                    .collection("Client")
                    .document(message.message_recieverID!!)
                userDB.get()
                    .addOnCompleteListener {
                            task: Task<DocumentSnapshot> ->
                        if (task.isSuccessful){
                            val document = task.result!!.toObject(User::class.java)
                            if (document != null) {
                                Picasso.get().load(document.user_profilePictureURL).into(profilePicture)
                                fullname.text = "${document.user_firstName} ${document.user_lastName}"
                                lastMessage.text = message.message_messageContent
                            }
                        }
                    }
            }
        }
    }
}