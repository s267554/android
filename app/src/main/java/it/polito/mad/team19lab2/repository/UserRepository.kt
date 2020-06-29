package it.polito.mad.team19lab2.repository

import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import it.polito.mad.team19lab2.data.ItemShortModel
import it.polito.mad.team19lab2.data.ReviewModel
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
    fun addReviewToUser(review: ReviewModel, user: UserModel?){
        //add review
        var documentReference = firestoreDB.collection("utenti").document(review.userId)
            .collection("comments").document(review.itemId)
        documentReference.set(review)
        //set item as reviewed
        firestoreDB.collection("items").document(review.itemId).update(mapOf(
            "reviewed" to true))
        //update the user profile
        firestoreDB.collection("utenti").document(review.userId)
        val newRate=((user!!.rating*user!!.numberOfReviews!!)+review.rate)/(user.numberOfReviews+1)
        firestoreDB.collection("utenti").document(review.userId)
            .update(mapOf("numberOfReviews" to user.numberOfReviews+1,"rating" to newRate))

        return
    }
    fun getReviewsOfUser(userId: String):Query{
        var q = firestoreDB.collection("utenti").document(userId).collection("comments")
        return q
    }

}