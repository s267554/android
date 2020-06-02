package it.polito.mad.team19lab2.data
import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class ItemShortModel(var id : String, var title: String, var price: Float, var imagePath: String) : Parcelable {
    constructor(): this("", "",0.0F,"" )
}