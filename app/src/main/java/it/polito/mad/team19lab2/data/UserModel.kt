package it.polito.mad.team19lab2.data

import android.provider.ContactsContract

data class UserModel(var email: String, var fullname : String,
                     var nickname: String, var location: String,
                     var interests: List<Int>,  var rating: Float, var imagePath: String){
    constructor(email : String): this(email, "", "", "", listOf<Int>(), 0F, "")
    constructor(): this("", "", "", "", listOf<Int>(), 0F, "")
}