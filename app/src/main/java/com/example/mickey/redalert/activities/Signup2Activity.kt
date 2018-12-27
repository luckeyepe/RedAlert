package com.example.mickey.redalert.activities

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.example.mickey.redalert.R
import com.example.mickey.redalert.models.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import kotlinx.android.synthetic.main.activity_signup2.*

class Signup2Activity : AppCompatActivity() {

    private var mCurrentUser: FirebaseUser?= null
    private var mDatabase: DatabaseReference?= null
    private var mAuth: FirebaseAuth?=null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_signup2)
        mAuth = FirebaseAuth.getInstance()
        mCurrentUser = FirebaseAuth.getInstance().currentUser

        btn_signupNext2.setOnClickListener {
            var user = User()
            user.user_id = mCurrentUser!!.uid
            user.user_bloodType = edt_signup2BloodType.text.toString().trim()
            var allergyString = edt_signup2Alergies.text.toString().trim()

            if (rg_signup2Gender.checkedRadioButtonId != -1 && rg_signup2OrganDonor.checkedRadioButtonId != -1 && !user.user_bloodType.isNullOrEmpty() && !allergyString.isNullOrEmpty()) {
                if (rg_signup2Gender.checkedRadioButtonId == R.id.rb_signup2Male){
                    user.user_gender = "M"
                }else{
                    user.user_gender = "F"
                }

                user.user_isOrganDonor = rg_signup2OrganDonor.checkedRadioButtonId == R.id.rb_signup2Yes

                addAdditionalInfo(user, allergyString)
            }else{
                Toast.makeText(this, "Please fill out all the fields", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun addAdditionalInfo(user: User, allergyString: String) {
        mDatabase = FirebaseDatabase.getInstance().reference.child("Users").child(user.user_id!!)
        var userAddedDetails = HashMap<String, Any>()
        userAddedDetails["user_blood_type"] = user.user_bloodType!!
        userAddedDetails["user_gender"] = user.user_gender!!
        userAddedDetails["user_is_organ_donor"] = user.user_isOrganDonor!!

        mDatabase!!.updateChildren(userAddedDetails).addOnSuccessListener {
            //split allergy into array
            var allergyArray = allergyString.split(", ")
            //add allergy to firebase
            mDatabase = FirebaseDatabase.getInstance().reference.child("Allergies").child(user.user_id!!)
            mDatabase!!.setValue(allergyArray).addOnSuccessListener {
                var signup3 = Intent(this, Signup3Activity::class.java)
                startActivity(signup3)
            }
        }
    }

    override fun onStart() {
        super.onStart()
        var currentUser = mAuth!!.currentUser
    }
}
