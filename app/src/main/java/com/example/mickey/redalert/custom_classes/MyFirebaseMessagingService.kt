package com.example.mickey.redalert.custom_classes

import android.content.Intent
import androidx.appcompat.app.AlertDialog
import android.util.Log
import android.widget.Toast
import com.example.mickey.redalert.R
import com.example.mickey.redalert.activities.DashboardActivityJava
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
            val db = FirebaseFirestore.getInstance().collection("Users").document(currentUser.uid)
            db.update("user_token", token)
        }
    }

    override fun onMessageReceived(message: RemoteMessage?) {
//        super.onMessageReceived(p0)
        if (message?.notification != null){
            Log.d("FCM", "Data: "+message.notification!!.body.toString())
        }
    }
}