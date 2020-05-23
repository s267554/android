package it.polito.mad.team19lab2.data

import android.os.Parcelable
import com.google.firebase.Timestamp
import kotlinx.android.parcel.Parcelize

@Parcelize
data class ItemModel (
    var id: String,
    var title: String,
    var description: String,
    var category: String,
    var subcategory: String,
    var imagePath: String,
    var location: String,
    var expiryDate: String,
    var price: Float,
    var state: String,
    var userId: String,
    var expireDatestamp: Timestamp
) : Parcelable{

    fun compareTo(other: ItemModel): Int {
        val x=id==other.id&&title==other.title&&price==other.price&&imagePath==other.imagePath
        if (x)
            return 0
        else
            return 1
    }

    constructor() : this("","","","","","","","", 0.0F, "Available", "",Timestamp(0,0))
}