package com.example.irgendeineapp

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.os.Vibrator
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.Item
import com.xwray.groupie.ViewHolder
import kotlinx.android.synthetic.main.activity_messages.*
import kotlinx.android.synthetic.main.my_message.view.*
import kotlinx.android.synthetic.main.user_answers.view.*
import kotlinx.coroutines.*



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
    var isFirstMessage = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_messages)
        mAuth = FirebaseAuth.getInstance()

        gameManager = GameManager(this)
        gameManager?.getPlayerName()
        gameManager?.invoke?.clear() //CLEAR LOCAL LIST OF INVOKE, JUST TO BE SURE
        gameManager?.getInvokeFromDatabase() //GET CURRENTLY NEEDED INVOKE FROM FIREBASE USER PROFILE

        toUser = intent.getParcelableExtra<User>(ContactActivity.USER_KEY)
        toId = toUser?.user_id
        gameManager?.userId = toId!!
        val toolbar = supportActionBar
        toolbar?.title = toUser?.user_name
        toolbar?.setDisplayHomeAsUpEnabled(true)

        listenForMessages()
        button.setOnClickListener {
            if (selectedAnswer!=null){
                this.performSendMessage()
            }else{
                Toast.makeText(baseContext, "Was soll ich senden?",
                    Toast.LENGTH_SHORT).show()
            }

        }

        if(toId == "1"){
            provideChat()
        }
        provideUserAnswers()

        recyclerview_answers.adapter = answerAdapter
        recyclerview_chat_log.adapter = adapter
    }

    override fun onSupportNavigateUp(): Boolean {
        val intent = Intent(this, ContactActivity::class.java)
        startActivity(intent)
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
                        adapter.add(ChatToItem(chatMessage.text, toUser!!))
                    }else {
                        adapter.add(ChatFromItem(chatMessage.text))
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
                //gameManager?.clearInvokesFromDatabase("")

              //  gameManager?.invoke?.clear()

                // invoke.add(selectedAnswer?.invoke.toString())

                //setInvokeInDatabase(selectedAnswer?.invoke.toString())
                //gameManager?.clearInvokesFromDatabase("")
                println("selectedAnswerNode"+selectedAnswer?.node)
                gameManager?.clearInvokesFromDatabase(selectedAnswer?.node!!)
                gameManager?.handleDeleteNodes(selectedAnswer?.node!!)
                gameManager?.invoke?.clear()



                for(x in 0 until selectedAnswer?.invoke!!.size ){
                    gameManager?.invoke?.add(selectedAnswer?.invoke!![x])
                    //println("selectedAnswerinvoke" + selectedAnswer?.invoke!![x])
                }



                //WRITE NEW ENTRIES TO INVOKE
                for( x in 0 until gameManager?.invoke?.size!!) {
                    //setInvokeInDatabase(it.child("/invoke").value.toString())
                    gameManager?.setInvokeInDatabase(gameManager?.invoke!![x])
                }



                answerAdapter.clear()
                selectedAnswer = null
                recyclerview_chat_log.scrollToPosition(adapter.itemCount -1)
            }

        val latestMessageRef = FirebaseDatabase.getInstance().getReference("/ownPlaySettings/${player}/latest-messages/$fromId/$toId")
        latestMessageRef.setValue(chatMessage)

        //println("STARTING provideUserAnswers(). PARAMETERS ARE: " + invoke + " AND: " + toId)

            //provideUserAnswers(invoke, toId)
            provideUserAnswers()

    }

    private fun provideUserAnswers(){
        val player = mAuth.currentUser?.uid
        val answersRef = FirebaseDatabase.getInstance().getReference("/user-messages/0/$toId")
        answersRef.addListenerForSingleValueEvent(object: ValueEventListener{
            override fun onDataChange(p0: DataSnapshot) {
                GlobalScope.launch(){
                    if(isFirstMessage == 1) {
                        delay(3500)
                    }
                    p0.children.forEach {
                        if (gameManager?.invoke!!.contains(it.key)) {
                            val fromId = it.child("/from").value.toString()
                            val toId = it.child("/to").value.toString()
                            val reference = FirebaseDatabase.getInstance()
                                .getReference("/ownPlaySettings/${player}/messages/$toId/$fromId")
                                .push()

                            //DELETE ALL INVOKES FROM CURRENT INVOKE ENTRY
                            gameManager?.clearInvokesFromDatabase(it.key!!)
                            gameManager?.invoke?.clear()

                            //WRITE NEW ENTRIES TO INVOKE
                            for (x in 0 until it.child("/invoke").childrenCount) {
                                gameManager?.invoke?.add(
                                    it.child("/invoke").child("/$x").value.toString()!!
                                )
                            }

                            for (x in 0 until gameManager?.invoke!!.size) {
                                //setInvokeInDatabase(it.child("/invoke").value.toString())
                                //setInvokeInDatabase(invoke[x]) TODO: new Invokes
                                gameManager?.setInvokeInDatabase(
                                    it.child("/invoke").child("/$x").value.toString()!!
                                )

                            }

                            val actualMessage = gameManager?.changePlayerNameInText(it.child("/text").value.toString())
                            val chatMessage = ChatMessage(fromId, it.key.toString(), actualMessage!!, toId)

                            reference.setValue(chatMessage)
                                .addOnSuccessListener {
                                    // edittext_chat_log.text.clear()
                                    recyclerview_chat_log.scrollToPosition(adapter.itemCount - 1)

                                    val latestMessageRef = FirebaseDatabase.getInstance()
                                        .getReference("/ownPlaySettings/${player}/latest-messages/$toId/$fromId")
                                    latestMessageRef.setValue(chatMessage)
                                    shake(this@MessagesActivity)
                                }
                        } else {
                            //  Log.d("keine antwort", it.key)
                        }
                    }
                    isFirstMessage = 1
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
                        val node = it.key
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
                            answerAdapter.add(UserAnswer(gameManager?.changePlayerNameInText(it.value.toString())!!, _invoke, node!!))
                        }
                    }}

                recyclerview_chat_log.scrollToPosition(adapter.itemCount -1)

                answerAdapter.setOnItemClickListener { item, view ->
                    val answerItem = item as UserAnswer
                    selectedAnswer = answerItem
                    //view.setBackgroundColor(Color.parseColor("#404040"))

                    //println("klick"+ selectedAnswer?.invoke.toString())
                }
            }
            override fun onCancelled(error: DatabaseError) {
            }
        })
    }

    private fun shake(context: Context){
        (context.getSystemService(VIBRATOR_SERVICE) as Vibrator).vibrate(500)
        println("Vibration executed.")
    }

    private fun provideChat(){
        val chatRef = FirebaseDatabase.getInstance().getReference("/user-messages/0/1")
        chatRef.addListenerForSingleValueEvent(object: ValueEventListener{
            override fun onDataChange(p0: DataSnapshot) {
                p0.children.forEach {
                    val chatMessage = ChatMessage(it.child("/from").value.toString(),it.child("/invoke").value.toString(),it.child("/text").value.toString(),it.child("/to").value.toString())

                    if(chatMessage != null){
                        if(chatMessage.from == "0") {
                            adapter.add(ChatToItem(chatMessage.text, toUser!!))
                        }else {
                            adapter.add(ChatFromItem(chatMessage.text))
                        }
                    }
                    recyclerview_chat_log.scrollToPosition(adapter.itemCount -1)
                }
            }
            override fun onCancelled(p0: DatabaseError) {
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

class UserAnswer(val text: String, val invoke: MutableList<String>, val node: String): Item<ViewHolder>(){
    override fun bind(viewHolder: ViewHolder, position: Int) {
        viewHolder.itemView.answer.text = text
    }
    override fun getLayout(): Int {
        return R.layout.user_answers
    }
}

