package it.polito.mad.team19lab2.data
import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class UserShortModel(var id : String, var fullname: String, var nickname: String) : Parcelable {
    constructor(): this("","","")
}