package com.example.mickey.redalert.activities

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import com.example.mickey.redalert.R
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import kotlinx.android.synthetic.main.activity_login.*

class LoginActivity : AppCompatActivity() {

    private var mAuth: FirebaseAuth?= null
    private var mDatabase: DatabaseReference?= null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        mAuth = FirebaseAuth.getInstance()

        btn_loginLogin.setOnClickListener{
            var email = edt_loginEmail.text.toString().trim()
            var password = edt_loginPassword.text.toString().trim()

            if (email.isEmpty() && password.isEmpty()){

            }
            else{
                loginUser(email, password)
            }
        }
    }

    private fun loginUser(email: String, password: String) {

        mAuth!!.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener {
                    task: Task<AuthResult> ->
                if (task.isSuccessful){
                    var dashboardIntent = Intent(this, DashboardActivityJava::class.java)
                    startActivity(dashboardIntent)
                    finish()
                }else{
                    Log.d("LogIn", "Email: $email, Password: $password")
                    Toast.makeText(this, "User does not exist", Toast.LENGTH_LONG).show()
                }
            }
    }
}
