package com.example.mickey.redalert.activities

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.mickey.redalert.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {
    private var mAuth: FirebaseAuth?=null
    private var user: FirebaseUser?= null
    private var mAuthListner: FirebaseAuth.AuthStateListener ?= null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        //check if user is already logged in
        mAuth = FirebaseAuth.getInstance()
        mAuthListner = FirebaseAuth.AuthStateListener {
                firebaseAuth: FirebaseAuth ->
            user = firebaseAuth.currentUser
            if (user!=null){
                //go to dashboard
                startActivity(Intent(this, DashboardActivityJava::class.java))
                finish()
            }
        }

        btn_mainLogIn.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
        }

        btn_mainSignUp.setOnClickListener {
            startActivity(Intent(this, SignupActivity::class.java))
        }
    }

    //starts the check
    override fun onStart() {
        super.onStart()
        mAuth!!.addAuthStateListener(mAuthListner!!)
    }

    //saves ram
    override fun onStop() {
        super.onStop()
        if (mAuthListner != null){
            mAuth!!.removeAuthStateListener(mAuthListner!!)
        }
    }
}
