package it.polito.mad.team19lab2.repository

import android.util.Log
import com.google.android.gms.tasks.Task
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import it.polito.mad.team19lab2.data.ItemModel
import it.polito.mad.team19lab2.data.UserShortModel

class ItemRepository {
    val TAG="ITEM_REPOSITORY"
    var firestoreDB = FirebaseFirestore.getInstance()

    fun saveBuyer(item: ItemModel, usm: UserShortModel): Task<Void> {
        var documentReference = firestoreDB.collection("items").document(item.id).collection("buyers").document(usm.id)
        return documentReference.set(usm)
    }

    fun saveItem(item: ItemModel): Task<Void> {
        var documentReference = firestoreDB.collection("items").document(item.id)
        return documentReference.set(item)
    }

    fun sellItem(item: ItemModel): Task<Void>{
        var documentReference = firestoreDB.collection("items").document(item.id)
        return documentReference.set(item)
    }

    fun getItem(id: String): DocumentReference {
        var documentReference = firestoreDB.collection("items").document(id)
        return documentReference
    }

    fun getInterestedUsers(id : String): Query {
        var q:Query=firestoreDB.collection("items").document(id).collection("users")
        return q
    }

    fun getBuyers(id : String): Query {
        var q:Query=firestoreDB.collection("items").document(id).collection("buyers")
        return q
    }

    fun removeInterestedUser(usm: UserShortModel, id: String) {
        firestoreDB.collection("items").document(id)
            .collection("users").document(usm.id).delete()
        return
    }

    fun addInterestedUser(usm: UserShortModel, id: String)  {
        var documentReference = firestoreDB.collection("items").document(id)
            .collection("users").document(usm.id)
        documentReference.set(usm)
        return
    }
}