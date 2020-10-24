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

        //AN09, AN10, AN11, AN17, AN18, AN21, AN29, BE14, JA08, JA09, JA11, JA12, JA13, LI10, LI11, LI12, LI16, LU11, LU12, LU13, MA08, SP60, SP97, SP107, TI17
        // laufen ins leere, bis neue informationen da sind  (muss das abgefangen werden?)

        if(_singleInvoke == "BE04"){
            // Der Spieler muss herausfinden, was Alex an dem Abend anhatte, bevor die Geschichte weiter geht.
            // Er kann entweder die Infos an Benji weitergeben (SP??, SP??) oder aber er sagt, er weiß nicht was Alex anhatte. Dann sagt Benji ihm, dass er ihm erst helfen kann,
            // wenn er die Infos hat und das Gespräch geht erst weiter, wenn der Spieler mit den anderen Charakteren geredet hat und zumindest eine der Infos rausgefunden hat.
        }

        if(_singleInvoke == "BE13"){
           // Der Spieler hat erstmal alles von Benji erfahren und die Geschichte geht in einem anderen Chat weiter.
            // Benji schreibt nur noch eine Nachricht um nett zu sein (Wenn das Probleme macht, kann man die theoretisch auch weglassen,
            // die ist nur schmückend und nicht relevant für die Geschichte).

        }
        if(_singleInvoke == "LU"){
            // Luis gibt dem Spieler eine neue Nummer (Marlene). Ein neuer Chat wird hinzugefügt und der Spieler kann Marlene schreiben.
            // Da könnte als Invoke auch SP99 stehen, das ist die Nachricht, die der Spieler Marlene schicken kann.
        }
        if(_singleInvoke == "SP41"){
           // Der Spieler bekommt die Nummern von Timo und Luis. Beide Chats müssen hinzugefügt werden, bevor der Spieler ihnen schrieben kann.
            // (Nachricht an Timo: SP47, Nachricht an Luis: SP70)
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