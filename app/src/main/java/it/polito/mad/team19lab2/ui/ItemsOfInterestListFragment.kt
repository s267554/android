package it.polito.mad.team19lab2.ui

import android.content.ClipData
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.os.bundleOf
import androidx.fragment.app.viewModels
import androidx.lifecycle.observe
import androidx.navigation.findNavController
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.auth.FirebaseAuth
import it.polito.mad.team19lab2.R
import it.polito.mad.team19lab2.data.ItemModel
import it.polito.mad.team19lab2.data.ItemShortModel
import it.polito.mad.team19lab2.data.UserShortModel
import it.polito.mad.team19lab2.viewModel.ItemListViewModel
import it.polito.mad.team19lab2.viewModel.ItemViewModel
import it.polito.mad.team19lab2.viewModel.UserViewModel

import java.util.*
import kotlin.collections.ArrayList

class ItemsOfInterestListFragment : Fragment() {

    private var dataset = ArrayList<ItemShortModel>()
    private val userListVm: UserViewModel by viewModels()
    lateinit var swipeRefreshLayout: SwipeRefreshLayout

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_items_of_interest, container, false)

        val recycler: RecyclerView = view.findViewById(R.id.items_of_interest_list)
        userListVm.getItemsOfInterest().observe(viewLifecycleOwner, androidx.lifecycle.Observer {
            dataset= it as ArrayList<ItemShortModel>

            with(recycler) {
                layoutManager = LinearLayoutManager(context)
                adapter = ItemsOfInterestRecyclerViewAdapter(dataset)
            }
            if(dataset.count() != 0)
                view.findViewById<TextView>(R.id.empty_list)?.visibility = View.GONE
        })
        return view
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        swipeRefreshLayout=view.findViewById(R.id.swipeRefreshLayoutOnSale)
        swipeRefreshLayout.setOnRefreshListener(myRefreshListener())
    }
    inner class myRefreshListener: SwipeRefreshLayout.OnRefreshListener {
        override fun onRefresh() {
            swipeRefreshLayout.isRefreshing=false
        }
    }

}


