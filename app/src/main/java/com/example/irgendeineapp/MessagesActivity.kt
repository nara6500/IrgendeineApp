package com.example.irgendeineapp

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.Item
import com.xwray.groupie.ViewHolder
import kotlinx.android.synthetic.main.activity_messages.*
import kotlinx.android.synthetic.main.my_message.view.*
import kotlinx.android.synthetic.main.user_answers.view.*



class MessagesActivity: AppCompatActivity()  {

    lateinit var mAuth: FirebaseAuth

    companion object{
        val TAG = "ChatLog"
    }

    val adapter = GroupAdapter<ViewHolder>()
    val answerAdapter = GroupAdapter<ViewHolder>()
    var toUser: User? = null
    var invoke = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_messages)
        mAuth = FirebaseAuth.getInstance()

        getInvokeFromDatabase()
        toUser = intent.getParcelableExtra<User>(ContactActivity.USER_KEY)
        val toolbar = supportActionBar
        toolbar?.title = toUser?.user_name
        toolbar?.setDisplayHomeAsUpEnabled(true)

        listenForMessages()
         button.setOnClickListener {
             this.performSendMessage()
        }





        provideAnswers()

        recyclerview_answers.adapter = answerAdapter
        recyclerview_chat_log.adapter = adapter
    }


override fun onSupportNavigateUp(): Boolean {
    onBackPressed()
    return true
}



    private fun listenForMessages(){
        val fromId = "0"
        val toId = toUser?.user_id
        val player = mAuth.currentUser?.uid
        val ref = FirebaseDatabase.getInstance().getReference("/ownPlaySettings/${player}/messages/$fromId/$toId")


        ref.addChildEventListener(object: ChildEventListener{

            override fun onChildAdded(p0: DataSnapshot, p1: String?) {
                val chatMessage = p0.getValue(ChatMessage::class.java) ?: return


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
        val text = selectedAnswer?.text

        //val fromId = FirebaseAuth.getInstance().uid
        val user = intent.getParcelableExtra<User>(ContactActivity.USER_KEY)
        val player = mAuth.currentUser?.uid
        val toId = user.user_id
        val fromId = "0"


       // val reference = FirebaseDatabase.getInstance().getReference("/messages").push()
        val reference = FirebaseDatabase.getInstance().getReference("/ownPlaySettings/${player}/messages/$fromId/$toId").push()
        val chatMessage = ChatMessage(fromId, reference.key!!, text!!,toId)
        reference.setValue(chatMessage)
            .addOnSuccessListener {

               // edittext_chat_log.text.clear()


                setInvokeInDatabase(selectedAnswer?.invoke.toString())



                answerAdapter.clear()
                recyclerview_chat_log.scrollToPosition(adapter.itemCount -1)
            }

        val latestMessageRef = FirebaseDatabase.getInstance().getReference("/ownPlaySettings/${player}/latest-messages/$fromId/$toId")
        latestMessageRef.setValue(chatMessage)

        provideUserAnswers()
    }

    private fun getInvokeFromDatabase(){
        val player = mAuth.currentUser?.uid
        val invokeRef =  FirebaseDatabase.getInstance().getReference("/ownPlaySettings/${player}/playerSettings/")
          invokeRef.addListenerForSingleValueEvent(object : ValueEventListener {
              override fun onDataChange(p0: DataSnapshot) {
                  var invokeValue = p0.child("/invoke").value.toString()

                  invoke = invokeValue


              }

              override fun onCancelled(error: DatabaseError) {
              }
          })







    }


    private fun setInvokeInDatabase(_invoke: String){
        val player = mAuth.currentUser?.uid
        val invokeRef = FirebaseDatabase.getInstance().getReference("/ownPlaySettings/${player}/playerSettings")
        invokeRef.child("/invoke").setValue(_invoke)
        invoke = _invoke
    }

    private fun provideUserAnswers(){
        val player = mAuth.currentUser?.uid
        //currently set as default. Subject to change later. Will become var from function parameter
        val answersRef = FirebaseDatabase.getInstance().getReference("/user-messages/0/2")
        answersRef.addListenerForSingleValueEvent(object: ValueEventListener{
            override fun onDataChange(p0: DataSnapshot) {
                p0.children.forEach {
                    if(it.key == invoke){
                        val fromId = it.child("/from").value.toString()
                        val toId = it.child("/to").value.toString()
                        val reference = FirebaseDatabase.getInstance().getReference("/ownPlaySettings/${player}/messages/$toId/$fromId").push()

                        setInvokeInDatabase(it.child("/invoke").value.toString())

                        val actualMessage = it.child("/text").value.toString()
                        val chatMessage = ChatMessage(fromId, it.key.toString(), actualMessage,toId)
                        reference.setValue(chatMessage)
                            .addOnSuccessListener {
                                // edittext_chat_log.text.clear()
                                recyclerview_chat_log.scrollToPosition(adapter.itemCount -1)
                                val latestMessageRef = FirebaseDatabase.getInstance().getReference("/ownPlaySettings/${player}/latest-messages/$toId/$fromId")
                                latestMessageRef.setValue(chatMessage)
                            }

                    }else{
                      //  Log.d("keine antwort", it.key)
                    }
                    provideAnswers()
                }
            }
            override fun onCancelled(error: DatabaseError) {
            }
        })


    }

    var selectedAnswer :UserAnswer? = null
    //read available answers from Firebase
    private fun provideAnswers(){
        //currently set as default. Subject to change later. Will become var from function parameter
        val answersRef = FirebaseDatabase.getInstance().getReference("/user-messages/0/0")
        answersRef.addListenerForSingleValueEvent(object: ValueEventListener{
            override fun onDataChange(p0: DataSnapshot) {
                p0.children.forEach {
                    if(it.key == invoke && it.child("/to").value == toUser?.user_id){
                        val actualMessage = it.child("/text")

                        setInvokeInDatabase(it.child("/invoke").value.toString())

                        actualMessage.children.forEach{
                        answerAdapter.add(UserAnswer(it.value.toString(), invoke))
                    }
                }}

                answerAdapter.setOnItemClickListener { item, view ->
                    val answerItem = item as UserAnswer
                    selectedAnswer = answerItem

                }


            }
            override fun onCancelled(error: DatabaseError) {
            }
        })
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

class UserAnswer(val text: String, val invoke: String): Item<ViewHolder>(){
    override fun bind(viewHolder: ViewHolder, position: Int) {
        viewHolder.itemView.answer.text = text
    }
    override fun getLayout(): Int {
        return R.layout.user_answers
    }
}