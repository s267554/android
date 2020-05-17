package it.polito.mad.team19lab2

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.os.bundleOf
import androidx.navigation.findNavController
import androidx.recyclerview.widget.RecyclerView
import it.polito.mad.team19lab2.data.ItemModel

import kotlinx.android.synthetic.main.onsale_item_card_view.view.*

class OnSaleRecyclerViewAdapter(val onSaleItems: ArrayList<ItemModel>):
RecyclerView.Adapter<OnSaleRecyclerViewAdapter.ViewHolder> ( ){

    override fun getItemCount() = onSaleItems.size
    private val onSaleCL: View.OnClickListener

    init {
        onSaleCL = View.OnClickListener { v ->
            val item = v.tag as ItemModel
            val bundle = bundleOf("item_id1" to item.id)
            v.findNavController().navigate(R.id.action_nav_home_to_nav_on_sale, bundle)
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

        fun bind(item: ItemModel){
            itemTitle.text = item.title
            itemPrice.text = "€ " + item.price.toString()
            if(item.imagePath != null)
                itemImage.setImageResource(R.drawable.account_icon_foreground)
            else
                itemImage.setImageResource(R.drawable.sport_category_foreground)
        }
    }
}