package com.example.mickey.redalert.models

import com.google.firebase.Timestamp

class Message {
    var message_senderID: String ?= null
    var message_recieverID: String ?= null
    var message_messageContent: String ?= null
    var message_id: String ?= null
    var message_timeStamp : Long ?= null

    constructor()

    constructor(
        message_senderID: String?,
        message_recieverID: String?,
        message_messageContent: String?,
        message_id: String?,
        message_timeStamp: Long?
    ) {
        this.message_senderID = message_senderID
        this.message_recieverID = message_recieverID
        this.message_messageContent = message_messageContent
        this.message_id = message_id
        this.message_timeStamp = message_timeStamp
    }
}