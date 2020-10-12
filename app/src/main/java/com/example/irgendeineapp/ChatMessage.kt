package com.example.irgendeineapp

class ChatMessage(val from: String, val invoke: String, val text: String, val to: String) {
    constructor() : this("","","","")
}