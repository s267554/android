package it.polito.mad.team19lab2.repository

import android.util.Log
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import it.polito.mad.team19lab2.data.UserModel

class UserRepository {

    val TAG = "USER_REPOSITORY"
    var firestoreDB = FirebaseFirestore.getInstance()
    var user = FirebaseAuth.getInstance().currentUser

    fun saveUser(user: UserModel): Task<Void> {
        var documentReference = firestoreDB.collection("utenti").document(user.id)
        return documentReference.set(user)
    }

    fun getUser(): DocumentReference {
        var documentReference = firestoreDB.collection("utenti").document(user!!.uid)
        Log.d(TAG, documentReference.toString())
        return documentReference
    }

    fun createUser(): DocumentReference {
        var documentReference = firestoreDB.collection("utenti").document(user!!.uid)
        documentReference.set(UserModel(user!!.email.toString(), user!!.uid))
        return documentReference
    }

}