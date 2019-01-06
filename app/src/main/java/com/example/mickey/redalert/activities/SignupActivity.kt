package com.example.mickey.redalert.activities

import android.app.DatePickerDialog
import android.app.ProgressDialog
import android.content.Intent
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import android.text.TextUtils.isEmpty
import android.util.Log
import android.widget.Toast
import com.example.mickey.redalert.R
import com.example.mickey.redalert.models.User
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.activity_signup.*
import java.util.*
import java.sql.Timestamp
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.text.DateFormat
import android.widget.DatePicker
import com.google.firebase.auth.UserProfileChangeRequest

class SignupActivity : AppCompatActivity() {
    val TAG = "SignupActivity"
    private var mAuth: FirebaseAuth?= null
    private var mDatabase: DocumentReference?=null

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_signup)
        edt_signupBirthDate.isFocusable = false//disables user from writing directly
        edt_signupBirthDate.isClickable = true

        mAuth = FirebaseAuth.getInstance()

        edt_signupBirthDate.setOnClickListener {
            popupDateTimePicker()
        }

        btn_signupNext.setOnClickListener {
            if (isCompleteData()){
                var user = User()
                user.user_email = edt_signupEmail.text.toString().trim()
                user.user_firstName = edt_signupFirstName.text.toString().trim()
                user.user_lastName = edt_signupLastName.text.toString().trim()
                user.user_address = edt_signupAddress.text.toString().trim()

                val str_date = edt_signupBirthDate.text.toString().trim()
                val formatter: DateFormat
                val date: Date
                formatter = SimpleDateFormat("MM/dd/yyyy")
                date = formatter.parse(str_date)
                val timeStampDate = Timestamp(date.time)


                user.user_birthDate = timeStampDate
                user.user_contactNumber = edt_signupContactNumber.text.toString().trim()
                user.user_profilePictureURL = "default"
                var password = edt_signupPassword.text.toString().trim()

                if(isValidPassword(password)) {
                    createAccount(user, password)
                }
            }
            else {
                var alertDialog = AlertDialog.Builder(this)
                alertDialog.setMessage("Please fill out all the fields")
                alertDialog.setTitle("EMPTY FIELDS")
                alertDialog.show()
            }
        }
    }

    private fun isCompleteData() =
        (!edt_signupEmail.text.toString().trim().isNullOrEmpty() && !edt_signupFirstName.text.toString().trim().isNullOrEmpty()
                && !edt_signupLastName.text.toString().trim().isNullOrEmpty() && !edt_signupAddress.text.toString().trim().isNullOrEmpty()
                && !edt_signupBirthDate.text.toString().trim().isNullOrEmpty() && !edt_signupPassword.text.toString().trim().isNullOrEmpty())

    private fun createAccount(user: User, password: String) {
        val progress = ProgressDialog(this)
        progress.setTitle("Loading")
        progress.setMessage("Please wait while loading...")
        progress.show()

        mAuth!!.createUserWithEmailAndPassword(user.user_email!!, password)
            .addOnCompleteListener{
                    task: Task<AuthResult> ->
                if (task.isSuccessful){
                    var currentUser = mAuth!!.currentUser
                    Log.d(TAG, "the current user id is ${currentUser!!.uid}")
                    user.user_id = currentUser.uid

                    mDatabase = FirebaseFirestore.getInstance().collection("Client")
                        .document(currentUser!!.uid)
                    mDatabase!!.set(user)
                        .addOnCompleteListener {
                            task: Task<Void> ->
                            if (task.isSuccessful){
                                val profileUpdates = UserProfileChangeRequest.Builder()
                                    .setDisplayName("${user.user_firstName} ${user.user_lastName}")
                                    .build()

                                if (currentUser != null) {
                                    currentUser.updateProfile(profileUpdates)
                                        ?.addOnCompleteListener { task ->
                                            if (task.isSuccessful) {
                                                Log.d(TAG, "User profile updated.")
                                            }
                                        }
                                }

                                var alertDialog = AlertDialog.Builder(this)
                                alertDialog.setMessage("Success")
                                alertDialog.setTitle("SIGN UP SUCCESS")
                                alertDialog.show()
                                var signup2 = Intent(this, Signup2Activity::class.java)
                                startActivity(signup2)
                                progress.dismiss()
                                finish()
                            }else{
                                Log.e("$TAG Error", task.exception.toString())
                                var alertDialog = AlertDialog.Builder(this)
                                alertDialog.setMessage("An error occurred during sign up.\n Please try again later")
                                alertDialog.setTitle("SIGN UP ERROR")
                                alertDialog.show()
                                progress.dismiss()
                            }
                        }
                }
                else{
                    Toast.makeText(this, "User already Created", Toast.LENGTH_LONG).show()
                }
            }
    }

    private fun isValidPassword(password: String): Boolean {

        if(isStringContainNumber(password)){
            if(isStringContainUpperCase(password)){
                if(isStringContainLowerCase(password)){
                    if(isStringContainSpecialCharacter(password)){
                        if(password.length >=8){
                            return true
                        }
                    }
                }
            }
        }
        return false
    }

    private fun isStringContainSpecialCharacter(string: String): Boolean {
        for(c in string.toCharArray()){
            if (!c.isLetterOrDigit())
                return true
        }

        var alertDialog = AlertDialog.Builder(this)
        alertDialog.setMessage("Password must contain at least one (1) special character")
        alertDialog.setTitle("INCORRECT PASSWORD")
        alertDialog.show()

        return false
    }

    private fun isStringContainLowerCase(string: String): Boolean {
        for(c in string.toCharArray()){
            if (c.isLowerCase())
                return true
        }

        var alertDialog = AlertDialog.Builder(this)
        alertDialog.setMessage("Password must contain at least one (1) lower case character")
        alertDialog.setTitle("INCORRECT PASSWORD")
        alertDialog.show()
        return false
    }

    private fun isStringContainUpperCase(string: String): Boolean {
        for(c in string.toCharArray()){
            if (c.isUpperCase())
                return true
        }
        var alertDialog = AlertDialog.Builder(this)
        alertDialog.setMessage("Password must contain at least one (1) upper case character")
        alertDialog.setTitle("INCORRECT PASSWORD")
        alertDialog.show()

        return false
    }

    private fun isStringContainNumber(string: String): Boolean {

        for(c in string.toCharArray()){
            if (c.isDigit())
                return true
        }

        var alertDialog = AlertDialog.Builder(this)
        alertDialog.setMessage("Password must contain at least one (1) digit")
        alertDialog.setTitle("INCORRECT PASSWORD")
        alertDialog.show()

        return false
    }

    private fun popupDateTimePicker() {
        val c = Calendar.getInstance()
        val year = c.get(Calendar.YEAR)
        val month = c.get(Calendar.MONTH)
        val day = c.get(Calendar.DAY_OF_MONTH)

        val dpd = DatePickerDialog(this, DatePickerDialog.OnDateSetListener { view, year, monthOfYear, dayOfMonth ->
            // Display Selected date in textbox
            var date = "${monthOfYear+1}/$dayOfMonth/$year"
            edt_signupBirthDate.setText(date)
        }, year, month, day)
        dpd.show()
    }
}
