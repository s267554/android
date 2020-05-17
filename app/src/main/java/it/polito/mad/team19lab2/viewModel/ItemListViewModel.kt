package it.polito.mad.team19lab2.viewModel

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.firestore.EventListener
import com.google.firebase.firestore.QuerySnapshot
import it.polito.mad.team19lab2.data.ItemModel
import it.polito.mad.team19lab2.repository.ItemListRepository


class ItemListViewModel : ViewModel() {
    var itemListRepository = ItemListRepository()
    var TAG = "ITEM_LIST_VIEW_MODEL"
    var liveItems : MutableLiveData<List<ItemModel>> = MutableLiveData()

    fun getAllItems(): MutableLiveData<List<ItemModel>>{
        itemListRepository.getAllItems().addSnapshotListener(EventListener<QuerySnapshot> { value, e ->
            if (e != null) {
                 Log.d(TAG, "Listen failed.", e)
                liveItems.value = null
                return@EventListener
            }

            var liveItemsList : MutableList<ItemModel> = mutableListOf()
            for (doc in value!!) {
                Log.d(TAG,doc.toString())
                var item = doc.toObject(ItemModel::class.java)
                liveItemsList.add(item)
            }
            liveItems.value = liveItemsList
        })

        return liveItems
    }

}