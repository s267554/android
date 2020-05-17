package it.polito.mad.team19lab2.repository

import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.QuerySnapshot

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
}