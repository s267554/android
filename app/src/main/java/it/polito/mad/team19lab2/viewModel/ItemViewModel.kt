package it.polito.mad.team19lab2.viewModel


import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.EventListener
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.QuerySnapshot
import it.polito.mad.team19lab2.data.ItemModel
import it.polito.mad.team19lab2.data.UserShortModel
import it.polito.mad.team19lab2.repository.ItemRepository
import it.polito.mad.team19lab2.repository.UserRepository

class ItemViewModel : ViewModel() {
    var itemRepository = ItemRepository()
    var userRepository = UserRepository()
    var TAG = "ITEM_VIEW_MODEL"
    var liveItem: MutableLiveData<ItemModel> = MutableLiveData()
    var liveUsers : MutableLiveData<List<UserShortModel>> = MutableLiveData()
    private var userList : MutableList<UserShortModel> = mutableListOf()

    fun saveItem(item: ItemModel) {
        itemRepository.saveItem(item).addOnFailureListener {
            Log.e(TAG, "Failed to save item! ${item.id}")
        }
    }

    fun getItem(id: String): MutableLiveData<ItemModel> {
        itemRepository.getItem(id).addSnapshotListener(EventListener<DocumentSnapshot> { value, e ->
            if (e != null) {
                Log.e(TAG, "Listen failed.", e)
                liveItem.value = null
                return@EventListener
            }
            if (value != null) {
                liveItem.value = value.toObject(ItemModel::class.java)
            }
        })
        return liveItem
    }

    fun getInterestedUsers(id:String): MutableLiveData<List<UserShortModel>>{
        userList.clear()
        takeLiveUsersFromQuery(itemRepository.getInterestedUsers(id))
        return liveUsers
    }

    private fun takeLiveUsersFromQuery(q: Query){
        q.addSnapshotListener(EventListener<QuerySnapshot> { value, e ->
            if (e != null) {
                Log.d(TAG, "Listen failed.", e)
                liveUsers.value = null
                return@EventListener
            }
            for (doc in value!!) {
                Log.d(TAG,doc.toString())
                var user = doc.toObject(UserShortModel::class.java)
                userList.add(user)
            }
            var tmpArray=ArrayList(userList)
            liveUsers.value = tmpArray
            userList.clear()
        })
    }

    fun addInterestedUser(iid: String) {
        // TODO: move some to repository?
        // TODO: better way to get my own UserShortModel
        // TODO: switch to coroutines maybe?
        val query = userRepository.getProfile().get()
        query.addOnSuccessListener {
            if (it != null) {
                Log.d(TAG, "value: $it")
                val usm = it.toObject(UserShortModel::class.java)
                Log.d(TAG, "usm = ${usm.toString()}")
                if (usm != null) {
                    itemRepository.firestoreDB.collection("items").document(iid)
                        .collection("users").add(usm)
                }
            }
        }
    }

}