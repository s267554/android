package it.polito.mad.team19lab2.viewModel

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
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
    private var flag=0;
    val myId= FirebaseAuth.getInstance()!!.uid!!;

    fun getAllItems(): MutableLiveData<List<ItemModel>>{
        itemsList.clear()
        takeLiveItemsOnSaleFromQuery(itemListRepository.getHigherItems(),null)
        takeLiveItemsOnSaleFromQuery(itemListRepository.getLowerItems(),null)
        return liveItems
    }

    fun getMyItems(): MutableLiveData<List<ItemModel>>{
        itemsList.clear()
        takeLiveItemsFromQuery(itemListRepository.getMyItems())
        return liveItems
    }

    private fun takeLiveItemsFromQuery(q:Query){
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
            itemsList.clear()
        })
    }

    private fun takeLiveItemsOnSaleFromQuery(q:Query,title: String?){
        q.addSnapshotListener(EventListener<QuerySnapshot> { value, e ->
            if (e != null) {
                Log.d(TAG, "Listen failed.", e)
                liveItems.value = null
                return@EventListener
            }
            Log.d("vittoz",value?.size().toString())
            for (doc in value!!) {
                Log.d("vittoz",doc.toString())
                var item = doc.toObject(ItemModel::class.java)
                if(item.userId!=myId) {
                    if (title.isNullOrEmpty())
                        itemsList.add(item)
                    else if(item.title.contains(title,true))
                        itemsList.add(item)
                }

            }
            var tmpArray=ArrayList(itemsList)
            liveItems.value = tmpArray
            if(flag==1) {
                flag=0
                itemsList.clear()
            }
            else{
                flag=1
            }
        })
    }

    fun getQueryItems(title: String?, category: String?, minprice: String?,maxprice: String?, location: String?): MutableLiveData<List<ItemModel>> {
        itemsList.clear()
        takeLiveItemsOnSaleFromQuery(itemListRepository.getItemsWithQuery(title,category,minprice,maxprice,location),title)
        //takeLiveItemsOnSaleFromQuery(itemListRepository.getLowerItemsWithQuery(title,category,minprice,maxprice,location))
        return liveItems
    }


}