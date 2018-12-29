package com.example.mickey.redalert.activities

import android.app.Activity
import android.app.Dialog
import android.app.ProgressDialog
import android.content.Intent
import android.net.Uri
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Window
import android.widget.ArrayAdapter
import android.widget.Toast
import com.example.mickey.redalert.R
import com.example.mickey.redalert.models.User
import com.google.android.gms.tasks.Continuation
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.UploadTask
import com.squareup.picasso.Picasso
import com.theartofdev.edmodo.cropper.CropImage
import kotlinx.android.synthetic.main.activity_signup3v2.*
import kotlinx.android.synthetic.main.popup_emergency_contacts.view.*


class Signup3Activity : AppCompatActivity() {
    private var mCurrentUser: FirebaseUser?= null
    private var mAuth: FirebaseAuth?=null
    var GALLERY_ID: Int = 1
    var mStorage: StorageReference? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_signup3v2)
        mAuth = FirebaseAuth.getInstance()
        mCurrentUser = FirebaseAuth.getInstance().currentUser

        var user = User()
        user.user_id = mCurrentUser!!.uid

        //on create of the activity fill the listview if available
        fillUpListView()

        btn_signup3AddPhoneNumber.setOnClickListener {
            //popup the emergency contacts
            var dialog: Dialog?
            var popupView = LayoutInflater.from(this).inflate(R.layout.popup_emergency_contacts, null)
            var arrayList = ArrayList<String>()

            var popupEditTextPhoneNumber = popupView.editText_popupEmergencyContactsContactNumber
            var popupButtonAdd = popupView.button_popupEmergencyContactsAdd
            var popupListView = popupView.listView_popupEmergencyContactsPhoneNumberList

            popupButtonAdd.setOnClickListener {
                if(!popupEditTextPhoneNumber.text.toString().trim().isNullOrEmpty()){

                    if (popupEditTextPhoneNumber.text.toString().trim().length == 11) {

                        if (!arrayList.contains(popupEditTextPhoneNumber.text.toString().trim())) {

                            arrayList.add(popupEditTextPhoneNumber.text.toString().trim())
                            var database = FirebaseFirestore.getInstance()
                                .collection("Client")
                                .document(mCurrentUser!!.uid)

                            //update the current list
                            database.update(
                                "user_emergencyContacts",
                                FieldValue.arrayUnion(popupEditTextPhoneNumber.text.toString().trim())
                            )
                                .addOnCompleteListener { task: Task<Void> ->
                                    if (task.isSuccessful) {
                                        //get the contacts list
                                        database.get()
                                            .addOnCompleteListener { task: Task<DocumentSnapshot> ->
                                                if (task.isSuccessful) {
                                                    val user1 = task.result!!.toObject(User::class.java)
                                                    var arrayListUser = user1!!.user_emergencyContacts

                                                    if (arrayListUser != null) {
                                                        var adapter = ArrayAdapter<String>(
                                                            this,
                                                            R.layout.row_simple_text,
                                                            R.id.textView_rowSimpleTextText,
                                                            arrayListUser
                                                        )
                                                        popupListView.adapter = adapter
                                                        fillUpListView()//update the list on the main activity
                                                    }
                                                }
                                            }
                                    }
                                }
                        }else{
                            //todo popup error that contact number already exists
                            var alertDialog = android.support.v7.app.AlertDialog.Builder(this)
                            alertDialog.setMessage("That contact number is already in the list")
                            alertDialog.setTitle("DUPLICATE CONTACT NUMBER")
                            alertDialog.show()
                        }
                    }else{
                        //todo popup error contact number is to short
                        var alertDialog = android.support.v7.app.AlertDialog.Builder(this)
                        alertDialog.setMessage("That contact number is not valid")
                        alertDialog.setTitle("INVALID CONTACT NUMBER")
                        alertDialog.show()
                    }
                }
            }

            dialog = Dialog(this)
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
            dialog.setContentView(popupView)
            dialog.show()
        }

        btn_signup3CreateAccount.setOnClickListener {
            if (listView_signup3EmergencyContacts.adapter.count == 0){
                Toast.makeText(this, "Sorry, but you need to at least one valid emergency contact", Toast.LENGTH_LONG).show()
            }else{
                var dashboardActivity = Intent(this, DashboardActivityJava::class.java)
                startActivity(dashboardActivity)
            }
        }

        img_signup3UserPicture.setOnClickListener {
            var gallerIntent = Intent()
            gallerIntent.type = "image/*"
            gallerIntent.action = Intent.ACTION_GET_CONTENT
            startActivityForResult(Intent.createChooser(gallerIntent, "Select Image"), GALLERY_ID)
        }
    }

    private fun fillUpListView() {
        var database = FirebaseFirestore.getInstance()
            .collection("Client")
            .document(mCurrentUser!!.uid)

        //get the contacts list
        database.get()
            .addOnCompleteListener {
                task: Task<DocumentSnapshot> ->
                if(task.isSuccessful){
                    val user = task.result!!.toObject(User::class.java)
                    var arrayList = user!!.user_emergencyContacts

                    if (arrayList != null) {
                        var adapter = ArrayAdapter<String>(
                            this,
                            R.layout.row_simple_text,
                            R.id.textView_rowSimpleTextText,
                            arrayList
                        )
                        listView_signup3EmergencyContacts.adapter = adapter
                    }

                }
            }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        val progress = ProgressDialog(this)

        if (requestCode == GALLERY_ID && resultCode == Activity.RESULT_OK) {
            val imageURI: Uri = data!!.data

            CropImage.activity(imageURI).setAspectRatio(1,1).start(this)
        }

        if (CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE === requestCode) {
            val result = CropImage.getActivityResult(data)

            if (resultCode === Activity.RESULT_OK) {
                //loading start
                progress.setTitle("Loading")
                progress.setMessage("Please wait while loading...")
                progress.setCancelable(false) // disable dismiss by tapping outside of the dialog
                progress.show()


                val resultURI = result.uri
                mCurrentUser = FirebaseAuth.getInstance().currentUser
                var userID = mCurrentUser!!.uid

                var database = FirebaseFirestore.getInstance().collection("Client").document(userID)
                mStorage = FirebaseStorage.getInstance().reference

                var thumbnailPath = mStorage!!.child("user_profPic").child("$userID.jpg")

                var uploadTask = thumbnailPath.putFile(resultURI)

                var urlTask = uploadTask.continueWithTask(Continuation<UploadTask.TaskSnapshot, Task<Uri>> { task ->
                    if (!task.isSuccessful) {
                        task.exception?.let {
                            throw it
                        }
                    }
                    return@Continuation thumbnailPath.downloadUrl
                }).addOnCompleteListener {
                        task ->
                    if(task.isSuccessful){
                        val downloadUri = task.result
                        var userAddedDetails = HashMap<String, String>()
                        userAddedDetails["user_profilePictureURL"] = downloadUri.toString()

                        database.update(userAddedDetails as Map<String, Any>).addOnCompleteListener {
                            task: Task<Void> ->
                            if (task.isSuccessful) {
                                //loading end
                                progress.dismiss()
                                Picasso.get().load(downloadUri.toString()).into(img_signup3UserPicture)
                            }
                            else{
                                //loading end
                                progress.dismiss()
                                Toast.makeText(applicationContext, "Upload error", Toast.LENGTH_LONG).show()
                            }
                        }
                    }else{
                        //loading end
                        progress.dismiss()
                        Toast.makeText(applicationContext, "Upload error", Toast.LENGTH_LONG).show()
                    }
                }
            }
        }
    }

    override fun onStart() {
        super.onStart()
        var currentUser = mAuth!!.currentUser
    }
}
