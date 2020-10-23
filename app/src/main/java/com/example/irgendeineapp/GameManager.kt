package com.example.irgendeineapp

import android.os.Parcelable
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.android.parcel.Parcelize

class GameManager {
    lateinit var mAuth: FirebaseAuth

    val invoke = mutableListOf<String>()

    constructor(){
        mAuth = FirebaseAuth.getInstance()
    }


    fun actionOnSpecialInvoke(singleInvoke: String){
        if(singleInvoke == "AN01"){

        }
    }

     fun setInvokeInDatabase(_invoke: String){
        val player = mAuth.currentUser?.uid
        val invokeRef = FirebaseDatabase.getInstance().getReference("/ownPlaySettings/${player}/playerSettings")
        val invokeAdd = invokeRef.child("/invoke").push()
        invokeAdd.setValue(_invoke)
        //println("SETTING INVOKES IN FIREBASE NOW TO: " + _invoke)
        //invokeRef.child("/invoke").setValue(_invoke)
        //invoke = _invoke
    }

     fun getInvokeFromDatabase(){
        val player = mAuth.currentUser?.uid
        val invokeRef =  FirebaseDatabase.getInstance().getReference("/ownPlaySettings/${player}/playerSettings/invoke")
        invokeRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(p0: DataSnapshot) {
                //loop over all children in path "/invoke"
                p0.children.forEach{
                    val invokeValue = it.value.toString()
                    //invoke = invokeValue
                    invoke.add(invokeValue)
                    println("erster invoke:" + invoke.toString())
                }
            }

            override fun onCancelled(error: DatabaseError) {
            }
        })
    }

     fun clearInvokesFromDatabase(){
        val player = mAuth.currentUser?.uid
        val invokeRef = FirebaseDatabase.getInstance().getReference("/ownPlaySettings/${player}/playerSettings/invoke")
        invokeRef.removeValue()
    }
}