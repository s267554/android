package it.polito.mad.team19lab2.viewModel

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.firestore.EventListener
import com.google.firebase.firestore.QuerySnapshot
import it.polito.mad.team19lab2.data.ItemModel
import it.polito.mad.team19lab2.repository.ItemListRepository
import com.google.firebase.firestore.Query


class ItemListViewModel : ViewModel() {
    var itemListRepository = ItemListRepository()
    var TAG = "ITEM_LIST_VIEW_MODEL"
    var liveItems : MutableLiveData<List<ItemModel>> = MutableLiveData()
    private var itemsList : MutableList<ItemModel> = mutableListOf()

    fun getAllItems(): MutableLiveData<List<ItemModel>>{
        itemsList.clear()
        takeLiveItemsFromQuery(itemListRepository.getHigherItems(), false)
        takeLiveItemsFromQuery(itemListRepository.getLowerItems(), true)
        return liveItems
    }

    fun getMyItems(): MutableLiveData<List<ItemModel>>{
        itemsList.clear()
        takeLiveItemsFromQuery(itemListRepository.getMyItems(), true)
        return liveItems
    }

    fun takeLiveItemsFromQuery(q:Query, clear:Boolean){
        q.addSnapshotListener(EventListener<QuerySnapshot> { value, e ->
            if (e != null) {
                Log.d(TAG, "Listen failed.", e)
                liveItems.value = null
                return@EventListener
            }
            for (doc in value!!) {
                Log.d(TAG,doc.toString())
                var item = doc.toObject(ItemModel::class.java)
                itemsList.add(item)
            }
            var tmpArray=ArrayList(itemsList)
            liveItems.value = tmpArray
            if(clear)
                itemsList.clear()
        })
    }

}