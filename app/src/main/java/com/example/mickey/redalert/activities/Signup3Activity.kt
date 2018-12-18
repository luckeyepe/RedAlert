package com.example.mickey.redalert.activities

import android.app.Activity
import android.app.AlertDialog
import android.app.ProgressDialog
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.example.mickey.redalert.R
import com.example.mickey.redalert.models.User
import com.google.android.gms.tasks.Continuation
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.UploadTask
import com.squareup.picasso.Picasso
import com.theartofdev.edmodo.cropper.CropImage
import kotlinx.android.synthetic.main.activity_signup3v2.*


class Signup3Activity : AppCompatActivity() {
    private var mCurrentUser: FirebaseUser?= null
    private var mDatabase: DatabaseReference?= null
    private var mAuth: FirebaseAuth?=null
    private var CONTACT_NUMBERS = ArrayList<Int>(0)
    var GALLERY_ID: Int = 1
    var mStorage: StorageReference? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_signup3v2)
        mAuth = FirebaseAuth.getInstance()
        mCurrentUser = FirebaseAuth.getInstance().currentUser

        var user = User()
        user.user_id = mCurrentUser!!.uid

        btn_signup3AddPhoneNumber.setOnClickListener {
            if (edt_signup3ContactNumber.text.toString().trim().isNullOrEmpty()){
                Toast.makeText(this, "Sorry, but you need to fill in a valid emergency contact", Toast.LENGTH_LONG).show()
            }else{
                CONTACT_NUMBERS.add(edt_signup3ContactNumber.text.toString().trim().toInt())
            }
        }

        btn_signup3CreateAccount.setOnClickListener {
            if (CONTACT_NUMBERS.size == 0){
                Toast.makeText(this, "Sorry, but you need to at least one valid emergency contact", Toast.LENGTH_LONG).show()
            }else{
                mDatabase = FirebaseDatabase.getInstance().reference.child("Emergency_Contacts").child(user.user_id!!)
                mDatabase!!.setValue(CONTACT_NUMBERS).addOnSuccessListener {
                    var dashboardActivity = Intent(this, DashboardActivity::class.java)
                    startActivity(dashboardActivity)
                }
            }
        }

//        img_signup3UserPicture.setOnClickListener {
//            var gallerIntent = Intent()
//            gallerIntent.type = "image/*"
//            gallerIntent.action = Intent.ACTION_GET_CONTENT
//            startActivityForResult(Intent.createChooser(gallerIntent, "Select Image"), GALLERY_ID)
//        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
//        super.onActivityResult(requestCode, resultCode, data)
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

                mDatabase = FirebaseDatabase.getInstance().reference.child("Users").child(userID).child("user_profPic")
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
                        mDatabase!!.setValue(downloadUri.toString()).addOnCompleteListener { task: Task<Void> ->
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
