package it.polito.mad.team19lab2.ui

import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.viewModels
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import it.polito.mad.team19lab2.R
import it.polito.mad.team19lab2.data.ItemModel
import it.polito.mad.team19lab2.data.ReviewModel
import it.polito.mad.team19lab2.viewModel.ItemListViewModel
import it.polito.mad.team19lab2.viewModel.UserViewModel

import java.util.*

class BoughtItemsListFragment : Fragment(),RateAndCommentDialog.NoticeDialogListener {

    private var dataset = ArrayList<ItemModel>()
    private val itemListVm: ItemListViewModel by viewModels()
    private val userViewModel: UserViewModel by viewModels()
    lateinit var swipeRefreshLayout: SwipeRefreshLayout

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
                adapter = BoughtItemsRecyclerViewAdapter(dataset,childFragmentManager)
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

    override fun onDialogPositiveClick(
        userId: String,
        itemId: String,
        comment: String?,
        rate: Float,
        user_nick: String
    ) {
        var c=""
        if(!comment.isNullOrEmpty())
            c=comment
        val r=ReviewModel(itemId,userId,c,rate,user_nick)
        var u=userViewModel.addReview(r)
    }

    override fun onDialogNegativeClick(dialog: DialogFragment) {
        return;
    }

    inner class myRefreshListener: SwipeRefreshLayout.OnRefreshListener {
        override fun onRefresh() {
            swipeRefreshLayout.isRefreshing=false
        }
    }
}
