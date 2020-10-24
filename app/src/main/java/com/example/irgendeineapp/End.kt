package com.example.irgendeineapp

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.example.irgendeineapp.GameManager.Companion.ENDING

class End : AppCompatActivity()  {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val ending = intent.getStringExtra(ENDING)

        if(ending == "Ende0"){
            setContentView(R.layout.activity_ende0)
        }
        if(ending == "Ende1"){
            setContentView(R.layout.activity_ende1)
        }
        if(ending == "Ende2"){
            setContentView(R.layout.activity_ende2)
        }
        if(ending == "Ende3"){
            setContentView(R.layout.activity_ende3)
        }


    }

    fun startEnding(ending:String){


    }
    }