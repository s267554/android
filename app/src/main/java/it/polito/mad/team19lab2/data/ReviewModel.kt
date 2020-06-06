package it.polito.mad.team19lab2.data

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class ReviewModel(
    var itemId: String,
    var userId: String,
    var comment: String,
    var rate: Float,
    var userNick: String,
    var buyerId: String="",
    var buyerNick:String=""
): Parcelable {
    constructor():this ("","","", 0.0F,"")
}