package it.polito.mad.team19lab2

import android.content.Context
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.Snackbar

import org.json.JSONObject

/**
 * A fragment representing a list of Items.
 * Activities containing this fragment MUST implement the
 * [ItemListFragment.OnListFragmentInteractionListener] interface.
 */
class ItemListFragment : Fragment() {

    // TODO: Customize parameters
    private var columnCount = 1

    //private var listener: OnListFragmentInteractionListener? = null

    //my add
    private lateinit var dataset: Array<ItemInfo>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        arguments?.let {
            columnCount = it.getInt(ARG_COLUMN_COUNT)
        }

        val sharedPref = activity?.getSharedPreferences("it.polito.mad.team19lab2item_id", Context.MODE_PRIVATE)

        var item = ItemInfo()
        if (sharedPref != null) {
            val currentItem = sharedPref.getString("item", "notFound")
            if (currentItem != "notFound") {
                val jo = JSONObject(currentItem)
                item.title = jo.get("TITLE").toString()
                item.description = jo.get("DESCRIPTION").toString()
                item.location = jo.get("LOCATION").toString()
                item.price = jo.get("PRICE").toString().toFloat()
                item.expiryDate = jo.get("DATE").toString()
                item.category = jo.get("CATEGORY").toString()
                //   item.path = jo.get("PATH").toString()
                Log.d("debuglista", jo.toString())
            }
        }
        dataset = Array(1, {i -> item})

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_item_list, container, false)

        // che schifo di riga
        val recycler: RecyclerView = view.findViewById(R.id.list)

        // Set the adapter
        if (recycler is RecyclerView) {
            with(recycler) {
                layoutManager = when {
                    columnCount <= 1 -> LinearLayoutManager(context)
                    else -> GridLayoutManager(context, columnCount)
                }
                adapter = MyItemRecyclerViewAdapter(dataset /*,listener*/)
            }
        }

        // altro bello schifo
        val fab: FloatingActionButton = view.findViewById(R.id.fab)
        fab.setOnClickListener { view ->
            Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                .setAction("Action", null).show()
        }
        return view
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
/*        if (context is OnListFragmentInteractionListener) {
            listener = context
        } else {
            throw RuntimeException(context.toString() + " must implement OnListFragmentInteractionListener")
        }*/
    }

    override fun onDetach() {
        super.onDetach()
        //listener = null
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     *
     *
     * See the Android Training lesson
     * [Communicating with Other Fragments](http://developer.android.com/training/basics/fragments/communicating.html)
     * for more information.
     */
/*    interface OnListFragmentInteractionListener {
        // TODO: Update argument type and name
        fun onListFragmentInteraction(item: DummyItem?)
    }*/

    companion object {

        // TODO: Customize parameter argument names
        const val ARG_COLUMN_COUNT = "column-count"

        // TODO: Customize parameter initialization
        @JvmStatic
        fun newInstance(columnCount: Int) =
            ItemListFragment().apply {
                arguments = Bundle().apply {
                    putInt(ARG_COLUMN_COUNT, columnCount)
                }
            }
    }
}
