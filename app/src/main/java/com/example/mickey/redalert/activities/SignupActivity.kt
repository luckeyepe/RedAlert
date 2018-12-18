package com.example.mickey.redalert.activities

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.text.TextUtils.isEmpty
import android.widget.Toast
import com.example.mickey.redalert.R
import com.example.mickey.redalert.models.User
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import kotlinx.android.synthetic.main.activity_signup.*

class SignupActivity : AppCompatActivity() {

    private var mAuth: FirebaseAuth?= null
    private var mDatabase: DatabaseReference?= null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_signup)

        mAuth = FirebaseAuth.getInstance()

        btn_signupNext.setOnClickListener {
            var user = User()
            user.user_email = edt_signupEmail.text.toString().trim()
            user.user_first_name = edt_signupFirstName.text.toString().trim()
            user.user_last_name = edt_signupLastName.text.toString().trim()
            user.user_address = edt_signupAddress.text.toString().trim()
            user.user_birth_date = edt_signupBirthDate.text.toString().trim()
            user.user_contact_no = edt_signupContactNumber.text.toString().trim()
            user.user_profPic = "default"
            var password = edt_signupPassword.text.toString().trim()

            if ( user.user_email.isNullOrEmpty() &&  user.user_first_name.isNullOrEmpty() &&  user.user_last_name.isNullOrEmpty() && user.user_address.isNullOrEmpty()
            && user.user_birth_date.isNullOrEmpty() && password.isNullOrEmpty()){
                Toast.makeText(this, "Please fill out all the fields", Toast.LENGTH_LONG).show()
            }
            else {
                createAccount(user, password)
            }
        }
    }

    private fun createAccount(user: User, password: String) {
        mAuth!!.createUserWithEmailAndPassword(user.user_email!!, password)
            .addOnCompleteListener{
                    task: Task<AuthResult> ->
                if (task.isSuccessful){
                    var currentUserId = mAuth!!.currentUser
                    var userId = currentUserId!!.uid
                    mDatabase = FirebaseDatabase.getInstance().reference.child("Users").child(userId)

                    mDatabase!!.setValue(user).addOnCompleteListener {
                            task: Task<Void> ->
                        if (task.isSuccessful){
                            Toast.makeText(this, "User Created", Toast.LENGTH_LONG).show()

                            var signup2 = Intent(this, Signup2Activity::class.java)
                            startActivity(signup2)
                            finish()
                        }
                        else{
                            Toast.makeText(this, "User not Created", Toast.LENGTH_LONG).show()
                        }
                    }
                }
                else{
                    Toast.makeText(this, "User already Created", Toast.LENGTH_LONG).show()
                }
            }
    }
}
