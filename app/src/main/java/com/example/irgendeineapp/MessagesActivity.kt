package com.example.irgendeineapp

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.Item
import com.xwray.groupie.ViewHolder
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.activity_main.view.*
import kotlinx.android.synthetic.main.activity_messages.*

class MessagesActivity: AppCompatActivity()  {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_messages)

       // val username = intent.getStringExtra(MainActivity.USER_KEY)
        val user = intent.getParcelableExtra<User>(MainActivity.USER_KEY)

        supportActionBar?.title = user.user_name

        val adapter = GroupAdapter<ViewHolder>()
        adapter.add(ChatToItem())
        adapter.add(ChatFromItem())
        adapter.add(ChatToItem())

        recyclerview_chat_log.adapter = adapter



    }
}

class ChatToItem: Item<ViewHolder>(){
    override fun bind(viewHolder: ViewHolder, position: Int) {

    }
    override fun getLayout(): Int {
        return R.layout.my_message
    }
}


class ChatFromItem: Item<ViewHolder>(){
    override fun bind(viewHolder: ViewHolder, position: Int) {

    }
    override fun getLayout(): Int {
        return R.layout.their_message
    }
}