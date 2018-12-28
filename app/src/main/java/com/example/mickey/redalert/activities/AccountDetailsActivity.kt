package com.example.mickey.redalert.activities

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.widget.ArrayAdapter
import com.example.mickey.redalert.R
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.activity_account_details.*
import java.text.SimpleDateFormat

class AccountDetailsActivity : AppCompatActivity() {
    private val TAG = "AccountDetailsActivity"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_account_details)

        //grab data
        var currentUser = FirebaseAuth.getInstance().currentUser
        var database = FirebaseFirestore.getInstance().collection("Client").document(currentUser!!.uid)

        database.get()
            .addOnCompleteListener {
                task: Task<DocumentSnapshot> ->
                if (task.isSuccessful){
                    val user = task.result!!.toObject(com.example.mickey.redalert.models.User::class.java)

                    if (user!=null) {
                        //occupy textviews
                        textView_accountDetailsBloodType.text = user.user_bloodType

                        if (user.user_gender == "M") {
                            textView_accountDetailsGender.text = "Male"
                        } else {
                            textView_accountDetailsGender.text = "Female"
                        }

                        if (user.user_isOrganDonor == true){
                            textView_accountDetailsIsOrganDonor.text = "Yes"
                        }else{
                            textView_accountDetailsIsOrganDonor.text = "No"
                        }

                        textView_accountDetailsFullName.text = "${user.user_lastName}, ${user.user_firstName}"
                        textView_accountDetailsAddress.text = user.user_address
                        textView_accountDetailsContactNumber.text = user.user_contactNumber.toString()
                        var dateOfBirth = user.user_birthDate
                        val formatter = SimpleDateFormat("MM/dd/yyyy")
                        textView_accountDetailsDateOfBirth.text = formatter.format(dateOfBirth)

                        //put emergency contacts into list view
                        val arrayList = user.user_emergencyContacts

                        if (arrayList != null) {
                            val adapter = ArrayAdapter<Long>(
                                this,
                                R.layout.row_simple_text,
                                R.id.textView_rowSimpleTextText,
                                arrayList
                            )
                            listView_accountDetailsEmergencyContacts.adapter = adapter
                        }

                        //put allergies into list view
                        val arrayListAllergies = user.user_allergies

                        if (arrayListAllergies != null) {
                            val adapter = ArrayAdapter<String>(
                                this,
                                R.layout.row_simple_text,
                                R.id.textView_rowSimpleTextText,
                                arrayListAllergies
                            )
                            listView_accountDetailsAllergies.adapter = adapter
                        }

                        //load up user profile picture
                        Picasso.get().load(user.user_profilePictureURL).into(circleImageView_acountDetailsProfilePicture)

                    }

                }else{
                    //todo popup error that no data exists
                }
            }
    }
}
