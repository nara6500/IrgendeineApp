package com.example.irgendeineapp


import android.annotation.SuppressLint
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.ViewHolder
import kotlinx.android.synthetic.main.activity_main.*


class  MainActivity : AppCompatActivity() {

    lateinit var mAuth: FirebaseAuth

    companion object{
        var currentUser: User? = null
        val TAG = "LatestMessages"
    }


    @SuppressLint("ResourceAsColor")
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        mAuth = FirebaseAuth.getInstance()
        Log.d("Auth", mAuth.toString())

        recyclerview_latest_messages.adapter = adapter


        chats_button.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }
        contacts_button.setOnClickListener {
            val intent = Intent(this, ContactActivity::class.java)
            startActivity(intent)
        }
        notes_button.setOnClickListener {
            val intent = Intent(this, NotesActivity::class.java)
            startActivity(intent)
        }

        chats_button.setBackgroundColor(R.color.colorPrimaryDark)

        adapter.setOnItemClickListener { item, view ->

            val intent =Intent(this, MessagesActivity::class.java )
            val row = item as LatestMessageRow

            intent.putExtra(ContactActivity.USER_KEY, row.chatPartnerUser)

            startActivity(intent)

        }

        listenForLatestMessages()
/*
        val adapter = GroupAdapter<ViewHolder>()
        adapter.add(UserItem2())
        chat_List.adapter = adapter*/
    }

    override fun onStart() {
        super.onStart()
        val  currentUser = mAuth.currentUser


        if (mAuth.currentUser == null) {
            startActivity(Intent(this, PhoneAuthentication::class.java))
        }else {
            Toast.makeText(this, "Already Signed in :)", Toast.LENGTH_LONG).show()
        }
    }


    val adapter = GroupAdapter<ViewHolder>()

    val latestMessagesMap = HashMap<String, ChatMessage>()

    private fun refreshRecyclerViewMessages(){
        adapter.clear()
        latestMessagesMap.values.forEach{
            adapter.add(LatestMessageRow(it))
        }
    }

    private fun listenForLatestMessages(){
        val fromId ="0"
        val user = mAuth.currentUser?.uid
        val ref = FirebaseDatabase.getInstance().getReference("/ownPlaySettings/${user}/latest-messages/$fromId")
        ref.addChildEventListener(object: ChildEventListener{
            override fun onChildAdded(p0: DataSnapshot, p1: String?) {
                val chatMessage = p0.getValue(ChatMessage::class.java) ?: return
                adapter.add(LatestMessageRow(chatMessage))
            }
            override fun onChildChanged(p0: DataSnapshot, p1: String?) {
                val chatMessage = p0.getValue(ChatMessage::class.java) ?: return

                latestMessagesMap[p0.key!!] = chatMessage
                refreshRecyclerViewMessages()

                //adapter.add(LatestMessageRow(chatMessage))
            }
            override fun onCancelled(p0: DatabaseError) {

            }
            override fun onChildMoved(p0: DataSnapshot, p1: String?) {

            }
            override fun onChildRemoved(p0: DataSnapshot) {

            }
        })


    }


}





