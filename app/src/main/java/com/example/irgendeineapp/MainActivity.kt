package com.example.irgendeineapp


import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.google.firebase.database.*
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.Item
import com.xwray.groupie.ViewHolder
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.user_row_message.view.*


class  MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        recyclerview_latest_messages.adapter = adapter
        listenForLatestMessages()

/*
        val adapter = GroupAdapter<ViewHolder>()
        adapter.add(UserItem2())
        chat_List.adapter = adapter*/
    }

    companion object{
        val USER_KEY = "USER_KEY"
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
        val ref = FirebaseDatabase.getInstance().getReference("/latest-messages/$fromId")
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

        adapter.setOnItemClickListener { item, view ->

            val intent =Intent(view.context, MessagesActivity::class.java )
            intent.putExtra(USER_KEY, "")
            startActivity(intent)

        }
    }


}

class LatestMessageRow(val chatMessage: ChatMessage): Item<ViewHolder>(){
    override fun bind(viewHolder: ViewHolder, position: Int) {
        // will be called in our list for each user objject later on...
        viewHolder.itemView.latest_message.text = chatMessage.text

        // Picasso.get().load(user.profileImageUrl).into(viewHolder.itemView.message_Button_image)
    }

    override fun getLayout(): Int {
        return R.layout.user_row_message
    }
}




