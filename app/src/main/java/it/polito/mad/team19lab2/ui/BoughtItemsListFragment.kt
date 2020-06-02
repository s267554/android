package it.polito.mad.team19lab2.ui

import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.os.bundleOf
import androidx.fragment.app.viewModels
import androidx.navigation.findNavController
import com.google.android.material.floatingactionbutton.FloatingActionButton
import it.polito.mad.team19lab2.R
import it.polito.mad.team19lab2.data.ItemModel
import it.polito.mad.team19lab2.viewModel.ItemListViewModel

import java.util.*

class BoughtItemsListFragment : Fragment() {

    private var dataset = ArrayList<ItemModel>()
    private val itemListVm: ItemListViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_bought_items_list, container, false)

        val recycler: RecyclerView = view.findViewById(R.id.bought_items_list)
        itemListVm.getBoughtItems().observe(viewLifecycleOwner, androidx.lifecycle.Observer {
            dataset= it as ArrayList<ItemModel>
            with(recycler) {
                layoutManager = LinearLayoutManager(context)
                adapter = OnSaleRecyclerViewAdapter(dataset)
            }
            if(dataset.count() != 0)
                view.findViewById<TextView>(R.id.empty_list)?.visibility = View.GONE
        })

        return view
    }
}
