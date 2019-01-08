package com.example.mickey.redalert.activities.chat_activities

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AlertDialog
import com.example.mickey.redalert.R
import com.example.mickey.redalert.models.Message
import com.example.mickey.redalert.models.User
import com.example.mickey.redalert.view_holders.LatestMessagesViewHolder
import com.example.mickey.redalert.view_holders.UsersViewHolder
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
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

        grabLatestMessages()
    }

    private fun grabLatestMessages() {
        var adapter = GroupAdapter<ViewHolder>()

        recylerView_lastestMessagesActivityRecylerView.layoutManager =
                LinearLayoutManager(this)

        val currentUser = FirebaseAuth.getInstance().currentUser
        val db = FirebaseFirestore.getInstance()
            .collection("Latest Massages")
            .document("latest_messages")
            .collection(currentUser!!.uid)

        db.addSnapshotListener { querySnapshot, firebaseFirestoreException ->
            if (querySnapshot != null) {
                Log.d("LatestMessages:", "Data Exists")

                for (result in querySnapshot) {
                    val document = result.toObject(Message::class.java)
                    Log.d("Correspondent", document.message_senderID)
                    adapter.add(LatestMessagesViewHolder(document))
                }

                recylerView_lastestMessagesActivityRecylerView.adapter = adapter
            } else {
                Log.e("LatestMessages", firebaseFirestoreException.toString())
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
                    .collection("Users")
                    .document(FirebaseAuth.getInstance().currentUser!!.uid)
                    .get()
                    .addOnCompleteListener { task: Task<DocumentSnapshot> ->
                        if (task.isSuccessful) {
                            val document = task.result!!.toObject(User::class.java)
                            FirebaseFirestore.getInstance()
                                .collection("Users")
                                .document(messageItem.message.message_recieverID!!)
                                .get()
                                .addOnCompleteListener { task: Task<DocumentSnapshot> ->
                                    if (task.isSuccessful) {
                                        val recieverDocument = task.result!!.toObject(User::class.java)
                                        intent.putExtra("sendingUser", document)
                                        intent.putExtra("receivingUser", recieverDocument)
                                        startActivity(intent)
                                    }
                                }

                        }
                    }
            } else {
                Log.d("LatestMessages", "User UID: ${messageItem.message.message_senderID}")
                //pass on entire user object
                FirebaseFirestore.getInstance()
                    .collection("Users")
                    .document(messageItem.message.message_senderID!!)
                    .get()
                    .addOnCompleteListener { task: Task<DocumentSnapshot> ->
                        if (task.isSuccessful) {
                            val document = task.result!!.toObject(User::class.java)
                            FirebaseFirestore.getInstance()
                                .collection("Users")
                                .document(messageItem.message.message_recieverID!!)
                                .get()
                                .addOnCompleteListener { task: Task<DocumentSnapshot> ->
                                    if (task.isSuccessful) {
                                        val recieverDocument = task.result!!.toObject(User::class.java)
                                        intent.putExtra("sendingUser", recieverDocument)
                                        intent.putExtra("receivingUser", document)
                                        startActivity(intent)
                                    }
                                }

                        }
                    }

            }

        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_latest_messages, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when (item?.itemId){
            R.id.menuItem_menuLatestMessagesCreateMessage ->{
                val currentUser = FirebaseAuth.getInstance().currentUser
                val userDB = FirebaseFirestore.getInstance()
                    .collection("Users")
                    .document(currentUser!!.uid)

                userDB.addSnapshotListener { documentSnapshot, firebaseFirestoreException ->
                    if(documentSnapshot!= null){
                        var user = documentSnapshot.toObject(User::class.java)
                        Log.d("isERu:", user!!.user_isEru.toString())
                        if (user!!.user_isEru!!) {
                            startActivity(Intent(this, NewMessagesActivity::class.java))
                        }else{
                            errorDialog("You don't have access to this service", "RESTRICTED CONTENT")
                        }
                    }else{
                        Log.d("LatestMessages", firebaseFirestoreException.toString())
                    }
                }

            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun errorDialog(message: String, title: String) {
        var alertDialog = AlertDialog.Builder(this)
        alertDialog.setIcon(R.drawable.ic_error_black_24dp)
        alertDialog.setMessage(message)
        alertDialog.setTitle(title)
        alertDialog.setCancelable(false)
        alertDialog.setPositiveButton("OK") { dialog, which ->
            dialog.dismiss()
        }
        alertDialog.show()
    }
}
