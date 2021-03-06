package com.example.irgendeineapp

import android.content.Intent

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.tasks.Task
import com.google.firebase.FirebaseException
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthProvider
import com.google.firebase.database.FirebaseDatabase
import com.xwray.groupie.Item
import com.xwray.groupie.ViewHolder
import kotlinx.android.synthetic.main.activity_messages.*
import kotlinx.android.synthetic.main.activity_phone_authentication.*
import kotlinx.android.synthetic.main.user_answers.view.*


class PhoneAuthentication : AppCompatActivity() {


    lateinit var mAuth: FirebaseAuth
    var contactVisibility: ContactVisibility? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_phone_authentication)
        mAuth = FirebaseAuth.getInstance()
        contactVisibility = ContactVisibility()

        veriBtn.setOnClickListener {
                view: View? -> progress.visibility = View.VISIBLE
            verify ()
        }

    }

    private fun verify () {
         mAuth.signInAnonymously()
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Sign in success, update UI with the signed-in user's information
                    Log.d(MainActivity.TAG, "signInAnonymously:success")
                    val user = mAuth.currentUser?.uid
                    Log.d("userid", user.toString())
                    val userName = userName.text.toString()
                    val ref = FirebaseDatabase.getInstance().getReference("/ownPlaySettings/${user}/playerSettings")

                    val invoke = mutableListOf<String>()
                    invoke.add("AN01")

                    ref.child("/invoke").setValue(invoke)
                   ref.child("/userName").setValue(userName)
                    contactVisibility!!.setChatIsVisibleToFirebase()
                    startActivity(Intent(this, MainActivity::class.java))
                } else {
                    // If sign in fails, display a message to the user.
                    Log.w(MainActivity.TAG, "signInAnonymously:failure", task.exception)
                    Toast.makeText(baseContext, "Authentication failed.",
                        Toast.LENGTH_SHORT).show()

                }

            }



}}

class PlayerSettings(val name: String, val invoke: String){
    constructor() : this("","")
}