package it.polito.mad.team19lab2.viewModel


import android.util.Log
import android.widget.Toast
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.firestore.EventListener
import com.google.firebase.firestore.Query
import it.polito.mad.team19lab2.data.ItemModel
import it.polito.mad.team19lab2.data.UserShortModel
import it.polito.mad.team19lab2.repository.ItemRepository
import it.polito.mad.team19lab2.repository.UserRepository
import kotlin.coroutines.coroutineContext

class ItemViewModel : ViewModel() {
    private var itemRepository = ItemRepository()
    private var userRepository = UserRepository()
    private var TAG = "ITEM_VIEW_MODEL"
    private var liveItem: MutableLiveData<ItemModel> = MutableLiveData()
    private var liveUsers : MutableLiveData<List<UserShortModel>> = MutableLiveData()
    private var liveBuyers : MutableLiveData<List<UserShortModel>> = MutableLiveData()
    private var userList : MutableList<UserShortModel> = mutableListOf()
    private var buyerList : MutableList<UserShortModel> = mutableListOf()

    fun saveBuyer(item: ItemModel) {
        val query = userRepository.getProfile().get()
        query.addOnSuccessListener {
            if (it != null) {
                val usm = it.toObject(UserShortModel::class.java)
                if (usm != null) {
                    itemRepository.saveBuyer(item, usm).addOnFailureListener {

                    }
                }
            }
        }
    }

    fun saveItem(item: ItemModel){
        itemRepository.saveItem(item).addOnFailureListener {
            Log.e("ERROR", "Error on save item")
        }
    }

    fun sellItem(item: ItemModel){
        itemRepository.sellItem(item).addOnFailureListener {
            Log.e("ERROR", "Error on sell item")
        }
    }

    fun getItem(id: String): MutableLiveData<ItemModel> {
        itemRepository.getItem(id).addSnapshotListener(EventListener { value, e ->
            if (e != null) {
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

    fun getBuyers(id: String):MutableLiveData<List<UserShortModel>>{
        buyerList.clear()
        takeLiveBuyersFromQuery(itemRepository.getBuyers(id))
        return liveBuyers
    }

    private fun takeLiveBuyersFromQuery(q: Query){
        q.addSnapshotListener(EventListener { value, e ->
            if (e != null) {
                liveBuyers.value = null
                return@EventListener
            }
            for (doc in value!!) {
                val user = doc.toObject(UserShortModel::class.java)
                buyerList.add(user)
            }
            val tmpArray=ArrayList(buyerList)
            liveBuyers.value = tmpArray
            buyerList.clear()
        })
    }

    private fun takeLiveUsersFromQuery(q: Query){
        q.addSnapshotListener(EventListener { value, e ->
            if (e != null) {
                liveUsers.value = null
                return@EventListener
            }
            for (doc in value!!) {
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
                val usm = it.toObject(UserShortModel::class.java)
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