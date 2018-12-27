package com.example.mickey.redalert.models


import java.sql.Timestamp



class User {
    var user_id: String ?= null
    var user_firstName: String ?= null
    var user_lastName: String ?= null
    var user_email: String ?= null
    var user_address: String ?= null
    var user_contactNumber: Long ?= null
    var user_birthDate: Timestamp ?= null
    var user_gender: String ?= null
    var user_profilePictureURL: String ?= null
    var user_bloodType: String ?= null
    var user_isOrganDonor: Boolean ?= null
    var user_allergies: Array<String> ?= null
    var user_user_emergencyContacts: ArrayList<Long> ?= null

    constructor(
        user_id: String?,
        user_firstName: String?,
        user_lastName: String?,
        user_email: String?,
        user_address: String?,
        user_contactNumber: Long?,
        user_birthDate: Timestamp?,
        user_gender: String?,
        user_profilePictureURL: String?,
        user_bloodType: String?,
        user_isOrganDonor: Boolean?
    ) {
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
    }

    constructor()


}