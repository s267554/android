package it.polito.mad.team19lab2.repository

import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

class ItemListRepository {

    val TAG = "ITEM_REPOSITORY"
    var firestoreDB = FirebaseFirestore.getInstance()
    val myId=FirebaseAuth.getInstance()!!.uid!!;

    fun getMyItems(): Query {
        return firestoreDB.collection("items")
            .whereEqualTo("userId", myId)
    }
    fun getItemsWithQuery(
        category: String?=null,
        minprice: String?=null,
        maxprice: String?=null,
        location: String?=null
    ): Query {
        var q:Query=firestoreDB.collection("items")
        if(!minprice.isNullOrEmpty())
            q=q.whereGreaterThanOrEqualTo("price", minprice.toInt());
        if(!maxprice.isNullOrEmpty())
            q=q.whereLessThanOrEqualTo("price", maxprice.toInt());
        if(!category.isNullOrEmpty())
            q=q.whereEqualTo("category", category);
        if(!location.isNullOrEmpty())
            q=q.whereEqualTo("location", location);
        q=q.whereEqualTo("state","Available")
        return q
    }
}