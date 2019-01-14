package com.example.mickey.redalert.models

class CorrespondentWithLatestMessage {
    var correspondentWithLatestMessage_correspondentID: String ?= null
    var correspondentWithLastestMessage_senderID: String ?= null
    var correspondentWithLatestMessage_lastMessageText: String ?= null
    var correspondentWithLatestMessage_lastMessageTimeStamp: Long ?= null
    var correspondentWithLatestMessage_uniqueID: String ?= null

    constructor()
    constructor(
        correspondentWithLatestMessage_correspondentID: String?,
        correspondentWithLastestMessage_senderID: String?,
        correspondentWithLatestMessage_lastMessageText: String?,
        correspondentWithLatestMessage_lastMessageTimeStamp: Long?,
        correspondentWithLatestMessage_uniqueID: String?
    ) {
        this.correspondentWithLatestMessage_correspondentID = correspondentWithLatestMessage_correspondentID
        this.correspondentWithLastestMessage_senderID = correspondentWithLastestMessage_senderID
        this.correspondentWithLatestMessage_lastMessageText = correspondentWithLatestMessage_lastMessageText
        this.correspondentWithLatestMessage_lastMessageTimeStamp = correspondentWithLatestMessage_lastMessageTimeStamp
        this.correspondentWithLatestMessage_uniqueID = correspondentWithLatestMessage_uniqueID
    }


}