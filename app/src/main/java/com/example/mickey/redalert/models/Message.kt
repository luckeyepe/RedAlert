package com.example.mickey.redalert.models

class Message {
    var message_senderID: String ?= null
    var message_senderName: String ?= null
    var message_recieverID: String ?= null
    var message_messageContent: String ?= null
    var message_id: String ?= null
    var message_timeStamp : Long ?= null
    var message_type: String ?=null

    constructor()

    constructor(
        message_senderID: String?,
        message_senderName: String?,
        message_recieverID: String?,
        message_messageContent: String?,
        message_id: String?,
        message_timeStamp: Long?,
        message_type: String?
    ) {
        this.message_senderID = message_senderID
        this.message_senderName = message_senderName
        this.message_recieverID = message_recieverID
        this.message_messageContent = message_messageContent
        this.message_id = message_id
        this.message_timeStamp = message_timeStamp
        this.message_type = message_type
    }
//    constructor(
//        message_senderID: String?,
//        message_recieverID: String?,
//        message_messageContent: String?,
//        message_id: String?,
//        message_timeStamp: Long?
//    ) {
//        this.message_senderID = message_senderID
//        this.message_recieverID = message_recieverID
//        this.message_messageContent = message_messageContent
//        this.message_id = message_id
//        this.message_timeStamp = message_timeStamp
//    }
}