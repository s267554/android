package it.polito.mad.team19lab2

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

private var onSaleArray = ArrayList<ItemInfo>()

class OnSaleListFragment: Fragment(){

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        //Creation of 20 Fake Item
        val i: Int=0
        for(i in 0..20){
            val item = ItemInfo()
            var p = 10F
            item.title = "title item $i"
            p = p + i
            item.price = p
            onSaleArray.add(item)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_onsale_list, container, false)
        val r: RecyclerView = view.findViewById(R.id.onsale_list)

        with(r){
            layoutManager = LinearLayoutManager(context)
            adapter = OnSaleRecyclerViewAdapter(onSaleArray)
        }

        if(onSaleArray.count() != 0)
            view.findViewById<TextView>(R.id.empty_list)?.visibility = View.INVISIBLE
        return view
    }
}












