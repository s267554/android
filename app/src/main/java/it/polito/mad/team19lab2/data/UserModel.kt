package it.polito.mad.team19lab2.data

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class UserModel(var email: String, var fullname : String,
                     var nickname: String, var location: String,
                     var interests: List<Int>,  var rating: Float, var imagePath: String, var id: String,
                     var notificationToken: String?, var notificationTokens: ArrayList<String>?
) : Parcelable{
    constructor(email : String, id: String): this(email, "", "", "", listOf<Int>(), 0F, "", id, "", ArrayList())
    constructor(): this("", "", "", "", listOf<Int>(), 0F, "", "", "", ArrayList())
}