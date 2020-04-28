package it.polito.mad.team19lab2

import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.core.os.bundleOf
import androidx.navigation.findNavController


//import it.polito.mad.team19lab2.ItemListFragment.OnListFragmentInteractionListener

import kotlinx.android.synthetic.main.fragment_item.view.*

/**
 * [RecyclerView.Adapter] that can display a [DummyItem] and makes a call to the
 * specified [OnListFragmentInteractionListener].
 * TODO: Replace the implementation with code for your data type.
 */
class MyItemRecyclerViewAdapter(
    private val mValues: ArrayList<ItemInfo>
    //,
    //private val mListener: OnListFragmentInteractionListener?
) : RecyclerView.Adapter<MyItemRecyclerViewAdapter.ViewHolder>() {

    private val mOnClickListener: View.OnClickListener

    init {
        mOnClickListener = View.OnClickListener { v ->
            val item = v.tag as ItemInfo
            // Notify the active callbacks interface (the activity, if the fragment is attached to
            // one) that an item has been selected.
            //mListener?.onListFragmentInteraction(item)
            v.findNavController().navigate(R.id.action_nav_home_to_itemDetailsFragment, bundleOf("item_id1" to item.itemId))
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.fragment_item, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = mValues[position]
        holder.mIdView.text = item.title
        holder.mContentView.text = item.price.toString()
        if(item.image!=null)
            holder.mImage.setImageBitmap(item.image)

        with(holder.mView) {
            tag = item
            setOnClickListener(mOnClickListener)
        }

        holder.mEditButton.setOnClickListener {it.findNavController().navigate(R.id.action_nav_home_to_nav_edit_item,
            bundleOf("item_id1" to item.itemId, "deep_link" to true ))}
    }

    override fun getItemCount(): Int = mValues.size

    inner class ViewHolder(val mView: View) : RecyclerView.ViewHolder(mView) {
        val mIdView: TextView = mView.item_number
        val mContentView: TextView = mView.content
        val mEditButton: Button = mView.item_list_edit_button
        val mImage: ImageView = mView.item_image_preview

        override fun toString(): String {
            return super.toString() + " '" + mContentView.text + "'"
        }
    }
}
