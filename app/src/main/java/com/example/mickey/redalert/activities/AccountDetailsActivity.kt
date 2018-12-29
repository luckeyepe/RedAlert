package com.example.mickey.redalert.activities

import android.app.Activity
import android.app.DatePickerDialog
import android.app.Dialog
import android.app.ProgressDialog
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
import com.example.mickey.redalert.models.User
import com.google.android.gms.tasks.Continuation
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.UploadTask
import com.squareup.picasso.Picasso
import com.theartofdev.edmodo.cropper.CropImage
import kotlinx.android.synthetic.main.activity_account_details.*
import kotlinx.android.synthetic.main.activity_signup.*
import kotlinx.android.synthetic.main.popup_allergy_update.view.*
import kotlinx.android.synthetic.main.popup_blood_type.view.*
import kotlinx.android.synthetic.main.popup_change_password.view.*
import kotlinx.android.synthetic.main.popup_emergency_contacts_update.view.*
import kotlinx.android.synthetic.main.popup_update_account_details_part_1.view.*
import java.sql.Timestamp
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

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

        database.addSnapshotListener { documentSnapshot, firebaseFirestoreException ->
            if (documentSnapshot != null){
                val user = documentSnapshot.toObject(com.example.mickey.redalert.models.User::class.java)
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

                        textView_accountDetailsFullName.text = "${user.user_firstName} ${user.user_lastName}"
//                        textView_accountDetailsFullName.text = currentUser.displayName.toString()
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
                Log.e(TAG, firebaseFirestoreException.toString())
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

        button_accountDetailsUpdateInfo.setOnClickListener {
            //todo popup the update account detail xml
            var dialog = Dialog(this)
            var popupView = LayoutInflater.from(this).inflate(R.layout.popup_update_account_details_part_1, null)

            var popupEditTextLastName = popupView.editText_popupUpdateAccountDetailsLastName
            var popupEditTextFirstName = popupView.editText_popupUpdateAccountDetailsFirstName
            var popupEditTextAddress = popupView.editText_popupUpdateAccountDetailsAddress
            var popupEditTextContactNumber = popupView.editText_popupUpdateAccountDetailsContactNumber
            var popupEditTextBirthDate = popupView.editText_popupUpdateAccountDetailsBirthdate
            var popupRadioGroupGender = popupView.radioGroup_popupUpdateAccountDetailsGender
            var popupRadioButtonMale = popupView.radioButton_popupUpdateAccountDetailsMale
            var popupRadioButtonFemale = popupView.radioButton_popupUpdateAccountDetailsFemale
            var popupRadioGroupOrganDonor = popupView.radioGroup_popupUpdateAccountDetailsOrganDonor
            var popupRadioButtonYes = popupView.radioButton_popupUpdateAccountDetailsOrganDonorYes
            var popupRadioButtonNo = popupView.radioButton_popupUpdateAccountDetailsOrganDonorNo
            var popupEditTextBloodType = popupView.editText_popupUpdateAccountDetailsBloodType
            var popupListViewEmergencyContacts = popupView.listView_popupUpdateAccountDetailsEmergencyContacts
            var popupListViewAllergies = popupView.listView_popupUpdateAccountDetailsAllergies
            var popupButtonCancel = popupView.button_popupUpdateAccountDetailsCancel
            var popupButtonUpdate = popupView.button_popupUpdateAccountDetailsUpdate
            var popupImageViewEditAllergies = popupView.imageView_popupUpdateAccountDetailsEditAllergies
            var popupImageViewEditEmergencyContacts = popupView.imageView_popupUpdateAccountDetailsEditEmergencyContacts

            popupEditTextBirthDate.isFocusable = false
            popupEditTextBirthDate.isClickable = true
            popupEditTextBloodType.isFocusable = false
            popupEditTextBloodType.isClickable = true

            //fill up data
            val currentUser = FirebaseAuth.getInstance().currentUser
            val database = FirebaseFirestore.getInstance().collection("Client").document(currentUser!!.uid)

            database.addSnapshotListener { documentSnapshot, firebaseFirestoreException ->
               if (documentSnapshot!=null)
               {
                   ///////////////////////////////////////////////////////////////
                   val user = documentSnapshot.toObject(User::class.java)

                   if (user!=null){
                       popupEditTextLastName.setText(user.user_lastName)
                       popupEditTextFirstName.setText(user.user_firstName)
                       popupEditTextAddress.setText(user.user_address)
                       popupEditTextContactNumber.setText(user.user_contactNumber)

                       //date of birth
                       var dateOfBirth = user.user_birthDate
                       val formatter = SimpleDateFormat("MM/dd/yyyy")
                       popupEditTextBirthDate.setText(formatter.format(dateOfBirth))

                       //gender
                       if(user.user_gender == "M"){
                           popupRadioButtonMale.isChecked = true
                       }else{
                           popupRadioButtonFemale.isChecked = true
                       }

                       //organ donor
                       if(user.user_isOrganDonor == true){
                           popupRadioButtonYes.isChecked = true
                       }else{
                           popupRadioButtonNo.isChecked = true
                       }

                       popupEditTextBloodType.setText(user.user_bloodType)

                       //put emergency contacts into list view
                       val arrayList = user.user_emergencyContacts

                       if (arrayList != null) {
                           val adapter = ArrayAdapter<String>(
                               this,
                               R.layout.row_simple_text,
                               R.id.textView_rowSimpleTextText,
                               arrayList
                           )

                           //dynamic change of listview hight
                           if (arrayList.size > 1){
                               popupListViewEmergencyContacts.layoutParams.height = 95
                           }
                           popupListViewEmergencyContacts.adapter = adapter
                       }

                       //allows emergency listview to scroll
                       popupListViewEmergencyContacts.setOnTouchListener { v, event ->
                           v.parent.requestDisallowInterceptTouchEvent(true)
                           return@setOnTouchListener false
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

                           //dynamic change of listview hight
                           if (arrayListAllergies.size > 1){
                               popupListViewAllergies.layoutParams.height = 95
                           }
                           popupListViewAllergies.adapter = adapter
                       }

                       //allows emergency listview to scroll
                       popupListViewAllergies.setOnTouchListener { v, event ->
                           v.parent.requestDisallowInterceptTouchEvent(true)
                           return@setOnTouchListener false
                       }
                   }
                   ////////////////////////////////////////////////////////////////
               }else{
                   Log.e(TAG,firebaseFirestoreException.toString())
               }
            }

            //date time picker popup
            popupEditTextBirthDate.setOnClickListener {
                val c = Calendar.getInstance()
                val year = c.get(Calendar.YEAR)
                val month = c.get(Calendar.MONTH)
                val day = c.get(Calendar.DAY_OF_MONTH)

                val dpd =
                    DatePickerDialog(this, DatePickerDialog.OnDateSetListener { view, year, monthOfYear, dayOfMonth ->
                        // Display Selected date in textbox
                        var date = "${monthOfYear + 1}/$dayOfMonth/$year"
                        popupEditTextBirthDate.setText(date)
                    }, year, month, day)
                dpd.show()
            }

            //blood type popup
            popupEditTextBloodType.setOnClickListener {
                var dialogBloodType: Dialog?
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
                                popupEditTextBloodType.setText("A+")
                                popupRadioButtonABNegative.isChecked = false
                                popupRadioButtonANegative.isChecked = false
                                popupRadioButtonBNegative.isChecked = false
                                popupRadioButtonONegative.isChecked = false
                            }

                            popupRadioButtonBPositive.id ->{
                                popupEditTextBloodType.setText("B+")
                                popupRadioButtonABNegative.isChecked = false
                                popupRadioButtonANegative.isChecked = false
                                popupRadioButtonBNegative.isChecked = false
                                popupRadioButtonONegative.isChecked = false
                            }

                            popupRadioButtonABPositive.id ->{
                                popupEditTextBloodType.setText("AB+")
                                popupRadioButtonABNegative.isChecked = false
                                popupRadioButtonANegative.isChecked = false
                                popupRadioButtonBNegative.isChecked = false
                                popupRadioButtonONegative.isChecked = false
                            }

                            popupRadioButtonOPositive.id ->{
                                popupEditTextBloodType.setText("O+")
                                popupRadioButtonABNegative.isChecked = false
                                popupRadioButtonANegative.isChecked = false
                                popupRadioButtonBNegative.isChecked = false
                                popupRadioButtonONegative.isChecked = false
                            }
                        }
                    }
                }

                popupRadioGroupNegative.setOnCheckedChangeListener { group, checkedId ->
                    if (checkedId != -1){
                        popupRadioGroupPositive.setOnCheckedChangeListener(null)
                        popupRadioGroupPositive.clearCheck()

                        when(popupRadioGroupNegative.checkedRadioButtonId){
                            popupRadioButtonANegative.id->{
                                popupEditTextBloodType.setText("A-")
                                popupRadioGroupPositive.clearCheck()
                            }

                            popupRadioButtonBNegative.id ->{
                                popupEditTextBloodType.setText("B-")
                                popupRadioGroupPositive.clearCheck()
                            }

                            popupRadioButtonABNegative.id ->{
                                popupEditTextBloodType.setText("AB-")
                                popupRadioGroupPositive.clearCheck()
                            }

                            popupRadioButtonONegative.id ->{
                                popupEditTextBloodType.setText("O-")
                                popupRadioGroupPositive.clearCheck()
                            }
                        }
                    }
                }

                dialogBloodType = Dialog(this)
                dialogBloodType.requestWindowFeature(Window.FEATURE_NO_TITLE)
                dialogBloodType.setContentView(popupView)
                dialogBloodType.setCancelable(false)
                dialogBloodType.show()

                popupButtonOK.setOnClickListener {
                    dialogBloodType.dismiss()
                }
            }


            //popup edit allergies
            popupImageViewEditAllergies.setOnClickListener {
                //todo popup edit allergies
                Toast.makeText(this, "Popup Edit Allergies", Toast.LENGTH_SHORT).show()
                var dialogAllergyUpdate: Dialog?
                var popupView = LayoutInflater.from(this).inflate(R.layout.popup_allergy_update, null)

                val popupEditTextAllergy = popupView.editText_popUpAllergyUpdateAllergy
                val popupButtonAdd = popupView.button_popUpAllergyUpdateAdd
                val popupButtonCancel = popupView.button_popUpAllergyUpdateCancel
                val popupListView = popupView.listView_popupAllergyUpdateAllergyList

                //fill in data
                val currentUser = FirebaseAuth.getInstance().currentUser
                val database = FirebaseFirestore.getInstance()
                    .collection("Client")
                    .document("${currentUser!!.uid}")

                database.addSnapshotListener { documentSnapshot, firebaseFirestoreException ->
                    if (documentSnapshot!=null)
                    {
//                        val user = documentSnapshot.toObject(com.example.mickey.redalert.models.User::class.java)
                        val arrayListAllergy = documentSnapshot.get("user_allergies") as ArrayList<String>

                            if (arrayListAllergy != null) {
                                val adapter = ArrayAdapter<String>(
                                    this,
                                    R.layout.row_simple_text,
                                    R.id.textView_rowSimpleTextText,
                                    arrayListAllergy
                                )
                                popupListView.adapter = adapter


                                //allows list to open
                                popupListView.setOnItemClickListener { parent, view, position, id ->
                                    var alertDialog = AlertDialog.Builder(this)
                                    alertDialog.setIcon(R.drawable.ic_info_black_24dp)
                                    alertDialog.setMessage(
                                        "Do you want to delete \n" +
                                                "'${popupListView.getItemAtPosition(position)}' from your allergies? "
                                    )
                                    alertDialog.setTitle("CONFIRM DELETE!")
                                    alertDialog.setCancelable(false)

                                    alertDialog.setPositiveButton("OK") { dialog, which ->
                                        arrayListAllergy.removeAt(position)

                                        val adapter = ArrayAdapter<String>(
                                            this,
                                            R.layout.row_simple_text,
                                            R.id.textView_rowSimpleTextText,
                                            arrayListAllergy
                                        )
                                        popupListView.adapter = adapter

                                        database.update("user_allergies", arrayListAllergy)

                                    }

                                    alertDialog.setNegativeButton("No!") { dialog, which ->
                                        dialog.dismiss()

                                    }

                                    alertDialog.show()
                                    Toast.makeText(
                                        this,
                                        "${popupListView.getItemAtPosition(position)}",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            }
                        }else{
                        Log.e(TAG, firebaseFirestoreException.toString())
                    }

                }

                dialogAllergyUpdate = Dialog(this)
                dialogAllergyUpdate.requestWindowFeature(Window.FEATURE_NO_TITLE)
                dialogAllergyUpdate.setContentView(popupView)
                dialogAllergyUpdate.setCancelable(false)
                dialogAllergyUpdate.show()

                popupButtonCancel.setOnClickListener {
                    dialogAllergyUpdate.dismiss()
                }
                popupButtonAdd.setOnClickListener {
                    val currentUser = FirebaseAuth.getInstance().currentUser
                    val database = FirebaseFirestore.getInstance()
                        .collection("Client")
                        .document("${currentUser!!.uid}")
                    val allergy = popupEditTextAllergy.text.toString().trim()

                    if (allergy.isNullOrEmpty())
                    {
                        errorDialog("Please Enter Allergy!", "MISSING DATA!")
                    }else{
                        database.get().addOnCompleteListener { task: Task<DocumentSnapshot> ->
                            if (task.isSuccessful) {
                                //do
                                val arrayListAllergy = task.result!!.get("user_allergies") as ArrayList<String>
                                if (arrayListAllergy != null) {
                                    if (arrayListAllergy.contains(allergy)) {
                                        errorDialog(
                                            "'$allergy' is already in the\n Allergy List!",
                                            "DUPLICATE ENTRY!"
                                        )
                                    } else {
                                        database.update("user_allergies", FieldValue.arrayUnion(allergy))
                                    }
                                }
                            }else {
                                Log.e(TAG, task.exception.toString())
                            }
                        }
                    }

                }

            }

            //popup edit emergency contacts
            popupImageViewEditEmergencyContacts.setOnClickListener {
                //todo popup edit emergency contacts
                Toast.makeText(this, "Popup edit contacts", Toast.LENGTH_SHORT).show()
                var dialogEmergencyContacts: Dialog?
                var popupView = LayoutInflater.from(this).inflate(R.layout.popup_emergency_contacts_update, null)

                //////////////////////////////////////////////
                val popupEditTextContact = popupView.editText_popupEmergencyContactsUpdateContactNumber
                val popupButtonAdd = popupView.button_popupEmergencyContactsUpdateAdd
                val popupButtonCancel = popupView.button_popupEmergencyContactsUpdateCancel
                val popupListView = popupView.listView_popupEmergencyContactsUpdateList

                //fill in data
                val currentUser = FirebaseAuth.getInstance().currentUser
                val database = FirebaseFirestore.getInstance()
                    .collection("Client")
                    .document("${currentUser!!.uid}")

                database.addSnapshotListener { documentSnapshot, firebaseFirestoreException ->
                    if (documentSnapshot!=null)
                    {
//                        val user = documentSnapshot.toObject(com.example.mickey.redalert.models.User::class.java)
                        val arrayListContacts = documentSnapshot.get("user_emergencyContacts") as ArrayList<String>

                        if (arrayListContacts != null) {
                            val adapter = ArrayAdapter<String>(
                                this,
                                R.layout.row_simple_text,
                                R.id.textView_rowSimpleTextText,
                                arrayListContacts
                            )
                            popupListView.adapter = adapter


                            //allows list to open
                            popupListView.setOnItemClickListener { parent, view, position, id ->
                                var alertDialog = AlertDialog.Builder(this)
                                alertDialog.setIcon(R.drawable.ic_info_black_24dp)
                                alertDialog.setMessage(
                                    "Do you want to delete \n" +
                                            "'${popupListView.getItemAtPosition(position)}' from your contacts? "
                                )
                                alertDialog.setTitle("CONFIRM DELETE!")
                                alertDialog.setCancelable(false)

                                alertDialog.setPositiveButton("OK") { dialog, which ->
                                    arrayListContacts.removeAt(position)

                                    val adapter = ArrayAdapter<String>(
                                        this,
                                        R.layout.row_simple_text,
                                        R.id.textView_rowSimpleTextText,
                                        arrayListContacts
                                    )
                                    popupListView.adapter = adapter

                                    database.update("user_emergencyContacts", arrayListContacts)

                                }

                                alertDialog.setNegativeButton("No!") { dialog, which ->
                                    dialog.dismiss()

                                }

                                alertDialog.show()
                                Toast.makeText(
                                    this,
                                    "${popupListView.getItemAtPosition(position)}",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        }
                    }else{
                        Log.e(TAG, firebaseFirestoreException.toString())
                    }

                }
                //////////////////////////////////////////////

                dialogEmergencyContacts = Dialog(this)
                dialogEmergencyContacts.requestWindowFeature(Window.FEATURE_NO_TITLE)
                dialogEmergencyContacts.setContentView(popupView)
                dialogEmergencyContacts.setCancelable(false)
                dialogEmergencyContacts.show()

                popupButtonCancel.setOnClickListener {
                        dialogEmergencyContacts.dismiss()
                }
                popupButtonAdd.setOnClickListener {
                    val currentUser = FirebaseAuth.getInstance().currentUser
                    val database = FirebaseFirestore.getInstance()
                        .collection("Client")
                        .document("${currentUser!!.uid}")
                    val contactNumber = popupEditTextContact.text.toString().trim()

                    if (contactNumber.isNullOrEmpty())
                    {
                        errorDialog("Please Enter Contact Number!", "MISSING DATA!")
                    }else {
                        if (contactNumber.length == 11) {
                            database.get().addOnCompleteListener { task: Task<DocumentSnapshot> ->
                                if (task.isSuccessful) {
                                    //do
                                    val arrayListAllergy =
                                        task.result!!.get("user_emergencyContacts") as ArrayList<String>
                                    if (arrayListAllergy != null) {
                                        if (arrayListAllergy.contains(contactNumber)) {
                                            errorDialog(
                                                "'$contactNumber' is already in the\n Contact List!",
                                                "DUPLICATE ENTRY!"
                                            )
                                        } else {
                                            database.update(
                                                "user_emergencyContacts",
                                                FieldValue.arrayUnion(contactNumber)
                                            )
                                        }
                                    }
                                } else {
                                    Log.e(TAG, task.exception.toString())
                                }
                            }
                        }else{
                            errorDialog("Emergency Contact Number should be at least 11 digits long!! ",
                                "INVALID CONTACT NUMBER")
                        }
                    }

                }
            }

            popupButtonUpdate.setOnClickListener {
                var currentUser = FirebaseAuth.getInstance().currentUser
                Log.d(TAG, "the current user id is ${currentUser!!.uid}")
                //
                val firstName = popupEditTextFirstName.text.toString().trim()
                val lastName = popupEditTextLastName.text.toString().trim()
                val address = popupEditTextAddress.text.toString().trim()
                val contactNumber = popupEditTextContactNumber.text.toString().trim()

                val stringDate = popupEditTextBirthDate.text.toString().trim()
                val formatter: DateFormat
                val date: Date
                formatter = SimpleDateFormat("MM/dd/yyyy")
                date = formatter.parse(stringDate)
                val timeStampDate = Timestamp(date.time)

                val birthDate = timeStampDate
                val bloodType = popupEditTextBloodType.text.toString().trim()
                var gender = ""

                gender = if (popupRadioGroupGender.checkedRadioButtonId == popupRadioButtonMale.id){
                    "M"
                }else{
                    "F"
                }

                var organDonor = false
                organDonor = popupRadioGroupOrganDonor.checkedRadioButtonId == popupRadioButtonYes.id

                if (!firstName.isNullOrEmpty() && !lastName.isNullOrEmpty() && !address.isNullOrEmpty()
                    && !contactNumber.isNullOrEmpty()){
                    val progress = ProgressDialog(this)

                    progress.setTitle("Please Wait")
                    progress.setMessage("Updating your personal data...")
                    progress.setCancelable(false) // disable dismiss by tapping outside of the dialog
                    progress.show()
                    //todo update info
                    database.update(
                        "user_firstName", firstName,
                        "user_lastName", lastName,
                        "user_address", address,
                        "user_contactNumber", contactNumber,
                        "user_birthDate", birthDate,
                        "user_bloodType", bloodType,
                        "user_gender", gender,
                        "user_isOrganDonor", organDonor
                    ).addOnCompleteListener {
                        task: Task<Void> ->
                        if (task.isSuccessful){
                            val profileUpdates = UserProfileChangeRequest.Builder()
                                .setDisplayName("$firstName $lastName")
                                .build()

                            if (currentUser != null) {
                                currentUser.updateProfile(profileUpdates)
                                    .addOnCompleteListener { task ->
                                        if (task.isSuccessful) {
                                            Log.d(TAG, "User profile updated.")
                                            progress.dismiss()
                                            successDialog("UPDATE SUCCESS", "User information is now up to date")
                                        }
                                    }
                            }

                        }
                    }
                }

            }

            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
            dialog.setContentView(popupView)
            dialog.setCancelable(false)
            dialog.show()

            popupButtonCancel.setOnClickListener {
                dialog.dismiss()
            }
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
