package com.example.irgendeineapp


import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.squareup.picasso.Picasso
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.GroupieViewHolder
import com.xwray.groupie.Item
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.activity_messages.view.*
import kotlinx.android.synthetic.main.user_row_message.view.*

class  MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)


        /*val adapter = GroupAdapter<GroupieViewHolder>()


        adapter.add(UserItem())
        adapter.add(UserItem())

        chat_List.adapter = adapter*/

        fetchUsers()
    }

    private fun fetchUsers(){
        val ref = FirebaseDatabase.getInstance().getReference("/users")
        ref.addListenerForSingleValueEvent(object: ValueEventListener {
            override fun onCancelled(p0: DatabaseError) {

            }

            override fun onDataChange(p0: DataSnapshot) {
                val adapter = GroupAdapter<GroupieViewHolder>()

                p0.children.forEach {
                    Log.d("NewMessage", it.toString())
                    val user = it.getValue(User::class.java)
                    if (user != null) {
                        adapter.add(UserItem(user))
                    }

                }

                chat_List.adapter = adapter
            }
        })
    }
}


class UserItem(val user:User): Item<GroupieViewHolder>(){
    override fun bind(viewHolder: GroupieViewHolder, position: Int) {
        // will be called in our list for each user objject later on...
        viewHolder.itemView.message_Button.textView.text = user.username

        Picasso.get().load(user.profileImageUrl).into(viewHolder.itemView.message_Button_image)
    }

    override fun getLayout(): Int {
        return R.layout.user_row_message
    }
}

class User(val uid: String, val username:String, val profileImageUrl: String){
    constructor() : this ("","","")
}


