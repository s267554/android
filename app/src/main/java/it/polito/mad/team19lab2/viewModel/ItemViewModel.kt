package it.polito.mad.team19lab2.viewModel


import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.firestore.EventListener
import com.google.firebase.firestore.Query
import it.polito.mad.team19lab2.data.ItemModel
import it.polito.mad.team19lab2.data.UserShortModel
import it.polito.mad.team19lab2.repository.ItemRepository
import it.polito.mad.team19lab2.repository.UserRepository

class ItemViewModel : ViewModel() {
    private var itemRepository = ItemRepository()
    private var userRepository = UserRepository()
    private var TAG = "ITEM_VIEW_MODEL"
    private var liveItem: MutableLiveData<ItemModel> = MutableLiveData()
    private var liveUsers : MutableLiveData<List<UserShortModel>> = MutableLiveData()
    private var userList : MutableList<UserShortModel> = mutableListOf()

    fun saveItem(item: ItemModel) {
        itemRepository.saveItem(item).addOnFailureListener {
            Log.e(TAG, "Failed to save item! ${item.id}")
        }
    }

    fun getItem(id: String): MutableLiveData<ItemModel> {
        itemRepository.getItem(id).addSnapshotListener(EventListener { value, e ->
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
        q.addSnapshotListener(EventListener { value, e ->
            if (e != null) {
                Log.d(TAG, "Listen failed.", e)
                liveUsers.value = null
                return@EventListener
            }
            for (doc in value!!) {
                Log.d(TAG,doc.toString())
                val user = doc.toObject(UserShortModel::class.java)
                userList.add(user)
            }
            val tmpArray=ArrayList(userList)
            liveUsers.value = tmpArray
            userList.clear()
        })
    }

    fun addInterestedUser(id: String) {
        val query = userRepository.getProfile().get()
        query.addOnSuccessListener {
            if (it != null) {
                Log.d(TAG, "value: $it")
                val usm = it.toObject(UserShortModel::class.java)
                Log.d(TAG, "usm = ${usm.toString()}")
                if (usm != null) {
                    itemRepository.addInterestedUser(usm, id )
                }
            }
        }
    }

    fun removeInterestedUser(id: String){
        val query = userRepository.getProfile().get()
        query.addOnSuccessListener {
            if (it != null) {
                val usm = it.toObject(UserShortModel::class.java)
                if (usm != null) {
                    itemRepository.removeInterestedUser(usm, id)
                }
            }
        }
    }
}