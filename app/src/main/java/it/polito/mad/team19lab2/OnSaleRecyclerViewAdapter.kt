package it.polito.mad.team19lab2

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.os.bundleOf
import androidx.navigation.findNavController
import androidx.recyclerview.widget.RecyclerView

import kotlinx.android.synthetic.main.onsale_item_card_view.view.*

class OnSaleRecyclerViewAdapter (val onSaleItems: ArrayList<ItemInfo>):
RecyclerView.Adapter<OnSaleRecyclerViewAdapter.ViewHolder> ( ){

    override fun getItemCount() = onSaleItems.size
    private val onSaleCL: View.OnClickListener

    init {
        onSaleCL = View.OnClickListener { v ->
            val item = v.tag as ItemInfo
            v.findNavController().navigate(R.id.action_onSaleListFragment_to_nav_item_detail, bundleOf("item_id1" to item.itemId))
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OnSaleRecyclerViewAdapter.ViewHolder {
        val vh = LayoutInflater.from(parent.context)
            .inflate(R.layout.onsale_item_card_view, parent, false)
        return ViewHolder(vh)
    }

    override fun onBindViewHolder(holder: OnSaleRecyclerViewAdapter.ViewHolder, position: Int) {
        holder.bind(onSaleItems[position])
        with(holder.cv){
            tag = onSaleItems[position]
            setOnClickListener(onSaleCL)
        }
    }

    inner class ViewHolder(val cv: View): RecyclerView.ViewHolder(cv) {
        val itemTitle: TextView = cv.item_title
        val itemPrice: TextView = cv.content
        val itemImage: ImageView = cv.item_image_preview

        fun bind(item: ItemInfo){
            itemTitle.text = item.title
            itemPrice.text = "€ " + item.price.toString()
            if(item.image != null)
                itemImage.setImageBitmap(item.image)
        }
    }
}