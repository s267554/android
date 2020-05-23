package it.polito.mad.team19lab2.repository

import android.util.Log
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import it.polito.mad.team19lab2.data.UserModel

class UserRepository {

    val TAG = "USER_REPOSITORY"
    private var firestoreDB = FirebaseFirestore.getInstance()
    var user = FirebaseAuth.getInstance().currentUser

    fun saveProfile(profile: UserModel): Task<Void> {
        profile.id=user!!.uid
        var documentReference = firestoreDB.collection("utenti").document(profile.id)
        return documentReference.set(profile)
    }

    fun getUser(userId : String): DocumentReference {
        var documentReference = firestoreDB.collection("utenti").document(userId)
        Log.d(TAG, documentReference.toString())
        return documentReference
    }

    fun getProfile() : DocumentReference {
        var documentReference = firestoreDB.collection("utenti").document(user!!.uid)
        Log.d(TAG, documentReference.toString())
        return documentReference
    }

    fun createUser(): DocumentReference {
        var documentReference = firestoreDB.collection("utenti").document(user!!.uid)
        var newUser = UserModel(user!!.email.toString(), user!!.uid)
        newUser.nickname = "avatar${System.currentTimeMillis().toString().subSequence(0,10)}"
        newUser.fullname = user!!.email?.split("@")?.get(0) ?: "fullname"
        documentReference.set(newUser)
        return documentReference
    }

}