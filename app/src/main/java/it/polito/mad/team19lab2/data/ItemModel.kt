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
    //var sold: Boolean,
    var state: String,
    var userId: String,
    var expireDatestamp: Timestamp
) : Parcelable{
    constructor() : this("","","","","","","","", 0.0F, "Available", "",Timestamp(0,0))
}