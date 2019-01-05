package com.example.mickey.redalert.custom_classes

import android.util.Log
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class MyFirebaseMessagingService: FirebaseMessagingService() {
    override fun onNewToken(token: String?) {
//        super.onNewToken(p0)
        Log.d("Firebase Token", token)
        sendTokenToFirestore(token)
    }

    private fun sendTokenToFirestore(token: String?) {


        val currentUser = FirebaseAuth.getInstance().currentUser

        if (currentUser!=null) {
            //only emergency responce units have ERU in their display name
            if (currentUser.displayName!!.contains("ERU")) {
                val db = FirebaseFirestore.getInstance().collection("Eru").document(currentUser.uid)
                db.update("eru_token", token)
            } else {
                val db = FirebaseFirestore.getInstance().collection("Client").document(currentUser.uid)
                db.update("user_token", token)
            }
        }
    }

    override fun onMessageReceived(message: RemoteMessage?) {
//        super.onMessageReceived(p0)
        if (message!!.data.isEmpty()){
            Log.d("Firebase Token", "Data: "+message.data.toString())
//            Toast.makeText(applicationContext, message.data.toString(), Toast.LENGTH_LONG).show()
        }

        if (message?.notification != null){
            Log.d("Firebase Token", "Data: "+message.notification!!.body.toString())
        }
    }
}