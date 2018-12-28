package com.example.mickey.redalert.activities

import android.app.AlertDialog
import android.app.Notification
import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import com.example.mickey.redalert.R
import com.example.mickey.redalert.models.Notifications
import com.example.mickey.redalert.models.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.*
import kotlinx.android.synthetic.main.activity_dashboard.*

class DashboardActivity : AppCompatActivity() {
    var alertDialog: AlertDialog.Builder ?= null
    private var mCurrentUser: FirebaseUser?= null
    private var mDatabase: DatabaseReference?= null
    private var mAuth: FirebaseAuth?=null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dashboard)
        mAuth = FirebaseAuth.getInstance()
        mCurrentUser = FirebaseAuth.getInstance().currentUser
        var userUID = mCurrentUser!!.uid

//        btn_dashboardEmergency.setOnClickListener {
//            //get user info
//            mDatabase = FirebaseDatabase.getInstance().reference.child("Users").child(userUID)
//            mDatabase!!.addValueEventListener(object : ValueEventListener{
//                override fun onCancelled(p0: DatabaseError) {
//                    TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
//                }
//
//                override fun onDataChange(p0: DataSnapshot) {
//                    var notificationDetails = Notifications()
//                    //set user details for the notification
//                    notificationDetails.userDetails.user_contactNumber = p0.child("user_contact_no").value.toString()
//                    notificationDetails.userDetails.user_firstName = p0.child("user_first_name").value.toString()
//                    notificationDetails.userDetails.user_lastName = p0.child("user_last_name").value.toString()
//
//
//                    //get emergency contact
//                    mDatabase = FirebaseDatabase.getInstance().reference.child("Emergency_Contacts").child(userUID)
//                    mDatabase!!.addValueEventListener(object : ValueEventListener{
//                        override fun onCancelled(p0: DatabaseError) {
//                            TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
//                        }
//
//                        override fun onDataChange(p0: DataSnapshot) {
//                            //loop through emmergency number for user details
//                            var emergencyContacts = ArrayList<Int>(0)
//
//                            for (shot in p0.children){
//                                emergencyContacts.add(shot.value.toString().toInt())
//                            }
//
//                            notificationDetails.emergency_contact_numbers = emergencyContacts
//                            //set notification
////                            notificationDetails.notfication_emergency_service = "Police Department, Ambulance, Fire Department"
//                            notificationDetails.notification_message = "I am in need of help but I don't know who I need to call, or I need immediate help"
//
//                            //send notification along with user data
//                            mDatabase = FirebaseDatabase.getInstance().reference.child("Notifications").child("Police Department, Ambulance, Fire Department").child(userUID).push()
//
//                            mDatabase!!.setValue(notificationDetails).addOnSuccessListener {
//                                Toast.makeText(applicationContext, "Emergency units requested, please wait for the emergency units to respond!", Toast.LENGTH_LONG).show()
//                            }.addOnFailureListener {
//                                Toast.makeText(applicationContext, "Emergency request failed, may God have mercy on your soul", Toast.LENGTH_LONG).show()
//                            }
//                        }
//                    })
//                }
//
//            })
//        }

//        btn_dashboardAmbulance.setOnClickListener {
//
//            //get user info
//            mDatabase = FirebaseDatabase.getInstance().reference.child("Users").child(userUID)
//            mDatabase!!.addValueEventListener(object : ValueEventListener {
//                override fun onCancelled(p0: DatabaseError) {
//                    TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
//                }
//
//                override fun onDataChange(p0: DataSnapshot) {
//                    var notificationDetails = Notifications()
//                    //set user details for the notification
//                    notificationDetails.userDetails.user_contact_no = p0.child("user_contact_no").value.toString()
//                    notificationDetails.userDetails.user_firstName = p0.child("user_first_name").value.toString()
//                    notificationDetails.userDetails.user_lastName = p0.child("user_last_name").value.toString()
//
//
//                    //get emergency contact
//                    mDatabase = FirebaseDatabase.getInstance().reference.child("Emergency_Contacts").child(userUID)
//                    mDatabase!!.addValueEventListener(object : ValueEventListener {
//                        override fun onCancelled(p0: DatabaseError) {
//                            TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
//                        }
//
//                        override fun onDataChange(p0: DataSnapshot) {
//                            //loop through emmergency number for user details
//                            var emergencyContacts = ArrayList<Int>(0)
//
//                            for (shot in p0.children) {
//                                emergencyContacts.add(shot.value.toString().toInt())
//                            }
//                            notificationDetails.emergency_contact_numbers = emergencyContacts
//
//                            //get user's allergies
//                            mDatabase = FirebaseDatabase.getInstance().reference.child("Allergies").child(userUID)
//                            mDatabase!!.addValueEventListener(object : ValueEventListener {
//                                override fun onCancelled(p0: DatabaseError) {
//                                    TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
//                                }
//
//                                override fun onDataChange(p0: DataSnapshot) {
//                                    //loop through allergies of the user for user details
//                                    var allegies = ArrayList<String>(0)
//
//                                    for (shot in p0.children) {
//                                        allegies.add(shot.value.toString())
//                                    }
//                                    notificationDetails.allergies = allegies
//
//                                    //set notification
//                                    notificationDetails.notification_message = "I am in need of medical attention"
//
//                                    //send notification along with user data
//                                    mDatabase = FirebaseDatabase.getInstance().reference.child("Notifications")
//                                        .child("Ambulance").child(userUID).push()
//
//                                    mDatabase!!.setValue(notificationDetails).addOnSuccessListener {
//                                        Toast.makeText(
//                                            applicationContext,
//                                            "Emergency request sent, please sit tight",
//                                            Toast.LENGTH_LONG
//                                        ).show()
//                                    }.addOnFailureListener {
//                                        Toast.makeText(
//                                            applicationContext,
//                                            "Emergency request failed, may God have mercy on your soul",
//                                            Toast.LENGTH_LONG
//                                        ).show()
//                                    }
//                                }
//
//                            })
//                        }
//                    })
//                }
//            })
//        }
//
//        btn_dashboardPolice.setOnClickListener {
//
//            //get user info
//            mDatabase = FirebaseDatabase.getInstance().reference.child("Users").child(userUID)
//            mDatabase!!.addValueEventListener(object : ValueEventListener{
//                override fun onCancelled(p0: DatabaseError) {
//                    TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
//                }
//
//                override fun onDataChange(p0: DataSnapshot) {
//                    var notificationDetails = Notifications()
//                    //set user details for the notification
//                    notificationDetails.userDetails.user_contact_no = p0.child("user_contact_no").value.toString()
//                    notificationDetails.userDetails.user_first_name = p0.child("user_first_name").value.toString()
//                    notificationDetails.userDetails.user_last_name = p0.child("user_last_name").value.toString()
//
//
//                    //get emergency contact
//                    mDatabase = FirebaseDatabase.getInstance().reference.child("Emergency_Contacts").child(userUID)
//                    mDatabase!!.addValueEventListener(object : ValueEventListener{
//                        override fun onCancelled(p0: DatabaseError) {
//                            TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
//                        }
//
//                        override fun onDataChange(p0: DataSnapshot) {
//                            //loop through emmergency number for user details
//                            var emergencyContacts = ArrayList<Int>(0)
//
//                            for (shot in p0.children){
//                                emergencyContacts.add(shot.value.toString().toInt())
//                            }
//
//                            notificationDetails.emergency_contact_numbers = emergencyContacts
//                            //set notification
////                            notificationDetails.notfication_emergency_service = "Police Department, Ambulance, Fire Department"
//                            notificationDetails.notification_message = "I am in need of the police"
//
//                            //send notification along with user data
//                            mDatabase = FirebaseDatabase.getInstance().reference.child("Notifications").child("Police Department").child(userUID).push()
//
//                            mDatabase!!.setValue(notificationDetails).addOnSuccessListener {
//                                Toast.makeText(applicationContext, "Emergency request sent, please sit tight", Toast.LENGTH_LONG).show()
//                            }.addOnFailureListener {
//                                Toast.makeText(applicationContext, "Emergency request failed, may God have mercy on your soul", Toast.LENGTH_LONG).show()
//                            }
//                        }
//                    })
//                }
//            })
//        }
//
//        btn_dashboardFireDepartment.setOnClickListener {
//
//            //get user info
//            mDatabase = FirebaseDatabase.getInstance().reference.child("Users").child(userUID)
//            mDatabase!!.addValueEventListener(object : ValueEventListener{
//                override fun onCancelled(p0: DatabaseError) {
//                    TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
//                }
//
//                override fun onDataChange(p0: DataSnapshot) {
//                    var notificationDetails = Notifications()
//                    //set user details for the notification
//                    notificationDetails.userDetails.user_contact_no = p0.child("user_contact_no").value.toString()
//                    notificationDetails.userDetails.user_first_name = p0.child("user_first_name").value.toString()
//                    notificationDetails.userDetails.user_last_name = p0.child("user_last_name").value.toString()
//
//
//                    //get emergency contact
//                    mDatabase = FirebaseDatabase.getInstance().reference.child("Emergency_Contacts").child(userUID)
//                    mDatabase!!.addValueEventListener(object : ValueEventListener{
//                        override fun onCancelled(p0: DatabaseError) {
//                            TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
//                        }
//
//                        override fun onDataChange(p0: DataSnapshot) {
//                            //loop through emmergency number for user details
//                            var emergencyContacts = ArrayList<Int>(0)
//
//                            for (shot in p0.children){
//                                emergencyContacts.add(shot.value.toString().toInt())
//                            }
//
//                            notificationDetails.emergency_contact_numbers = emergencyContacts
//                            //set notification
////                            notificationDetails.notfication_emergency_service = "Police Department, Ambulance, Fire Department"
//                            notificationDetails.notification_message = "I am in need of help, a fire is spreading in my location"
//
//                            //send notification along with user data
//                            mDatabase = FirebaseDatabase.getInstance().reference.child("Notifications").child("Fire Department").child(userUID).push()
//
//                            mDatabase!!.setValue(notificationDetails).addOnSuccessListener {
//                                Toast.makeText(applicationContext, "Emergency request sent, please sit tight", Toast.LENGTH_LONG).show()
//                            }.addOnFailureListener {
//                                Toast.makeText(applicationContext, "Emergency request failed, may God have mercy on your soul", Toast.LENGTH_LONG).show()
//                            }
//                        }
//                    })
//                }
//            })
//        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        super.onCreateOptionsMenu(menu)
        menuInflater.inflate(R.menu.menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        super.onOptionsItemSelected(item)

        if (item != null){

            when(item.itemId){
                R.id.item_menuLogout -> {
                    //log out user
                    FirebaseAuth.getInstance().signOut()
                    startActivity(Intent(this, MainActivity::class.java))
                    finish()
                }

                R.id.item_menuAccount -> {
                    //opens settingsActivities
                    startActivity(Intent(this, AccountDetailsActivity::class.java))
                }
            }
        }

        return true
    }
}
