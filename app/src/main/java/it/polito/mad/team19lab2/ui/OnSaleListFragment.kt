package it.polito.mad.team19lab2.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import it.polito.mad.team19lab2.R
import it.polito.mad.team19lab2.data.ItemModel
import it.polito.mad.team19lab2.viewModel.ItemListViewModel


class OnSaleListFragment: Fragment(){

    private var onSaleArray = ArrayList<ItemModel>()
    private val itemListVm: ItemListViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
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
}












