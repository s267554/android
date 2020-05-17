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
import it.polito.mad.team19lab2.data.ItemModel

import kotlinx.android.synthetic.main.fragment_item.view.*
import java.util.ArrayList


class MyItemRecyclerViewAdapter(
    private val mValues: ArrayList<ItemModel>
) : RecyclerView.Adapter<MyItemRecyclerViewAdapter.ViewHolder>() {

    private val mOnClickListener: View.OnClickListener

    init {
        mOnClickListener = View.OnClickListener { v ->
            val item = v.tag as ItemModel
            val bundle = bundleOf(
                "item_id1" to item.id,
                "source_fragment" to R.id.action_nav_my_advertisement_to_nav_item_detail
            )
            v.findNavController().navigate(R.id.action_nav_my_advertisement_to_nav_item_detail, bundle)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.fragment_item, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = mValues[position] as ItemModel
        holder.mIdView.text = item.title
        holder.mContentView.text = "€ " + item.price.toString()
        if(item.imagePath!=null)
            holder.mImage.setImageResource(R.drawable.account_icon_foreground)
        else{
            holder.mImage.setImageResource(R.drawable.sport_category_foreground)
        }

        with(holder.mView) {
            tag = item
            setOnClickListener(mOnClickListener)
        }

        holder.mEditButton.setOnClickListener {it.findNavController().navigate(R.id.action_nav_my_advertisement_to_nav_edit_item,
            bundleOf("item_id1" to item.id, "deep_link" to true, "edit" to true ))}
    }

    override fun getItemCount(): Int = mValues.size

    inner class ViewHolder(val mView: View) : RecyclerView.ViewHolder(mView) {
        val mIdView: TextView = mView.item_title
        val mContentView: TextView = mView.content
        val mEditButton: Button = mView.item_list_edit_button
        val mImage: ImageView = mView.item_image_preview

        override fun toString(): String {
            return super.toString() + " '" + mContentView.text + "'"
        }
    }
}
