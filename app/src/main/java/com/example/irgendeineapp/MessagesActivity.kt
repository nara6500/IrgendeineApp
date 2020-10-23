package com.example.irgendeineapp

import android.graphics.Color
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
    var toId: String? =""
    var gameManager: GameManager? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_messages)
        mAuth = FirebaseAuth.getInstance()

        gameManager = GameManager()
        gameManager?.invoke?.clear() //CLEAR LOCAL LIST OF INVOKE, JUST TO BE SURE
        //println("INVOKE LIST CLEARED.")
        gameManager?.getInvokeFromDatabase() //GET CURRENTLY NEEDED INVOKE FROM FIREBASE USER PROFILE
        //println("INVOKE LIST AT START IS " + invoke)

        toUser = intent.getParcelableExtra<User>(ContactActivity.USER_KEY)
        toId = toUser?.user_id
        val toolbar = supportActionBar
        toolbar?.title = toUser?.user_name
        toolbar?.setDisplayHomeAsUpEnabled(true)

        listenForMessages()
        button.setOnClickListener {
            this.performSendMessage()
        }

        provideUserAnswers()

        recyclerview_answers.adapter = answerAdapter
        recyclerview_chat_log.adapter = adapter
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }

    private fun listenForMessages(){
        val fromId = "0"

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
        val player = mAuth.currentUser?.uid
        val fromId = "0"

        // val reference = FirebaseDatabase.getInstance().getReference("/messages").push()
        val reference = FirebaseDatabase.getInstance().getReference("/ownPlaySettings/${player}/messages/$fromId/$toId").push()
        val chatMessage = ChatMessage(fromId, reference.key!!, text!!,toId!!)
        reference.setValue(chatMessage!!)
            .addOnSuccessListener {
                gameManager?.invoke?.clear()

                // invoke.add(selectedAnswer?.invoke.toString())

                //setInvokeInDatabase(selectedAnswer?.invoke.toString())
                //DELETE ALL INVOKES FROM CURRENT INVOKE ENTRY
                gameManager?.clearInvokesFromDatabase()

                for(x in 0 until selectedAnswer?.invoke!!.size ){
                    gameManager?.invoke?.add(selectedAnswer?.invoke!![x])
                    //println("selectedAnswerinvoke" + selectedAnswer?.invoke!![x])
                }

                //WRITE NEW ENTRIES TO INVOKE
                //println("INVOKE SIZE IS " + invoke.size + ". STARTING TO WRITE NOW.")
                for( x in 0 until gameManager?.invoke?.size!!) {
                    //setInvokeInDatabase(it.child("/invoke").value.toString())
                    gameManager?.setInvokeInDatabase(gameManager?.invoke!![x])
                }

                answerAdapter.clear()
                recyclerview_chat_log.scrollToPosition(adapter.itemCount -1)
            }

        val latestMessageRef = FirebaseDatabase.getInstance().getReference("/ownPlaySettings/${player}/latest-messages/$fromId/$toId")
        latestMessageRef.setValue(chatMessage)

        //println("STARTING provideUserAnswers(). PARAMETERS ARE: " + invoke + " AND: " + toId)

            //provideUserAnswers(invoke, toId)
            provideUserAnswers()

    }







    private fun provideUserAnswers(){
        //println("ENTERING provideUserAnswers() NOW.")
        val player = mAuth.currentUser?.uid
        val answersRef = FirebaseDatabase.getInstance().getReference("/user-messages/0/$toId")
        answersRef.addListenerForSingleValueEvent(object: ValueEventListener{
            override fun onDataChange(p0: DataSnapshot) {
                p0.children.forEach {
                    if(gameManager?.invoke!!.contains(it.key)){
                        val fromId = it.child("/from").value.toString()
                        val toId = it.child("/to").value.toString()
                        val reference = FirebaseDatabase.getInstance().getReference("/ownPlaySettings/${player}/messages/$toId/$fromId").push()

                        //DELETE ALL INVOKES FROM CURRENT INVOKE ENTRY
                        gameManager?.clearInvokesFromDatabase()
                        gameManager?.invoke?.clear()
                        //println("CLEARED CONTENTS FROM INVOKE LIST.")
                        //WRITE NEW ENTRIES TO INVOKE

                        for(x in 0 until it.child("/invoke").childrenCount) {
                            gameManager?.invoke?.add(it.child("/invoke").child("/$x").value.toString()!!)
                        }
                        //println("INVOKE IS NOW SET TO " + invoke + " AFTER LOOPING.")
                        for( x in 0 until gameManager?.invoke!!.size) {
                            //setInvokeInDatabase(it.child("/invoke").value.toString())
                            //setInvokeInDatabase(invoke[x]) TODO: new Invokes
                            gameManager?.setInvokeInDatabase(it.child("/invoke").child("/$x").value.toString()!!)

                        }

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
        //println("ENTERING provideAnswers() NOW.")
        val answersRef = FirebaseDatabase.getInstance().getReference("/user-messages/0/0")
        answersRef.addListenerForSingleValueEvent(object: ValueEventListener{
            override fun onDataChange(p0: DataSnapshot) {
                p0.children.forEach {
                    if(gameManager?.invoke!!.contains(it.key) && it.child("/to").value == toUser?.user_id){
                        val actualMessage = it.child("/text")
                        val invokeRef = it.child("/invoke")
                        //clearInvokesFromDatabase()
                        //println("CLEARING. SIZE SET TO: " + invoke.size)
                        //println(it.child("/invoke").childrenCount)
                        //for(x in 0 until it.child("/invoke").childrenCount)
                        //setInvokeInDatabase(it.child("/invoke").child("/$x").value.toString())
                        //println("FINAL ENTRY FOR FIREBASE IS: " + it.child("/invoke").value.toString())

                        actualMessage.children.forEach{
                            val _invoke = mutableListOf<String>()
                            for(x in 0 until invokeRef.childrenCount){
                                _invoke.add(invokeRef.child("/$x").value.toString())
                                //println("Was ist da drin?"+ it.child("/invoke").child("/$x").value.toString())
                            }
                            answerAdapter.add(UserAnswer(it.value.toString(), _invoke))
                        }
                    }}

                answerAdapter.setOnItemClickListener { item, view ->
                    val answerItem = item as UserAnswer
                    selectedAnswer = answerItem
                    view.setBackgroundColor(Color.parseColor("#404040"))
                    //println("klick"+ selectedAnswer?.invoke.toString())
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

class UserAnswer(val text: String, val invoke: MutableList<String>): Item<ViewHolder>(){
    override fun bind(viewHolder: ViewHolder, position: Int) {
        viewHolder.itemView.answer.text = text
    }
    override fun getLayout(): Int {
        return R.layout.user_answers
    }
}