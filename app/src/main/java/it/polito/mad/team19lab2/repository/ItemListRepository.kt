package it.polito.mad.team19lab2.repository

import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.FirebaseFirestore

class ItemListRepository {

    val TAG = "ITEM_REPOSITORY"
    var firestoreDB = FirebaseFirestore.getInstance()

    fun getAllItems(): CollectionReference {
        return firestoreDB.collection("items")
    }
}