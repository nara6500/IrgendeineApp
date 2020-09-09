package com.example.irgendeineapp


import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Parcelable
import android.util.Log
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.Item
import com.xwray.groupie.ViewHolder
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.activity_messages.view.*
import kotlinx.android.synthetic.main.user_row_message.view.*


class  MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        fetchUsers()
/*
        val adapter = GroupAdapter<ViewHolder>()
        adapter.add(UserItem2())
        chat_List.adapter = adapter*/
    }

    companion object{
        val USER_KEY = "USER_KEY"
    }

    private fun fetchUsers(){
        val ref = FirebaseDatabase.getInstance().getReference("/user")
        Log.d("Ref", ref.toString());
        ref.addListenerForSingleValueEvent(object: ValueEventListener {

            override fun onDataChange(p0: DataSnapshot) {

                val adapter = GroupAdapter<ViewHolder>()

                p0.children.forEach {

                    Log.d("NewMessage", it.toString())
                    val user = it.getValue(User::class.java)
                    if (user != null) {
                        adapter.add(UserItem(user))
                        Log.d("allright", user.toString())
                    } else
                        Log.d("clear", user.toString())

                }

                adapter.setOnItemClickListener { item, view ->

                    val userItem = item as UserItem

                    val intent =Intent(view.context, MessagesActivity::class.java )
                   // intent.putExtra(USER_KEY, userItem.user.user_name)
                    intent.putExtra(USER_KEY, userItem.user)
                    startActivity(intent)

                }

                chat_List.adapter = adapter
            }

            override fun onCancelled(p0: DatabaseError) {

            }
        })
    }
}

class UserItem2: Item<ViewHolder>(){
    override fun bind(viewHolder: ViewHolder, position: Int) {

    }

    override fun getLayout(): Int {
        return R.layout.user_row_message
    }
}


class UserItem(val user:User): Item<ViewHolder>(){
    override fun bind(viewHolder: ViewHolder, position: Int) {
        // will be called in our list for each user objject later on...
        viewHolder.itemView.user_name.text = user.user_name

       // Picasso.get().load(user.profileImageUrl).into(viewHolder.itemView.message_Button_image)
    }

    override fun getLayout(): Int {
        return R.layout.user_row_message
    }
}




