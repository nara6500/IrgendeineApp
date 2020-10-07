package com.example.irgendeineapp

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
class User(val user_id: String, val user_name:String, val user_photo:String): Parcelable {
    constructor() : this ("","","")
}

