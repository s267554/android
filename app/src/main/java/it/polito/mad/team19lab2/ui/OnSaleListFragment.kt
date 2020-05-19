package it.polito.mad.team19lab2.ui

import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.TextView
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
    private val itemListVm: ItemListViewModel by viewModels()
    lateinit var liveList:MutableLiveData<List<ItemModel>>
    lateinit var r: RecyclerView
    lateinit var emptyList:TextView
    private var search:Boolean =false
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
        itemListVm.getAllItems().observe(viewLifecycleOwner, Observer {
            if(!search){
                Log.d("vittoz", "all observer: "+it.toString())
                onSaleArray= it as ArrayList<ItemModel>
            with(r) {
                layoutManager = LinearLayoutManager(context)
                adapter = OnSaleRecyclerViewAdapter(
                    onSaleArray
                )
            }

            if (onSaleArray.count() != 0)
                emptyList.visibility = View.INVISIBLE
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
        price: String?,
        location: String?
    ) {
        Log.d(" vittoz query param","${title+"  "+category+"  "+price+"  "+location}")
        search=true
        itemListVm.getQueryItems(title,category,price,location)
            .observe(viewLifecycleOwner, Observer {
            if(search) {
                onSearchArray = it as ArrayList<ItemModel>
                Log.d("vittoz", "search observer: "+it.toString())
                with(r) {
                    layoutManager = LinearLayoutManager(context)
                    adapter = OnSaleRecyclerViewAdapter(
                        onSearchArray
                    )
                }
                if (onSearchArray.count() != 0)
                    emptyList.visibility = View.INVISIBLE
            }
            })
        r.adapter?.notifyDataSetChanged()
    }

    override fun onDialogNegativeClick(dialog: DialogFragment) {
    }


}












