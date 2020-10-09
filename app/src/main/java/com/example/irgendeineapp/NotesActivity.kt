package com.example.irgendeineapp

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.Item
import com.xwray.groupie.ViewHolder
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.activity_messages.*
import kotlinx.android.synthetic.main.my_message.view.*

class NotesActivity: AppCompatActivity() {
    companion object{
        val TAG = "ChatLog"
    }


    val adapter = GroupAdapter<ViewHolder>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_messages)

        recyclerview_chat_log.adapter = adapter


        supportActionBar?.title = "Notes"


        listenForMessages()


        button.setOnClickListener {

            performSendMessage()
        }
    }



        private fun listenForMessages(){
            val fromId = "0"
            val ref = FirebaseDatabase.getInstance().getReference("/notes/$fromId")

            ref.addChildEventListener(object: ChildEventListener {

                override fun onChildAdded(p0: DataSnapshot, p1: String?) {
                    val chatMessage = p0.getValue(ChatMessage::class.java)
                    Log.d(TAG, chatMessage?.text)

                    if(chatMessage != null){
                        if(chatMessage.fromId == "0") {
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
            //val text = edittext_chat_log.text.toString()

            val toId = "0"
            val fromId = "0"
            val reference = FirebaseDatabase.getInstance().getReference("/notes/$fromId").push()

            val chatMessage = ChatMessage(reference.key!!, "", fromId,toId, System.currentTimeMillis()/1000,"ID_test","invoke_test")
            reference.setValue(chatMessage)
                .addOnSuccessListener {
                    Log.d(TAG, "Saved our chat message:${reference.key}")
                    //edittext_chat_log.text.clear()
                    recyclerview_chat_log.scrollToPosition(adapter.itemCount -1)
                }

        }


    }
