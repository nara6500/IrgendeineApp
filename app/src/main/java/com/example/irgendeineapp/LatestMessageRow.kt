package com.example.irgendeineapp

import android.util.Log
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.squareup.picasso.Picasso
import com.xwray.groupie.Item
import com.xwray.groupie.ViewHolder
import kotlinx.android.synthetic.main.user_row_message.view.*

class LatestMessageRow(val chatMessage: ChatMessage): Item<ViewHolder>(){
    var chatPartnerUser:User? = null

    override fun bind(viewHolder: ViewHolder, position: Int) {
        // will be called in our list for each user object later on...
        viewHolder.itemView.latest_message.text = chatMessage.text

        val chatPartnerId: String
        if(chatMessage.from == "0"){
            chatPartnerId = chatMessage.to
        } else{
            chatPartnerId = chatMessage.from
        }

        val ref = FirebaseDatabase.getInstance().getReference("/user/$chatPartnerId")
        ref.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(p0: DataSnapshot) {
                chatPartnerUser =p0.getValue(User::class.java)
                viewHolder.itemView.user_name.text = chatPartnerUser?.user_name
                Picasso.get().load(chatPartnerUser?.user_photo).into(viewHolder.itemView.user_photo)

            }
            override fun onCancelled(p0: DatabaseError) {

            }
        })

    }

    override fun getLayout(): Int {
        return R.layout.user_row_message
    }
}
