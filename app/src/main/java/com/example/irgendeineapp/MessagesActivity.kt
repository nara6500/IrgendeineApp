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
import kotlinx.android.synthetic.main.activity_main.view.*
import kotlinx.android.synthetic.main.activity_messages.*
import kotlinx.android.synthetic.main.my_message.view.*
import kotlinx.android.synthetic.main.my_message.view.message_body
import kotlinx.android.synthetic.main.their_message.view.*

class MessagesActivity: AppCompatActivity()  {

    companion object{
        val TAG = "ChatLog"
    }

    val adapter = GroupAdapter<ViewHolder>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_messages)

        recyclerview_chat_log.adapter = adapter

        val user = intent.getParcelableExtra<User>(MainActivity.USER_KEY)

        supportActionBar?.title = user.user_name

        //setupDummyData()
        listenForMessages()


        button.setOnClickListener {

            performSendMessage()
        }
    }

    private fun listenForMessages(){
        val ref = FirebaseDatabase.getInstance().getReference("/messages")

        ref.addChildEventListener(object: ChildEventListener{

            override fun onChildAdded(p0: DataSnapshot, p1: String?) {
                val chatMessage = p0.getValue(ChatMessage::class.java)
                Log.d(TAG, chatMessage?.text)

                if(chatMessage != null){
                    if(chatMessage.fromId == "0") {
                        adapter.add(ChatFromItem(chatMessage.text))
                    }else {
                        adapter.add(ChatToItem(chatMessage.text))
                    }
                }
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
        val text = edittext_chat_log.text.toString()

        //val fromId = FirebaseAuth.getInstance().uid
        val user = intent.getParcelableExtra<User>(MainActivity.USER_KEY)
        val toId = user.user_id
        Log.d(TAG, "Attempt to send message1....")
        //  if (fromId == null ) return
        Log.d(TAG, "Attempt to send message2....")

        val reference = FirebaseDatabase.getInstance().getReference("/messages").push()

        val chatMessage = ChatMessage(reference.key!!, text, "0",toId, System.currentTimeMillis()/1000,"ID_test","invoke__test")
        reference.setValue(chatMessage)
            .addOnSuccessListener {
                Log.d(TAG, "Saved our chat message:${reference.key}")
            }
    }

    private fun setupDummyData(){
        val adapter = GroupAdapter<ViewHolder>()
        adapter.add(ChatToItem("erste Nachricht"))
        adapter.add(ChatFromItem("zweite Nachricht"))
        adapter.add(ChatToItem("dritte Nachricht"))

        recyclerview_chat_log.adapter = adapter
    }
}

class ChatToItem(val text: String): Item<ViewHolder>(){
    override fun bind(viewHolder: ViewHolder, position: Int) {
        viewHolder.itemView.message_body.text = text
    }
    override fun getLayout(): Int {
        return R.layout.my_message
    }
}


class ChatFromItem(val text: String): Item<ViewHolder>(){
    override fun bind(viewHolder: ViewHolder, position: Int) {
        viewHolder.itemView.message_body.text = text
    }
    override fun getLayout(): Int {
        return R.layout.their_message
    }
}