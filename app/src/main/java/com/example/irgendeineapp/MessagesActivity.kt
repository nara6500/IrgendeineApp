package com.example.irgendeineapp

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.Item
import com.xwray.groupie.ViewHolder
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.activity_main.view.*
import kotlinx.android.synthetic.main.activity_messages.*
import kotlinx.android.synthetic.main.my_message.view.*
import kotlinx.android.synthetic.main.my_message.view.message_body
import kotlinx.android.synthetic.main.their_message.view.*
import kotlinx.android.synthetic.main.user_answers.*
import kotlinx.android.synthetic.main.user_answers.view.*

class MessagesActivity: AppCompatActivity()  {

    companion object{
        val TAG = "ChatLog"
    }

    val adapter = GroupAdapter<ViewHolder>()
    val answerAdapter = GroupAdapter<ViewHolder>()
    var toUser: User? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        Log.d("STATUS", "Starting chatlog overview.")
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_messages)

        recyclerview_chat_log.adapter = adapter
        recyclerview_answers.adapter = answerAdapter

        toUser = intent.getParcelableExtra<User>(ContactActivity.USER_KEY)
        val toolbar = supportActionBar
        toolbar?.title = toUser?.user_name
        toolbar?.setDisplayHomeAsUpEnabled(true)

        listenForMessages()
        button.setOnClickListener {
            performSendMessage()
        }
        answer?.setOnClickListener{
            setAnswerToSend()
        }
        provideAnswers("A09")
    }


override fun onSupportNavigateUp(): Boolean {
    onBackPressed()
    return true
}



    private fun listenForMessages(){
        val fromId = "0"
        val toId = toUser?.user_id
        val ref = FirebaseDatabase.getInstance().getReference("/user-messages/$fromId/$toId")

        ref.addChildEventListener(object: ChildEventListener{

            override fun onChildAdded(p0: DataSnapshot, p1: String?) {
                val chatMessage = p0.getValue(ChatMessage::class.java) ?: return
                Log.d(TAG, chatMessage?.text)

                if(chatMessage != null){
                    if(chatMessage.from == "0") {
                        adapter.add(ChatFromItem(chatMessage.text))
                    }else {
                        adapter.add(ChatToItem(chatMessage.text, toUser!!))
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
        val text = recyclerview_answers.answer.text.toString()

        //val fromId = FirebaseAuth.getInstance().uid
        val user = intent.getParcelableExtra<User>(ContactActivity.USER_KEY)
        val toId = user.user_id
        val fromId = "0"
        Log.d(TAG, "Attempt to send message1....")
        //  if (fromId == null ) return
        Log.d(TAG, "Attempt to send message2....")

       // val reference = FirebaseDatabase.getInstance().getReference("/messages").push()
        val reference = FirebaseDatabase.getInstance().getReference("/user-messages/$fromId/$toId").push() /*<--THIS HAS TO GO*/

        val chatMessage = ChatMessage(fromId, reference.key!!, text,toId)
        reference.setValue(chatMessage)
            .addOnSuccessListener {
                Log.d(TAG, "Saved our chat message:${reference.key}")
               // edittext_chat_log.text.clear()
                recyclerview_chat_log.scrollToPosition(adapter.itemCount -1)
            }

        val latestMessageRef = FirebaseDatabase.getInstance().getReference("/latest-messages/$fromId/$toId")
        latestMessageRef.setValue(chatMessage)
    }

    //read available answers from Firebase
    private fun provideAnswers(invoke:String){
        //currently set as default. Subject to change later. Will become var from function parameter
        val answersRef = FirebaseDatabase.getInstance().getReference("/user-messages/0/0")
        answersRef.addListenerForSingleValueEvent(object: ValueEventListener{
            override fun onDataChange(p0: DataSnapshot) {
                p0.children.forEach {
                    if(it.key == invoke && it.child("/to").value == toUser?.user_id){
                    val actualMessage = it.child("/text")
                    actualMessage.children.forEach{
                        answerAdapter.add(UserAnswer(it.value.toString()))

                    }
                }}
            }
            override fun onCancelled(error: DatabaseError) {
            }
        })
    }

    //select answer as answer to write in chatlog
    public fun setAnswerToSend(){
        val text = recyclerview_answers.answer.text.toString()
    }
}

class ChatToItem(val text: String, val user:User): Item<ViewHolder>(){
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

class UserAnswer(val text: String): Item<ViewHolder>(){
    override fun bind(viewHolder: ViewHolder, position: Int) {
        viewHolder.itemView.answer.text = text
        /*viewHolder.itemView.answer.setOnClickListener{
            Log.d("BUTTON PRESSED",this.text)
        }*/
    }
    override fun getLayout(): Int {
        return R.layout.user_answers
    }
}