package com.example.irgendeineapp

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.Item
import com.xwray.groupie.ViewHolder
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.activity_messages.*
import kotlinx.android.synthetic.main.activity_messages.button
import kotlinx.android.synthetic.main.activity_messages.recyclerview_chat_log
import kotlinx.android.synthetic.main.my_message.view.*
import kotlinx.android.synthetic.main.notes.*

class NotesActivity: AppCompatActivity() {
    companion object{
        val TAG = "ChatLog"
    }

    lateinit var mAuth: FirebaseAuth
    val adapter = GroupAdapter<ViewHolder>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.notes)
        mAuth = FirebaseAuth.getInstance()
        recyclerview_chat_log.adapter = adapter


        supportActionBar?.title = "Notes"


        listenForMessages()


        button.setOnClickListener {

            performSendMessage()
        }
    }



        private fun listenForMessages(){
            val player = mAuth.currentUser?.uid
            val fromId = "0"
            val ref = FirebaseDatabase.getInstance().getReference("/ownPlaySettings/${player}/notes/$fromId")

            ref.addChildEventListener(object: ChildEventListener {

                override fun onChildAdded(p0: DataSnapshot, p1: String?) {
                    val chatMessage = p0.getValue(ChatMessage::class.java)
                    Log.d(TAG, chatMessage?.text)

                    if(chatMessage != null){
                        if(chatMessage.from == "0") {
                            adapter.add(ChatFromItem(chatMessage.text))
                        }else {

                        }
                    }
                    recyclerview_chat_log.scrollToPosition(adapter.itemCount -1)

                }

                override fun onCancelled(p0: DatabaseError) {

                }

                override fun onChildChanged(p0: DataSnapshot, p1: String?) {

                }

                override fun onChildMoved(p0: DataSnapshot, p1: String?) {

                }

                override fun onChildRemoved(p0: DataSnapshot) {

                }
            })
        }

        //Send Message to Firebase
        private fun performSendMessage(){
            val player = mAuth.currentUser?.uid
            val text = edittext_chat_log.text.toString()

            val toId = "0"
            val fromId = "0"
            val reference = FirebaseDatabase.getInstance().getReference("/ownPlaySettings/${player}/notes/$fromId").push()

            val chatMessage = ChatMessage(fromId,reference.key!!, text , toId)
            reference.setValue(chatMessage)
                .addOnSuccessListener {
                    Log.d(TAG, "Saved our chat message:${reference.key}")
                    edittext_chat_log.text.clear()
                    recyclerview_chat_log.scrollToPosition(adapter.itemCount -1)
                }

        }


    }
