package it.polito.mad.team19lab2.repository

import android.util.Log
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import it.polito.mad.team19lab2.data.ItemShortModel
import it.polito.mad.team19lab2.data.UserModel

class UserRepository {

    val TAG = "USER_REPOSITORY"
    private var firestoreDB = FirebaseFirestore.getInstance()
    var user = FirebaseAuth.getInstance().currentUser
    private var myId = FirebaseAuth.getInstance().uid

    fun saveProfile(profile: UserModel): Task<Void> {
        profile.id=user!!.uid
        var documentReference = firestoreDB.collection("utenti").document(profile.id)
        return documentReference.set(profile)
    }
    fun saveUser(profile: UserModel): Task<Void> {
        var documentReference = firestoreDB.collection("utenti").document(profile.id)
        return documentReference.set(profile)
    }

    fun getUser(userId : String): DocumentReference {
        var documentReference = firestoreDB.collection("utenti").document(userId)
        return documentReference
    }

    fun getProfile() : DocumentReference {
        var documentReference = firestoreDB.collection("utenti").document(user!!.uid)
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

    fun getItemsOfInterest(): Query{
        var q = firestoreDB.collection("utenti").document(myId!!).collection("interests")
        return q
    }

    fun addInterestedUser(ism: ItemShortModel){
        var documentReference = firestoreDB.collection("utenti").document(myId!!)
            .collection("interests").document(ism.id)
        documentReference.set(ism)
        return
    }

    fun removeInterestedUser(ism: ItemShortModel){
        firestoreDB.collection("utenti").document(myId!!)
            .collection("interests").document(ism.id).delete()
        return
    }

}