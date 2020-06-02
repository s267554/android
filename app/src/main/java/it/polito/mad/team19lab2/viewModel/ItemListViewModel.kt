package it.polito.mad.team19lab2.viewModel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.EventListener
import it.polito.mad.team19lab2.data.ItemModel
import it.polito.mad.team19lab2.repository.ItemListRepository
import com.google.firebase.firestore.Query


class ItemListViewModel : ViewModel() {
    private var itemListRepository = ItemListRepository()
    private var TAG = "ITEM_LIST_VIEW_MODEL"
    private var liveItems : MutableLiveData<List<ItemModel>> = MutableLiveData()
    private var itemsList : MutableList<ItemModel> = mutableListOf()
    private val myId= FirebaseAuth.getInstance().uid!!

    fun getAllItems(): MutableLiveData<List<ItemModel>>{
        itemsList.clear()
        takeOtherFromQuery(itemListRepository.getItemsWithQuery(),null)
        return liveItems
    }

    fun getMyItems(): MutableLiveData<List<ItemModel>>{
        itemsList.clear()
        takeLiveItemsFromQuery(itemListRepository.getMyItems())
        return liveItems
    }

    fun getBoughtItems(): MutableLiveData<List<ItemModel>>{
        itemsList.clear()
        takeLiveItemsFromQuery(itemListRepository.getBoughtItems())
        return liveItems
    }

    private fun takeLiveItemsFromQuery(q:Query){
        q.addSnapshotListener(EventListener { value, e ->
            if (e != null) {
                liveItems.value = null
                return@EventListener
            }
            for (doc in value!!) {
                val item = doc.toObject(ItemModel::class.java)
                itemsList.add(item)
            }
            val tmpArray=ArrayList(itemsList)
            liveItems.value = tmpArray
            itemsList.clear()
        })
    }

    private fun takeOtherFromQuery(q:Query,title: String?){
        q.addSnapshotListener(EventListener { value, e ->
            if (e != null) {
                liveItems.value = null
                return@EventListener
            }
            for (doc in value!!) {
                val item = doc.toObject(ItemModel::class.java)

                if(item.userId!=myId&&item.expireDatestamp>= Timestamp.now()) {
                    if (title.isNullOrEmpty()){
                        itemsList.add(item)
                    }
                    else if(item.title.contains(title,true)){
                        itemsList.add(item)
                    }
                }
            }
            val tmpArray=ArrayList(itemsList)
            liveItems.value = tmpArray
            itemsList.clear()
                    })
    }


    fun getQueryItems(title: String?, category: Int, minprice: String?,maxprice: String?, location: String?): MutableLiveData<List<ItemModel>> {
        itemsList.clear()
        takeOtherFromQuery(itemListRepository.getItemsWithQuery(category,minprice,maxprice,location),title)
        return liveItems
    }


}