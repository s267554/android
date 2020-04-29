package it.polito.mad.team19lab2

import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.os.bundleOf
import androidx.navigation.findNavController
import com.google.android.material.floatingactionbutton.FloatingActionButton

import org.json.JSONObject
import java.io.File
import java.util.*

class ItemListFragment : Fragment() {

    private var dataset = ArrayList<ItemInfo>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val sharedPref = activity?.getSharedPreferences("it.polito.mad.team19lab2.items", Context.MODE_PRIVATE)


        if (sharedPref != null) {
            for((key,value) in sharedPref.all){
                val item = ItemInfo()
                item.itemId = key
                val jo = JSONObject(value as String)
                item.title = jo.get("TITLE").toString()
                item.description = jo.get("DESCRIPTION").toString()
                item.location = jo.get("LOCATION").toString()
                item.price = jo.get("PRICE").toString().toFloat()
                item.expiryDate = jo.get("DATE").toString()
                item.category = jo.get("CATEGORY").toString()

                val file = File(activity?.applicationContext?.filesDir, "$key.png")
                if(file.exists()) {
                    item.image = MediaStore.Images.Media.getBitmap(activity?.contentResolver, Uri.fromFile(file))
                }
                dataset.add(item)
            }
        }

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_item_list, container, false)

        val recycler: RecyclerView = view.findViewById(R.id.list)

        // Set the adapter
        with(recycler) {
            layoutManager = LinearLayoutManager(context)
            adapter = MyItemRecyclerViewAdapter(dataset /*,listener*/)
        }

        val fab: FloatingActionButton = view.findViewById(R.id.fab)
        fab.setOnClickListener {  it.findNavController().navigate(R.id.action_nav_home_to_nav_edit_item, bundleOf("item_id1" to UUID.randomUUID().toString(),"deep_link" to true))
        }

        if(dataset.count() != 0)
            view.findViewById<TextView>(R.id.empty_list)?.visibility = View.INVISIBLE

        return view
    }
}
