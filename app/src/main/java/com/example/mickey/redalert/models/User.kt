package com.example.mickey.redalert.models


import android.os.Parcel
import android.os.Parcelable
import kotlinx.android.parcel.Parcelize
import java.util.*
import kotlin.collections.ArrayList

class User() : Parcelable {
    var user_id: String ?= null
    var user_firstName: String ?= null
    var user_lastName: String ?= null
    var user_email: String ?= null
    var user_address: String ?= null
    var user_contactNumber: String ?= null
    var user_birthDate: Date ?= null
    var user_gender: String ?= null
    var user_profilePictureURL: String ?= null
    var user_bloodType: String ?= null
    var user_isOrganDonor: Boolean ?= null
    var user_allergies: ArrayList<String> ?= null
    var user_emergencyContacts: ArrayList<String> ?= null

    constructor(parcel: Parcel) : this() {
        user_id = parcel.readString()
        user_firstName = parcel.readString()
        user_lastName = parcel.readString()
        user_email = parcel.readString()
        user_address = parcel.readString()
        user_contactNumber = parcel.readString()
        user_gender = parcel.readString()
        user_profilePictureURL = parcel.readString()
        user_bloodType = parcel.readString()
        user_isOrganDonor = parcel.readValue(Boolean::class.java.classLoader) as? Boolean
    }

    constructor(
        user_id: String?,
        user_firstName: String?,
        user_lastName: String?,
        user_email: String?,
        user_address: String?,
        user_contactNumber: String?,
        user_birthDate: Date?,
        user_gender: String?,
        user_profilePictureURL: String?,
        user_bloodType: String?,
        user_isOrganDonor: Boolean?,
        user_allergies: ArrayList<String>?,
        user_emergencyContacts: ArrayList<String>?
    ) : this() {
        this.user_id = user_id
        this.user_firstName = user_firstName
        this.user_lastName = user_lastName
        this.user_email = user_email
        this.user_address = user_address
        this.user_contactNumber = user_contactNumber
        this.user_birthDate = user_birthDate
        this.user_gender = user_gender
        this.user_profilePictureURL = user_profilePictureURL
        this.user_bloodType = user_bloodType
        this.user_isOrganDonor = user_isOrganDonor
        this.user_allergies = user_allergies
        this.user_emergencyContacts = user_emergencyContacts
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(user_id)
        parcel.writeString(user_firstName)
        parcel.writeString(user_lastName)
        parcel.writeString(user_email)
        parcel.writeString(user_address)
        parcel.writeString(user_contactNumber)
        parcel.writeString(user_gender)
        parcel.writeString(user_profilePictureURL)
        parcel.writeString(user_bloodType)
        parcel.writeValue(user_isOrganDonor)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<User> {
        override fun createFromParcel(parcel: Parcel): User {
            return User(parcel)
        }

        override fun newArray(size: Int): Array<User?> {
            return arrayOfNulls(size)
        }
    }


}