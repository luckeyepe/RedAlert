package com.example.mickey.redalert.activities

import android.app.Dialog
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import android.util.Log
import android.view.LayoutInflater
import android.view.Window
import android.widget.Toast
import com.example.mickey.redalert.R
import com.example.mickey.redalert.models.User
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.activity_signup2.*
import kotlinx.android.synthetic.main.popup_blood_type.*
import kotlinx.android.synthetic.main.popup_blood_type.view.*

class Signup2Activity : AppCompatActivity() {
    val TAG = "Signup2Activity"
    private var mCurrentUser: FirebaseUser?= null
    private var mDatabase: DocumentReference?= null
    private var mAuth: FirebaseAuth?=null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_signup2)
        edt_signup2BloodType.isFocusable = false
        edt_signup2BloodType.isClickable = true

        mAuth = FirebaseAuth.getInstance()
        mCurrentUser = FirebaseAuth.getInstance().currentUser

        edt_signup2BloodType.setOnClickListener {
            var dialog: Dialog?
            var popupView = LayoutInflater.from(this).inflate(R.layout.popup_blood_type, null)

            var popupRadioGroupPositive = popupView.radioGroup_popupBloodTypeGroupPositive
            var popupRadioGroupNegative = popupView.radioGroup_popupBloodTypeGroupNegative
            var popupRadioButtonAPositive = popupView.radioButton_popupBloodTypeAPositive
            var popupRadioButtonBPositive = popupView.radioButton_popupBloodTypeBPositive
            var popupRadioButtonABPositive = popupView.radioButton_popupBloodTypeABPositive
            var popupRadioButtonOPositive = popupView.radioButton_popupBloodTypeOPositive
            var popupRadioButtonANegative = popupView.radioButton_popupBloodTypeANegative
            var popupRadioButtonBNegative = popupView.radioButton_popupBloodTypeBNegative
            var popupRadioButtonABNegative = popupView.radioButton_popupBloodTypeABNegative
            var popupRadioButtonONegative = popupView.radioButton_popupBloodTypeONegative
            var popupButtonOK = popupView.button_popupBloodTypeOk

            //clean all checks
            popupRadioGroupPositive.clearCheck()
            popupRadioGroupNegative.clearCheck()

            //change listener
            popupRadioGroupPositive.setOnCheckedChangeListener { group, checkedId ->
                if (checkedId != -1){
                    popupRadioGroupNegative.setOnCheckedChangeListener(null)
                    popupRadioGroupNegative.clearCheck()

                    when(popupRadioGroupPositive.checkedRadioButtonId){
                        popupRadioButtonAPositive.id->{
                            edt_signup2BloodType.setText("A+")
                        }

                        popupRadioButtonBPositive.id ->{
                            edt_signup2BloodType.setText("B+")
                        }

                        popupRadioButtonABPositive.id ->{
                            edt_signup2BloodType.setText("AB+")
                        }

                        popupRadioButtonOPositive.id ->{
                            edt_signup2BloodType.setText("O+")
                        }
                    }
                }
            }

            popupRadioGroupNegative.setOnCheckedChangeListener { group, checkedId ->
                if (checkedId != -1){
                    popupRadioGroupPositive.clearCheck()
                    popupRadioGroupPositive.setOnCheckedChangeListener(null)

                    when(popupRadioGroupNegative.checkedRadioButtonId){
                        popupRadioButtonANegative.id->{
                            edt_signup2BloodType.setText("A-")
                        }

                        popupRadioButtonBNegative.id ->{
                            edt_signup2BloodType.setText("B-")
                        }

                        popupRadioButtonABNegative.id ->{
                            edt_signup2BloodType.setText("AB-")
                        }

                        popupRadioButtonONegative.id ->{
                            edt_signup2BloodType.setText("O-")
                        }
                    }
                }
            }

            dialog = Dialog(this)
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
            dialog.setContentView(popupView)
            dialog.setCancelable(false)
            dialog.show()

            popupButtonOK.setOnClickListener {
                dialog.dismiss()
            }
        }


        btn_signupNext2.setOnClickListener {
            var user = User()
            Toast.makeText(this, "Hello there", Toast.LENGTH_SHORT).show()
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
        mDatabase = FirebaseFirestore.getInstance().collection("Users").document(user.user_id!!)
        var userAddedDetails = HashMap<String, Any>()
        userAddedDetails["user_bloodType"] = user.user_bloodType!!
        userAddedDetails["user_gender"] = user.user_gender!!
        userAddedDetails["user_isOrganDonor"] = user.user_isOrganDonor!!
        userAddedDetails["user_allergies"] = allergyString.split(", ")
        userAddedDetails["user_isEru"] = false

        mDatabase!!.update(userAddedDetails)
            .addOnCompleteListener {
                task: Task<Void> ->
                if (task.isSuccessful){
                    var signup3 = Intent(this, Signup3Activity::class.java)
                    startActivity(signup3)
                }else{
                    Log.e("$TAG Error", task.exception.toString())
                    var alertDialog = AlertDialog.Builder(this)
                    alertDialog.setMessage("An error occurred during sign up.\n Please try again later")
                    alertDialog.setTitle("SIGN UP ERROR")
                    alertDialog.show()
                }
            }
//        mDatabase!!.updateChildren(userAddedDetails).addOnSuccessListener {
//            //split allergy into array
//
//            //add allergy to firebase
//            mDatabase = FirebaseDatabase.getInstance().reference.child("Allergies").child(user.user_id!!)
//            mDatabase!!.setValue(allergyArray).addOnSuccessListener {
//                var signup3 = Intent(this, Signup3Activity::class.java)
//                startActivity(signup3)
//            }
//        }
    }

    override fun onStart() {
        super.onStart()
        var currentUser = mAuth!!.currentUser
    }
}
