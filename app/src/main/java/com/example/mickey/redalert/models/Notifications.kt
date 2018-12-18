package com.example.mickey.redalert.models

class Notifications {
    var userDetails = User()
    var notification_message: String ?= null
    var emergency_contact_numbers =  ArrayList<Int>(0)
    var allergies: ArrayList<String>?= null
}