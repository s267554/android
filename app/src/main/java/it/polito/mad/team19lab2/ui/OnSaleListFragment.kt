package it.polito.mad.team19lab2.ui

import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.Button
import android.widget.TextView
import androidx.core.os.bundleOf
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout.OnRefreshListener
import com.google.android.material.card.MaterialCardView
import it.polito.mad.team19lab2.R
import it.polito.mad.team19lab2.data.ItemModel
import it.polito.mad.team19lab2.viewModel.ItemListViewModel
import kotlinx.android.synthetic.main.fragment_onsale_list.*

class OnSaleListFragment: Fragment(),SearchDialogFragment.NoticeDialogListener{

    private var onSaleArray = ArrayList<ItemModel>()
    //private var onSearchArray = ArrayList<ItemModel>()
    //private var onClearArray = ArrayList<ItemModel>()
    lateinit var live_items: MutableLiveData<List<ItemModel>>
    lateinit var recyclerView: RecyclerView
    lateinit var adapter: OnSaleRecyclerViewAdapter
    lateinit var swipeRefreshLayout: SwipeRefreshLayout
    private val itemListVm: ItemListViewModel by viewModels()
    lateinit var emptyList:TextView
    lateinit var searchCard: MaterialCardView
    private lateinit var clearBotton: Button
    private lateinit var modifyBotton: Button
    private var search:Boolean =false
    private var queryTitle:String?=null
    private var queryCat: Int = -1
    private var queryMin:String?=null
    private var queryMax:String?=null
    private var queryLoc:String?=null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        emptyList=view.findViewById(R.id.empty_list)
        clearBotton=view.findViewById(R.id.button_clear_filter)
        modifyBotton=view.findViewById(R.id.modify_query_button)
        searchCard=view.findViewById(R.id.materialCardView2)
        modifyBotton.setOnClickListener {
            if(search) {
                val d = SearchDialogFragment(queryTitle,queryCat,queryMin,queryMax,queryLoc)
                d.show(childFragmentManager, "search dialog")
            }
        }
        clearBotton.setOnClickListener {
            if(search){
                queryTitle=null
                queryCat=-1
                queryLoc=null
                queryMin=null
                queryMax=null
                search=false

                //clear=true
                //adapter=OnSaleRecyclerViewAdapter(onSaleArray)
                //recyclerView.adapter=adapter
                //adapter.onNewData(onSaleArray)
                //swipeRefreshLayout.isRefreshing=false
                live_items.removeObservers(viewLifecycleOwner)
                live_items=itemListVm.getAllItems()
                live_items.observe(viewLifecycleOwner, Observer {
                    //if(!search){
                    if(!search && it!=null) {
                        onSaleArray = it as ArrayList<ItemModel>
                        adapter.onNewData(onSaleArray);
                        if (onSaleArray.count() != 0)
                            emptyList.visibility = View.GONE
                        else
                            emptyList.visibility = View.VISIBLE
                        searchCard.visibility=View.GONE
                    }

                })
            }
        }

        adapter=OnSaleRecyclerViewAdapter(onSaleArray)
        swipeRefreshLayout=view.findViewById(R.id.swipeRefreshLayoutOnSale)
        recyclerView=view.findViewById(R.id.onsale_list)
        recyclerView.adapter=adapter
        recyclerView.layoutManager=LinearLayoutManager(context)
        swipeRefreshLayout.setOnRefreshListener(myRefreshListener())
        live_items=itemListVm.getAllItems()
        if(savedInstanceState!=null&&savedInstanceState.containsKey("title")){
                queryTitle = savedInstanceState.getString("title")
                queryLoc = savedInstanceState.getString("location")
                queryMin = savedInstanceState.getString("min")
                queryMax = savedInstanceState.getString("max")
                queryCat = savedInstanceState.getInt("category", -1)
                search = true
                var c: String? = null
                if (queryCat != -1)
                    c = resources.getStringArray(R.array.categories)[queryCat]
                onDialogPositiveClick(queryTitle, c, queryMin, queryMax, queryLoc)
        }
        else {
            live_items.removeObservers(viewLifecycleOwner)
            live_items.observe(viewLifecycleOwner, Observer {
                if (it != null) {
                    if(!search) {
                        onSaleArray = it as ArrayList<ItemModel>
                        adapter.onNewData(onSaleArray);
                        if (onSaleArray.count() != 0)
                            emptyList.visibility = View.GONE
                        else
                            emptyList.visibility = View.VISIBLE
                    }
                    else{
                        var cat  = ""
                        if(queryCat!=-1) {
                            cat = resources.getStringArray(R.array.categories)[queryCat]
                        }
                        onDialogPositiveClick(queryTitle, cat, queryMin, queryMax, queryLoc)
                    }
                }
            })
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_onsale_list, container, false)
        return view
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        if(search){
            val b=bundleOf("category" to queryCat,"title" to queryTitle,"minprice" to queryMin,"maxprice" to queryMax,"location" to queryLoc)
            outState.putAll(b)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, menuInflater: MenuInflater) {
        menuInflater.inflate(R.menu.on_sale_fragment_menu, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if(item.itemId == R.id.search_item_action){
            val d=SearchDialogFragment()
            d.show(childFragmentManager,"search dialog")
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onDialogPositiveClick(
        title: String?,
        category: String?,
        minprice: String?,
        maxprice: String?,
        location: String?
    ) {
        var m1=minprice
        var m2=maxprice
        if(!minprice.isNullOrEmpty())
            if(minprice.toInt()<=0)
                m1=null
            else
                search=true
        if(!maxprice.isNullOrEmpty())
            if(maxprice.toInt()>=999999)
                m2=null
            else
                search=true
        if(!title.isNullOrEmpty()){
            searchCard.findViewById<TextView>(R.id.textViewTitle).text= "'$title'"
            search=true
        }
        else
            searchCard.findViewById<TextView>(R.id.textViewTitle).setText(R.string.any_title)
        if(!category.isNullOrEmpty()) {
            searchCard.findViewById<TextView>(R.id.textViewCat).text = "IN $category"
            search=true
        }
        else
            searchCard.findViewById<TextView>(R.id.textViewCat).setText(R.string.any_category)
        if(!location.isNullOrEmpty()) {
            searchCard.findViewById<TextView>(R.id.textViewloc).text = "@ $location"
            search=true
        }
        else
            searchCard.findViewById<TextView>(R.id.textViewloc).setText(R.string.any_location)

        var s: String
        s = if(!m1.isNullOrEmpty()){
            resources.getString(R.string.from)+m1+"€ "
        } else
            resources.getString(R.string.from)+"0€ "
        if(!m2.isNullOrEmpty()){
            s=s+resources.getString(R.string.upTo)+m2+ "€"
        }
        else
            s += resources.getString(R.string.upUnbound)
        val catIndex = resources.getStringArray(R.array.categories).indexOf(category)
        if(search) {
            queryTitle=title
            queryCat= catIndex
            queryLoc=location
            queryMin=m1
            queryMax=m2
            searchCard.findViewById<TextView>(R.id.textViewPrice).text = s
            adapter = OnSaleRecyclerViewAdapter(onSaleArray)
            recyclerView.adapter=adapter
            //adapter.onNewData(onSaleArray)
            live_items.removeObservers(viewLifecycleOwner)
            live_items=itemListVm.getQueryItems(title, catIndex, m1, m2, location)
            live_items.observe(viewLifecycleOwner, Observer {
                    if (search) {
                        Log.d("xxxxxx", "query items Observer")
                        onSaleArray = it as ArrayList<ItemModel>
                        adapter.onNewData(onSaleArray)
                        if (onSaleArray.count() != 0)
                            emptyList.visibility = View.GONE
                        else
                            emptyList.visibility = View.VISIBLE
                    }
                })
            searchCard.visibility = View.VISIBLE
        }

        else
            searchCard.visibility = View.GONE
        //r.adapter?.notifyDataSetChanged()
    }

    override fun onDialogNegativeClick(dialog: DialogFragment) {
    }

    inner class myRefreshListener:SwipeRefreshLayout.OnRefreshListener {
        override fun onRefresh() {
//            itemListVm.getAllItems().observe(viewLifecycleOwner, Observer {
//                if(!search && it!=null){
//                    onSaleArray= it as ArrayList<ItemModel>
//                    adapter.onNewData(onSaleArray);
//                }
//                if (onSaleArray.count() != 0)
//                    emptyList.visibility = View.GONE
//                else
//                    emptyList.visibility = View.VISIBLE
//            })
            swipeRefreshLayout.isRefreshing=false
        }
    }
}












