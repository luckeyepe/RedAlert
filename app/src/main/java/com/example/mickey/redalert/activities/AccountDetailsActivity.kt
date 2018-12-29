package com.example.mickey.redalert.activities

import android.app.Activity
import android.app.Dialog
import android.app.ProgressDialog
import android.content.DialogInterface
import android.content.Intent
import android.net.Uri
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v7.app.AlertDialog
import android.util.Log
import android.view.LayoutInflater
import android.view.Window
import android.widget.ArrayAdapter
import android.widget.Toast
import com.example.mickey.redalert.R
import com.google.android.gms.tasks.Continuation
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.UploadTask
import com.squareup.picasso.Picasso
import com.theartofdev.edmodo.cropper.CropImage
import kotlinx.android.synthetic.main.activity_account_details.*
import kotlinx.android.synthetic.main.activity_signup3v2.*
import kotlinx.android.synthetic.main.popup_change_password.view.*
import java.text.SimpleDateFormat

class AccountDetailsActivity : AppCompatActivity() {
    private val TAG = "AccountDetailsActivity"
    var GALLERY_ID: Int = 1
    var mStorage: StorageReference? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_account_details)

        //grab data
        var currentUser = FirebaseAuth.getInstance().currentUser
        var database = FirebaseFirestore.getInstance().collection("Client").document(currentUser!!.uid)

        val progress = ProgressDialog(this)

        progress.setTitle("Please Wait")
        progress.setMessage("Grabbing your personal data...")
        progress.setCancelable(false) // disable dismiss by tapping outside of the dialog
        progress.show()

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
                            val adapter = ArrayAdapter<String>(
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
                        if(user.user_profilePictureURL == "default"){
                            Picasso.get().load(R.drawable.default_avata)
                                .into(circleImageView_acountDetailsProfilePicture)
                        }else {
                            Picasso.get().load(user.user_profilePictureURL)
                                .into(circleImageView_acountDetailsProfilePicture)
                        }
                        progress.dismiss()
                    }

                }else{
                    //todo popup error that no data exists
                }
            }

        //update profile picture
        circleImageView_acountDetailsProfilePicture.setOnClickListener {
            var alertDialog = android.support.v7.app.AlertDialog.Builder(this)
            alertDialog.setMessage("Do you want to update your profile picture?")
            alertDialog.setCancelable(false)
            alertDialog.setTitle("UPDATE PHOTO")

            alertDialog.setPositiveButton("Yes") { dialog, which ->
                //todo open gallery
                var gallerIntent = Intent()
                gallerIntent.type = "image/*"
                gallerIntent.action = Intent.ACTION_GET_CONTENT
                startActivityForResult(Intent.createChooser(gallerIntent, "Select Image"), GALLERY_ID)
            }

            alertDialog.setNegativeButton("No") { dialog, which ->
                dialog.dismiss()
            }

            alertDialog.show()
        }

        button_accountDetailsUpdatePassword.setOnClickListener {
            var alertDialog = android.support.v7.app.AlertDialog.Builder(this)
            alertDialog.setMessage("Do you want to change your password?")
            alertDialog.setCancelable(false)
            alertDialog.setTitle("CHANGE PASSWORD")

            alertDialog.setPositiveButton("Yes"){dialog, which ->
                //todo popup update password xml
                var dialog: Dialog?
                var popupView = LayoutInflater.from(this).inflate(R.layout.popup_change_password, null)

                var popupEditTextOldPassword = popupView.textInputEditText_popupChangePasswordOldPassword
                var popupEditTextNewPassword = popupView.textInputEditText_popupChangePasswordNewPassword
                var popupEditTextConfirmPassword = popupView.textInputEditText_popupChangePasswordConfirmPassword
                var popupUpdateButton = popupView.button_popupChangePasswordUpdate

                popupUpdateButton.setOnClickListener {
                    if (!popupEditTextOldPassword.text.toString().trim().isNullOrEmpty()
                        &&!popupEditTextNewPassword.text.toString().trim().isNullOrEmpty()
                        && !popupEditTextConfirmPassword.text.toString().trim().isNullOrEmpty()) {
                        val currentUser = FirebaseAuth.getInstance().currentUser
                        val oldPassword = popupEditTextOldPassword.text.toString().trim()
                        val credential = EmailAuthProvider.getCredential(currentUser!!.email.toString(), oldPassword)

                        currentUser.reauthenticate(credential)
                            .addOnCompleteListener {
                                task: Task<Void> ->
                                if (task.isSuccessful){
                                    val newPassword = popupEditTextNewPassword.text.toString().trim()
                                    val confirmPassword = popupEditTextConfirmPassword.text.toString().trim()
                                    if(isValidPassword(newPassword, confirmPassword)){
                                        currentUser.updatePassword(newPassword).addOnCompleteListener {
                                            task: Task<Void> ->
                                            if  (task.isSuccessful){
                                                Log.d(TAG, "User has updated their password")
                                                successDialog("SUCCESS", "Your password has now been update")
                                            }else{
                                                Log.e(TAG, task.exception.toString())
                                            }
                                        }
                                    }
                                }else{
                                    //todo popup old password mismatch
                                    errorDialog("Incorrect old password", "PASSWORD ERROR")
                                }
                            }
                    }else{
                        //todo popup please make sure all fields are not empty
                        errorDialog("Fill in all fields", "MISSING DATA")
                    }

                }

                dialog = Dialog(this)
                dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
                dialog.setContentView(popupView)
                dialog.show()
            }

            alertDialog.setNegativeButton("No") { dialog, which ->
                dialog.dismiss()
            }

            alertDialog.show()
        }

    }

    private fun successDialog(title: String, message: String) {
        var alertDialog = AlertDialog.Builder(this)
        alertDialog.setIcon(R.drawable.ic_info_black_24dp)
        alertDialog.setMessage(message)
        alertDialog.setTitle(title)
        alertDialog.setCancelable(false)
        alertDialog.setPositiveButton("OK") { dialog, which ->
            dialog.dismiss()
        }
        alertDialog.show()
    }

    private fun isValidPassword(newPassword: String, confirmPassword: String): Boolean {
        if(isStringContainNumber(newPassword)){
            if(isStringContainUpperCase(newPassword)){
                if(isStringContainLowerCase(newPassword)){
                    if(isStringContainSpecialCharacter(newPassword)){
                        if(newPassword.length >=8){
                            if (newPassword == confirmPassword){
                                return true
                            }else{
                                errorDialog("New password and confirm password do not match", "INCORRECT PASSWORD")
                            }
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

        errorDialog("Password must contain at least one (1) special character", "INCORRECT PASSWORD")

        return false
    }

    private fun isStringContainLowerCase(string: String): Boolean {
        for(c in string.toCharArray()){
            if (c.isLowerCase())
                return true
        }

        errorDialog("Password must contain at least one (1) lower case character", "INCORRECT PASSWORD")

        return false
    }

    private fun isStringContainUpperCase(string: String): Boolean {
        for(c in string.toCharArray()){
            if (c.isUpperCase())
                return true
        }

        errorDialog("Password must contain at least one (1) upper case character", "INCORRECT PASSWORD")

        return false
    }

    private fun isStringContainNumber(string: String): Boolean {

        for(c in string.toCharArray()){
            if (c.isDigit())
                return true
        }

        errorDialog("Password must contain at least one (1) digit", "INCORRECT PASSWORD")

        return false
    }

    private fun errorDialog(message: String, title: String) {
        var alertDialog = AlertDialog.Builder(this)
        alertDialog.setIcon(R.drawable.ic_error_black_24dp)
        alertDialog.setMessage(message)
        alertDialog.setTitle(title)
        alertDialog.setCancelable(false)
        alertDialog.setPositiveButton("OK") { dialog, which ->
            dialog.dismiss()
        }
        alertDialog.show()
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
                progress.setMessage("Uploading Photo...")
                progress.setCancelable(false) // disable dismiss by tapping outside of the dialog
                progress.show()


                val resultURI = result.uri
                val mCurrentUser = FirebaseAuth.getInstance().currentUser
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
                                Picasso.get().load(downloadUri.toString()).into(circleImageView_acountDetailsProfilePicture)
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
}
