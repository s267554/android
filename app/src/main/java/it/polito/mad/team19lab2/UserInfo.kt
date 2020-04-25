package it.polito.mad.team19lab2

import android.graphics.Bitmap

class UserInfo {

    var img:Int= R.drawable.avatar_foreground
    var fullname:String =""
    var nickname:String=""
    var email_address:String=""
    var location_area:String=""
    var interests:MutableList<Int> = arrayListOf()
    var rating:Float = 0F
    var image: Bitmap? = null
}