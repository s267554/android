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

class ItemListFragment : Fragment() {

    private var dataset = ArrayList<ItemModel>()
    private val itemListVm: ItemListViewModel by viewModels()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_item_list, container, false)

        val recycler: RecyclerView = view.findViewById(R.id.list)


        //todo change with getMyItems!!
        itemListVm.getMyItems().observe(viewLifecycleOwner, androidx.lifecycle.Observer {
            dataset= it as ArrayList<ItemModel>
            with(recycler) {
                layoutManager = LinearLayoutManager(context)
                adapter =
                    MyItemRecyclerViewAdapter(dataset)
            }
            if(dataset.count() != 0)
                view.findViewById<TextView>(R.id.empty_list)?.visibility = View.INVISIBLE
        })

        val fab: FloatingActionButton = view.findViewById(R.id.fab)
        val bundle = bundleOf(
            "item_id1" to "-1","deep_link" to true,
            "sourceFragment" to R.id.action_nav_my_advertisement_to_nav_edit_item
        )
        fab.setOnClickListener {  it.findNavController().navigate(R.id.action_nav_my_advertisement_to_nav_edit_item, bundle)
        }

        return view
    }
}
