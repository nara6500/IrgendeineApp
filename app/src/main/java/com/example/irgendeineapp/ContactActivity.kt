package com.example.irgendeineapp

import android.annotation.SuppressLint
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import androidx.core.app.NavUtils
import com.google.firebase.database.*
import com.squareup.picasso.Picasso
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.Item
import com.xwray.groupie.ViewHolder
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.user_row_contact.view.*


class  ContactActivity : AppCompatActivity() {

    @SuppressLint("ResourceAsColor")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

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

        contacts_button.setBackgroundColor(R.color.colorPrimaryDark)


        fetchUsers()
/*
        val adapter = GroupAdapter<ViewHolder>()
        adapter.add(UserItem2())
        chat_List.adapter = adapter*/
    }





    companion object{
        val USER_KEY = "USER_KEY"
    }


    val adapter = GroupAdapter<ViewHolder>()
    private fun fetchUsers(){
        val ref = FirebaseDatabase.getInstance().getReference("/user")
        Log.d("Ref", ref.toString());
        ref.addListenerForSingleValueEvent(object: ValueEventListener {

            override fun onDataChange(p0: DataSnapshot) {



                p0.children.forEach {

                    Log.d("NewMessage", it.toString())
                    val user = it.getValue(User::class.java)
                    if (user != null && user.user_id !="0") {
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

                recyclerview_latest_messages.adapter = adapter
            }

            override fun onCancelled(p0: DatabaseError) {

            }
        })
    }
}




class UserItem(val user:User): Item<ViewHolder>(){
    override fun bind(viewHolder: ViewHolder, position: Int) {
        // will be called in our list for each user objject later on...
        viewHolder.itemView.user_name.text = user.user_name

        val targetImageView = viewHolder.itemView.user_photo
        Picasso.get().load(user?.user_photo).into(targetImageView)
    }


    override fun getLayout(): Int {
        return R.layout.user_row_contact
    }
}




