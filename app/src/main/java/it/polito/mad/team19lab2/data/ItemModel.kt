package it.polito.mad.team19lab2.data

import android.os.Parcelable
import com.google.firebase.Timestamp
import kotlinx.android.parcel.Parcelize

@Parcelize
data class ItemModel (
    var id: String,
    var title: String,
    var description: String,
    //var category: String,
    var category: Int,
    //var subcategory: String,
    var subcategory: Int,
    var imagePath: String,
    var location: String,
    var expiryDate: String,
    var price: Float,
    //var state: String,
    var state: Int,
    var userId: String,
    var expireDatestamp: Timestamp,
    var buyerId: String,
    var reviewed:Boolean=false,
    var imageVersion:Int=0
) : Parcelable{
    fun compareTo(other: ItemModel): Int {
        val x=id==other.id&&title==other.title&&price==other.price&&imageVersion==other.imageVersion
        if (x)
            return 0
        else
            return 1
    }

    constructor() : this("","","",-1,-1,"","","", 0.0F, 1, "",Timestamp(0,0), "")
}