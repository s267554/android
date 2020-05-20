package it.polito.mad.team19lab2.ui

import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.Button
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.card.MaterialCardView
import it.polito.mad.team19lab2.R
import it.polito.mad.team19lab2.data.ItemModel
import it.polito.mad.team19lab2.viewModel.ItemListViewModel
import kotlinx.android.synthetic.main.fragment_onsale_list.*


class OnSaleListFragment: Fragment(),SearchDialogFragment.NoticeDialogListener{

    private var onSaleArray = ArrayList<ItemModel>()
    private var onSearchArray = ArrayList<ItemModel>()
    //private var onClearArray = ArrayList<ItemModel>()
    private val itemListVm: ItemListViewModel by viewModels()
    lateinit var liveList:MutableLiveData<List<ItemModel>>
    lateinit var r: RecyclerView
    lateinit var emptyList:TextView
    lateinit var searchCard: MaterialCardView
    lateinit var clearBotton: Button
    lateinit var modifyBotton: Button
    private var search:Boolean =false
    private var query_title:String?=null
    private var query_cat:String?=null
    private var query_min:String?=null
    private var query_max:String?=null
    private var query_loc:String?=null

    //private var clear=false
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_onsale_list, container, false)
        r= view.findViewById(R.id.onsale_list)
        emptyList=view.findViewById<TextView>(R.id.empty_list)
        clearBotton=view.findViewById(R.id.button_clear_filter)
        modifyBotton=view.findViewById(R.id.modify_query_button)
        searchCard=view.findViewById(R.id.materialCardView2)
        modifyBotton.setOnClickListener {
            if(search) {
                val d = SearchDialogFragment(query_title,query_cat,query_min,query_max,query_loc);
                d.show(childFragmentManager, "search dialog")
            }
        }
        clearBotton.setOnClickListener {
            Log.d("vittoz",search.toString())
            if(search){
                query_title=null
                query_cat=null
                query_loc=null
                query_min=null
                query_max=null
                search=false
                //clear=true
                itemListVm.getAllItems().observe(viewLifecycleOwner, Observer {
                    if(!search){

                        if(!search){
                            onSaleArray= it as ArrayList<ItemModel>
                            Log.d("vittoz", "clear observer: "+onSaleArray.toString())
                            with(r) {
                                layoutManager = LinearLayoutManager(context)
                                adapter = OnSaleRecyclerViewAdapter(
                                    onSaleArray
                                )
                            }

                            if (onSaleArray.count() != 0)
                                emptyList.visibility = View.GONE
                            else
                                emptyList.visibility = View.VISIBLE
                        }
                    }
                })
                searchCard.visibility=View.GONE
            }
        }
        itemListVm.getAllItems().observe(viewLifecycleOwner, Observer {
            if(!search && it!=null){
                onSaleArray= it as ArrayList<ItemModel>
                Log.d("vittoz", "all observer: "+onSaleArray.toString())
            with(r) {
                layoutManager = LinearLayoutManager(context)
                adapter = OnSaleRecyclerViewAdapter(
                    onSaleArray
                )
            }

            if (onSaleArray.count() != 0)
                emptyList.visibility = View.GONE
            }
            else
                emptyList.visibility = View.VISIBLE
        })
        return view
    }



    override fun onCreateOptionsMenu(menu: Menu, menuInflater: MenuInflater) {
        menuInflater.inflate(R.menu.on_sale_fragment_menu, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if(item.itemId == R.id.search_item_action){
            val d=SearchDialogFragment();
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
        Log.d(" vittoz query param", title+"  "+category+"  "+minprice+"  "+maxprice+"  "+location)
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
        if(!category.isNullOrEmpty()) {
            searchCard.findViewById<TextView>(R.id.textViewCat).text = "IN $category"
            search=true
        }
        if(!location.isNullOrEmpty()) {
            searchCard.findViewById<TextView>(R.id.textViewloc).text = "@ $location"
            search=true
        }
        var s=""
        if(!m1.isNullOrEmpty()){
            s=resources.getString(R.string.from)+m1+"€ "
        }
        else
            s=resources.getString(R.string.from)+"0€ "
        if(!m2.isNullOrEmpty()){
            s=s+resources.getString(R.string.upTo)+m2+ "€"
        }
        else
            s=s+resources.getString(R.string.upUnbound)
        if(search) {
            query_title=title
            query_cat=category
            query_loc=location
            query_min=m1
            query_max=m2

            searchCard.findViewById<TextView>(R.id.textViewPrice).text = s

            itemListVm.getQueryItems(title, category, m1, m2, location)
                .observe(viewLifecycleOwner, Observer {
                    if (search) {
                        onSearchArray = it as ArrayList<ItemModel>
                        Log.d("vittoz", "search observer: " + onSearchArray.toString())
                        with(r) {
                            layoutManager = LinearLayoutManager(context)
                            adapter = OnSaleRecyclerViewAdapter(
                                onSearchArray
                            )
                        }
                        if (onSearchArray.count() != 0)
                            emptyList.visibility = View.GONE
                        else
                            emptyList.visibility = View.VISIBLE
                    }
                })
            searchCard.visibility = View.VISIBLE
        }
        //r.adapter?.notifyDataSetChanged()
    }

    override fun onDialogNegativeClick(dialog: DialogFragment) {
    }


}












