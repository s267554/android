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
import it.polito.mad.team19lab2.R
import it.polito.mad.team19lab2.data.ItemModel
import it.polito.mad.team19lab2.viewModel.ItemListViewModel


class OnSaleListFragment: Fragment(),SearchDialogFragment.NoticeDialogListener{

    private var onSaleArray = ArrayList<ItemModel>()
    private var onSearchArray = ArrayList<ItemModel>()
    //private var onClearArray = ArrayList<ItemModel>()
    private val itemListVm: ItemListViewModel by viewModels()
    lateinit var liveList:MutableLiveData<List<ItemModel>>
    lateinit var r: RecyclerView
    lateinit var emptyList:TextView
    lateinit var searchLayout:ConstraintLayout
    lateinit var clearBotton: Button
    private var search:Boolean =false
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
        searchLayout=view.findViewById<ConstraintLayout>(R.id.searchConstraintLayout)
        clearBotton.setOnClickListener {
            Log.d("vittoz",search.toString())
            if(search){
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
                        }
                    }
                })
                searchLayout.visibility=View.GONE
            }
        }
        itemListVm.getAllItems().observe(viewLifecycleOwner, Observer {
            if(!search){
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
        Log.d(" vittoz query param","${title+"  "+category+"  "+minprice+"  "+maxprice+"  "+location}")
        search=true
        var m1=minprice
        var m2=maxprice
        if(!minprice.isNullOrEmpty())
            if(minprice.toInt()<=0)
                m1=null
        if(!maxprice.isNullOrEmpty())
            if(maxprice.toInt()>=999999)
                m2=null

        itemListVm.getQueryItems(title,category,m1,m2,location)
            .observe(viewLifecycleOwner, Observer {
            if(search) {
                onSearchArray = it as ArrayList<ItemModel>
                Log.d("vittoz", "search observer: "+onSearchArray.toString())
                with(r) {
                    layoutManager = LinearLayoutManager(context)
                    adapter = OnSaleRecyclerViewAdapter(
                        onSearchArray
                    )
                }
                if (onSearchArray.count() != 0)
                    emptyList.visibility = View.GONE
            }
            })
        searchLayout.visibility=View.VISIBLE
        //r.adapter?.notifyDataSetChanged()
    }

    override fun onDialogNegativeClick(dialog: DialogFragment) {
    }


}












