package com.example.irgendeineapp

import android.content.Intent
import android.os.Parcelable
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat.startActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.getValue
import kotlinx.android.parcel.Parcelize

class GameManager {
    lateinit var mAuth: FirebaseAuth

    val invoke = mutableListOf<String>()
    var playerName: String = ""
    var userId : String = ""
    var context : MessagesActivity

    constructor(activity: MessagesActivity){
        mAuth = FirebaseAuth.getInstance()
        context = activity

    }
    companion object{
        val ENDING = "ENDING"
        var chatIsVisible = mutableListOf(0,0,1,0,0,0,0,0,0)
    }

    fun changePlayerNameInText(text: String): String{

        return text.replace("SPIELERNAME".toRegex(), "$playerName")
    }

    fun checkIfAction(_singleInvoke : String){

        // Spieler soll auf Alex Chat klicken, danach Nachricht SP05
        // in getInvoke augerufen??? -> funktioniert aber

        if(_singleInvoke == "AN05_wait"){
            println(_singleInvoke)
            println("userid"+userId)
            if(this.userId == "1"){
                for(x in 0 until invoke.size){
                    if(invoke[x].contains(_singleInvoke)){
                        clearInvokesFromDatabase(invoke[x])
                        setInvokeInDatabase("SP05")
                    }
                }
            }
        }



        if(_singleInvoke == "BE04"){
            // Der Spieler muss herausfinden, was Alex an dem Abend anhatte, bevor die Geschichte weiter geht.
            // Er kann entweder die Infos an Benji weitergeben (SP??, SP??) oder aber er sagt, er weiß nicht was Alex anhatte. Dann sagt Benji ihm, dass er ihm erst helfen kann,
            // wenn er die Infos hat und das Gespräch geht erst weiter, wenn der Spieler mit den anderen Charakteren geredet hat und zumindest eine der Infos rausgefunden hat.
        }






    }

    fun checkIfEnd(_singleInvoke: String){
        if(_singleInvoke == "Ende0") {

            val intent = Intent(context,End::class.java)
            intent.putExtra(ENDING, _singleInvoke )
            context.startActivity(intent)

        }
        if(_singleInvoke == "Ende1") {
            val intent = Intent(context,End::class.java)
            intent.putExtra(ENDING, _singleInvoke )
            context.startActivity(intent)


        }
        if(_singleInvoke == "Ende2") {
            val intent = Intent(context,End::class.java)
            intent.putExtra(ENDING, _singleInvoke )
            context.startActivity(intent)
        }
        if(_singleInvoke == "Ende3") {
            val intent = Intent(context,End::class.java)
            intent.putExtra(ENDING, _singleInvoke )
            context.startActivity(intent)
        }
    }

    fun checkForVisibility(_singleInvoke:String){
        if(_singleInvoke == "AN05_wait") {
            chatIsVisible[1] = 1
        }
        if(_singleInvoke == "Timo"){
            chatIsVisible[3] = 1


            for(x in 0 until invoke.size){
                if(invoke[x].contains(_singleInvoke)){
                    clearInvokesFromDatabase(invoke[x])
                    setInvokeInDatabase("SP47")
                }
            }


        }
        if(_singleInvoke == "BE01") {
            chatIsVisible[4] = 1
        }
        if(_singleInvoke == "AN07") {
            chatIsVisible[5] = 1
        }
        if(_singleInvoke == "Lina"){
            chatIsVisible[6] = 1


            for(x in 0 until invoke.size){
                if(invoke[x].contains(_singleInvoke)){
                    clearInvokesFromDatabase(invoke[x])
                    setInvokeInDatabase("SP82")
                }
            }


        }
        if(_singleInvoke == "Luis"){
            chatIsVisible[7] = 1

            for(x in 0 until invoke.size){
                if(invoke[x].contains(_singleInvoke)){
                    clearInvokesFromDatabase(invoke[x])
                    setInvokeInDatabase("SP70")
                }
            }

        }
        if(_singleInvoke == "SP99") {
            chatIsVisible[8] = 1
        }





    }

    fun getPlayerName(){
        val player = mAuth.currentUser?.uid
        val invokeRef =  FirebaseDatabase.getInstance().getReference("/ownPlaySettings/${player}/playerSettings/userName")
        invokeRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(p0: DataSnapshot) {
                playerName = p0.getValue<String>().toString()

            }

            override fun onCancelled(error: DatabaseError) {
            }
        })
    }




     fun setInvokeInDatabase(_invoke: String){
        val player = mAuth.currentUser?.uid
        val invokeRef = FirebaseDatabase.getInstance().getReference("/ownPlaySettings/${player}/playerSettings")
        val invokeAdd = invokeRef.child("/invoke").push()
        invokeAdd.setValue(_invoke)
         checkIfEnd(_invoke)
         checkForVisibility(_invoke)

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
                    checkIfAction(invokeValue)
                    println("erster invoke:" + invoke.toString())
                }
            }

            override fun onCancelled(error: DatabaseError) {
            }
        })
    }

    fun handleDeleteNodes(_selectedInvoke:String){
        // referenz des gesetzten invokes
        val player = mAuth.currentUser?.uid
        val deleteRef = FirebaseDatabase.getInstance().getReference("/user-messages/0/0")
        deleteRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(p0: DataSnapshot) {
                p0.children.forEach {
                    if (it.key == _selectedInvoke){
                        val deleteNodeList = it.child("/deleteNode")
                        for(x in 0 until deleteNodeList?.childrenCount){
                            val invokeValue = deleteNodeList.child("/$x").value.toString()
                            clearInvokesFromDatabase(invokeValue)
                        }

                    }

                }
            }

            override fun onCancelled(p0: DatabaseError) {
            }
        })
    }

    fun clearInvokesFromDatabase(invokeToDelete:String) {
        val player = mAuth.currentUser?.uid
        /*val invokeRef = FirebaseDatabase.getInstance().getReference("/ownPlaySettings/${player}/playerSettings/invoke")
         invokeRef.removeValue()*/
        val deleteRef = FirebaseDatabase.getInstance()
            .getReference("/ownPlaySettings/${player}/playerSettings/invoke")
        deleteRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(p0: DataSnapshot) {
                p0.children.forEach {
                    if (it.value == invokeToDelete) {
                        var deleteKey = it.key!!
                        deleteRef.child("/$deleteKey").removeValue()
                    }
                }
            }

            override fun onCancelled(p0: DatabaseError) {
            }
        })
    }

}