package it.polito.mad.team19lab2.viewModel

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.EventListener
import it.polito.mad.team19lab2.data.UserModel
import it.polito.mad.team19lab2.repository.UserRepository

class UserViewModel: ViewModel() {
    val TAG = "USER_VIEW_MODEL"
    var userRepository = UserRepository()
    var liveUser: MutableLiveData<UserModel> = MutableLiveData()

    fun saveUser(user: UserModel){
        userRepository.saveProfile(user).addOnFailureListener {
            Log.e(TAG, "Failed to save User!")
        }
    }

    fun getUser(userId: String): MutableLiveData<UserModel> {
        userRepository.getUser(userId).addSnapshotListener(EventListener<DocumentSnapshot> { value, e ->
            if (e != null) {
                Log.e(TAG, "Listen failed.", e)
                liveUser.value = null
                return@EventListener
            }
            if (value != null) {
                liveUser.value = value.toObject(UserModel::class.java)
            }
        })
        return liveUser
    }

    fun getOrCreateUser(): MutableLiveData<UserModel>{
        userRepository.getProfile().addSnapshotListener(EventListener<DocumentSnapshot> { value, e ->
            if (e != null) {
                Log.e(TAG, "Listen failed.", e)
                liveUser.value = null
                return@EventListener
            }
            if (value != null) {
                if(value.exists())
                    liveUser.value = value.toObject(UserModel::class.java)
                else{
                    userRepository.createUser().addSnapshotListener(EventListener<DocumentSnapshot> { value, e ->
                        if (e != null) {
                            Log.e(TAG, "Listen failed.", e)
                            liveUser.value = null
                            return@EventListener
                        }
                        if (value != null) {
                            liveUser.value = value.toObject(UserModel::class.java)
                        }
                    })
                }
            }
        })
        return liveUser
    }
}