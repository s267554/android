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
            .whereGreaterThan("userId", myId).whereGreaterThan("userId", myId).whereEqualTo("state", "Available")
    }
    fun getLowerItems(): Query {
        return firestoreDB.collection("items")
            .whereLessThan("userId", myId).whereGreaterThan("userId", myId).whereEqualTo("state", "Available")
    }

    fun getMyItems(): Query {
        return firestoreDB.collection("items")
            .whereEqualTo("userId", myId)
    }

    fun getItemsWithQuery(
        title: String?,
        category: String?,
        minprice: String?,
        maxprice: String?,
        location: String?
    ): Query {
        var q:Query=firestoreDB.collection("items")
        if(!minprice.isNullOrEmpty())
            q=q.whereGreaterThan("price", minprice);
        if(!maxprice.isNullOrEmpty())
            q=q.whereLessThan("price", maxprice);
        if(!category.isNullOrEmpty())
            q=q.whereEqualTo("category", category);
        if(!location.isNullOrEmpty())
            q=q.whereEqualTo("location", location);
        return q
    }

    fun getLowerItemsWithQuery(
        title: String?,
        category: String?,
        minprice: String?,
        maxprice: String?,
        location: String?
    ): Query {

        var q=firestoreDB.collection("items")
            .whereLessThan("userId", myId)
        if(!title.isNullOrEmpty())
            q=q.whereEqualTo("title", title);
        if(!minprice.isNullOrEmpty())
                q=q.whereGreaterThan("price", minprice);
        if(!maxprice.isNullOrEmpty())
            q=q.whereLessThan("price", maxprice);
        if(!category.isNullOrEmpty())
            q=q.whereEqualTo("category", category);
        if(!location.isNullOrEmpty())
            q=q.whereEqualTo("location", location);
        return q
    }
}