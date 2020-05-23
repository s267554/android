package it.polito.mad.team19lab2.ui

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.core.os.bundleOf
import androidx.navigation.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.ktx.storage
import com.squareup.picasso.Picasso
import it.polito.mad.team19lab2.R
import it.polito.mad.team19lab2.data.ItemModel

import kotlinx.android.synthetic.main.fragment_item.view.*

class OnSaleRecyclerViewAdapter(private val onSaleItems: ArrayList<ItemModel>):
RecyclerView.Adapter<OnSaleRecyclerViewAdapter.ViewHolder> ( ){

    override fun getItemCount() = onSaleItems.size
    private val onSaleCL: View.OnClickListener
    lateinit var storage: FirebaseStorage

    init {
        onSaleCL = View.OnClickListener { v ->
            val item = v.tag as ItemModel
            val bundle = bundleOf("item_id1" to item.id)
            v.findNavController().navigate(R.id.action_nav_home_to_nav_item_detail, bundle)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val vh = LayoutInflater.from(parent.context)
            .inflate(R.layout.fragment_item, parent, false)
        return ViewHolder(vh)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(onSaleItems[position])
        with(holder.cv){
            tag = onSaleItems[position]
            val b = findViewById<Button>(R.id.item_list_edit_button)
            b.visibility = View.GONE
            setOnClickListener(onSaleCL)
        }
    }

    inner class ViewHolder(val cv: View): RecyclerView.ViewHolder(cv) {
        private val itemTitle: TextView = cv.item_title
        private val itemPrice: TextView = cv.content
        private val itemImage: ImageView = cv.item_image_preview

        fun bind(item: ItemModel){
            itemTitle.text = item.title
            itemPrice.text = "€ " + item.price.toString()
            storage = Firebase.storage
            if (item.imagePath.isNotEmpty()) {
                val storageRef = storage.reference
                storageRef.child(item.imagePath).downloadUrl.addOnSuccessListener {
                    Picasso.get().load(it).noFade().placeholder(R.drawable.progress_animation)
                        .into(this.itemImage, object : com.squareup.picasso.Callback {
                            override fun onSuccess() {
                            }

                            override fun onError(e: java.lang.Exception?) {
                            }
                        })
                }.addOnFailureListener {
                    Log.e("IMAGE", "Error in download image")
                }
            } else {
                this.itemImage.setImageResource(R.drawable.sport_category_foreground)
            }
        }

    }
}