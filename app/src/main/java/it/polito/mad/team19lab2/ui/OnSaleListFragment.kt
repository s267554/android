package it.polito.mad.team19lab2.ui

import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.TextView
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import it.polito.mad.team19lab2.R
import it.polito.mad.team19lab2.data.ItemModel
import it.polito.mad.team19lab2.viewModel.ItemListViewModel


class OnSaleListFragment: Fragment(),SearchDialogFragment.NoticeDialogListener{

    private var onSaleArray = ArrayList<ItemModel>()
    private val itemListVm: ItemListViewModel by viewModels()

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
        val r: RecyclerView = view.findViewById(R.id.onsale_list)

        itemListVm.getAllItems().observe(viewLifecycleOwner, androidx.lifecycle.Observer {
            onSaleArray= it as ArrayList<ItemModel>
            with(r) {
                layoutManager = LinearLayoutManager(context)
                adapter = OnSaleRecyclerViewAdapter(
                    onSaleArray
                )
            }

            if (onSaleArray.count() != 0)
                view.findViewById<TextView>(R.id.empty_list)?.visibility = View.INVISIBLE
        })

        return view
    }

    override fun onCreateOptionsMenu(menu: Menu, menuInflater: MenuInflater) {
        menuInflater.inflate(R.menu.item_edit_menu, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if(item.itemId == R.id.save_item_action){
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
        itemListVm.getQueryItems(title,category,price,location)
    }

    override fun onDialogNegativeClick(dialog: DialogFragment) {
        TODO("Not yet implemented")
    }


}












