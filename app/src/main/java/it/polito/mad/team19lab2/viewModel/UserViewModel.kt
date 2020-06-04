package it.polito.mad.team19lab2.viewModel

import android.content.ClipData
import android.icu.util.TimeUnit
import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.android.gms.tasks.Tasks
import com.google.firebase.firestore.EventListener
import com.google.firebase.firestore.Query
import it.polito.mad.team19lab2.data.ItemShortModel
import it.polito.mad.team19lab2.data.UserModel
import it.polito.mad.team19lab2.repository.ItemRepository
import it.polito.mad.team19lab2.repository.UserRepository
import it.polito.mad.team19lab2.ui.ItemsOfInterestListFragment
import java.util.concurrent.ExecutionException
import java.util.concurrent.TimeoutException

class UserViewModel: ViewModel() {
    private val TAG = "USER_VIEW_MODEL"
    private var userRepository = UserRepository()
    private var itemRepository = ItemRepository()
    private var itemList : MutableList<ItemShortModel> = mutableListOf()
    private var liveUser: MutableLiveData<UserModel> = MutableLiveData()
    private var liveItems : MutableLiveData<List<ItemShortModel>> = MutableLiveData()

    fun updateRate(userId:String,rate:Float){
        userRepository.getUser(userId).addSnapshotListener(EventListener { value, e ->
            if (e != null) {
                Log.e(TAG, "Listen failed.", e)
                liveUser.value = null
                return@EventListener
            }
            if (value != null) {
                val u=value.toObject(UserModel::class.java)
                if(u!=null) {
                    u.rating = rate
                    userRepository.saveUser(u).addOnFailureListener {
                        Log.e(TAG, "Failed to save User!")
                    }
                }
            }
        })
    }
    fun saveUser(user: UserModel){
        userRepository.saveProfile(user).addOnFailureListener {
            Log.e(TAG, "Failed to save User!")
        }
    }

    fun getUser(userId: String): MutableLiveData<UserModel> {
        userRepository.getUser(userId).addSnapshotListener(EventListener { value, e ->
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
        userRepository.getProfile().addSnapshotListener(EventListener { value, e ->
            if (e != null) {
                Log.e(TAG, "Listen failed.", e)
                liveUser.value = null
                return@EventListener
            }
            if (value != null) {
                if(value.exists())
                    liveUser.value = value.toObject(UserModel::class.java)
                else{
                    userRepository.createUser().addSnapshotListener(EventListener { v, err ->
                        if (err != null) {
                            Log.e(TAG, "Listen failed.", err)
                            liveUser.value = null
                            return@EventListener
                        }
                        if (v != null) {
                            liveUser.value = v.toObject(UserModel::class.java)
                        }
                    })
                }
            }
        })
        return liveUser
    }

    fun getItemsOfInterest(): MutableLiveData<List<ItemShortModel>>{
        itemList.clear()
        takeLiveItemsFromQuery(userRepository.getItemsOfInterest())
        return liveItems
    }

    fun addInterestedItem(idItem: String){
        val query = itemRepository.getItem(idItem).get()
        query.addOnSuccessListener {
            if (it != null) {
                val ism = it.toObject(ItemShortModel::class.java)
                if (ism != null) {
                    userRepository.addInterestedUser(ism)
                }
            }
        }
    }

    fun removeInterestedItem(idItem: String){
        val query = itemRepository.getItem(idItem).get()
        query.addOnSuccessListener {
            if (it != null) {
                val ism = it.toObject(ItemShortModel::class.java)
                if (ism != null) {
                    userRepository.removeInterestedUser(ism)
                }
            }
        }
    }

    private fun takeLiveItemsFromQuery(q: Query){
        q.addSnapshotListener(EventListener { value, e ->
            if (e != null) {
                liveItems.value = null
                return@EventListener
            }
            for (doc in value!!) {
                val item = doc.toObject(ItemShortModel::class.java)
                itemList.add(item)
            }
            val tmpArray=ArrayList(itemList)
            liveItems.value = tmpArray
            itemList.clear()
        })
    }
}