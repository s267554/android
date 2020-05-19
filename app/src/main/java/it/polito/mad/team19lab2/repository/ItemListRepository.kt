package it.polito.mad.team19lab2.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

class ItemListRepository {

    val TAG = "ITEM_REPOSITORY"
    var firestoreDB = FirebaseFirestore.getInstance()
    val myId=FirebaseAuth.getInstance()!!.uid!!;

    fun getHigherItems(): Query {
        return firestoreDB.collection("items")
            .whereGreaterThan("userId", myId)
    }
    fun getLowerItems(): Query {
        return firestoreDB.collection("items")
            .whereLessThan("userId", myId)
    }

    fun getMyItems(): Query {
        return firestoreDB.collection("items")
            .whereEqualTo("userId", myId)
    }

    fun getHigherItemsWithQuery(
        title: String?,
        category: String?,
        price: String?,
        location: String?
    ): Query {
        var q=firestoreDB.collection("items")
            .whereGreaterThan("userId", myId)
        if(!title.isNullOrEmpty())
            q.whereEqualTo("title", title);
        if(!price.isNullOrEmpty())
            q.whereEqualTo("price", price);
        if(!category.isNullOrEmpty())
            q.whereEqualTo("category", category);
        if(!location.isNullOrEmpty())
            q.whereEqualTo("location", location);
        return q
    }

    fun getLowerItemsWithQuery(
        title: String?,
        category: String?,
        price: String?,
        location: String?
    ): Query {
        return firestoreDB.collection("items")
            .whereLessThan("userId", myId)
    }
}