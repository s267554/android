package it.polito.mad.team19lab2.ui

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.Nullable
import androidx.core.os.bundleOf
import androidx.navigation.findNavController
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.ktx.storage
import com.squareup.picasso.Picasso
import it.polito.mad.team19lab2.R
import it.polito.mad.team19lab2.data.ItemModel
import it.polito.mad.team19lab2.data.ItemShortModel

import kotlinx.android.synthetic.main.fragment_item.view.*

class ItemsOfInterestRecyclerViewAdapter(private val itemSM: ArrayList<ItemShortModel>):

    RecyclerView.Adapter<ItemsOfInterestRecyclerViewAdapter.ViewHolder> ( ){

    override fun getItemCount() = itemSM.size
    private val viewItemCL: View.OnClickListener
    lateinit var storage: FirebaseStorage

    init {
        viewItemCL = View.OnClickListener { v ->
            val item = v.tag as ItemShortModel
            val bundle = bundleOf("item_id1" to item.id)

            v.findNavController().navigate(R.id.action_nav_items_of_interest_to_nav_item_detail, bundle)

        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val vh = LayoutInflater.from(parent.context)
            .inflate(R.layout.fragment_item, parent, false)
        return ViewHolder(vh)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        holder.bind(itemSM[position])
        with(holder.cv) {
            tag = itemSM[position]
            val b = findViewById<Button>(R.id.item_list_edit_button)
            b.visibility = View.GONE
            setOnClickListener(viewItemCL)
        }
    }

    inner class ViewHolder(val cv: View): RecyclerView.ViewHolder(cv) {
        val itemTitle: TextView = cv.item_title
        val itemPrice: TextView = cv.content
        val itemImage: ImageView = cv.item_image_preview

        fun bind(item: ItemShortModel){
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
                }
            } else {
                this.itemImage.setImageResource(R.drawable.sport_category_foreground)
            }
        }

    }
}