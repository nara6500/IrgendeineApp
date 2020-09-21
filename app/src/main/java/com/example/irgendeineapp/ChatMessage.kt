package com.example.irgendeineapp

class ChatMessage(val id: String, val text: String, val fromId: String, val toId: String, val timestamp: Long, val storypathId: String, val invokeID: String) {
    constructor() : this("","","","", -1,"", "")
}