package it.polito.mad.team19lab2.ui

import android.util.Log
import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.core.os.bundleOf
import androidx.navigation.findNavController
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.ktx.storage
import com.squareup.picasso.Picasso
import it.polito.mad.team19lab2.R
import it.polito.mad.team19lab2.data.ItemModel

import kotlinx.android.synthetic.main.fragment_item.view.*
import java.util.ArrayList


class MyItemRecyclerViewAdapter(
    private val mValues: ArrayList<ItemModel>
) : RecyclerView.Adapter<MyItemRecyclerViewAdapter.ViewHolder>() {

    private val mOnClickListener: View.OnClickListener
    private lateinit var storage: FirebaseStorage

    init {
        mOnClickListener = View.OnClickListener { v ->
            val item = v.tag as ItemModel
            val bundle = bundleOf(
                "item_id1" to item.id,
                "source_fragment" to R.id.action_nav_my_advertisement_to_nav_item_detail
            )
            v.findNavController()
                .navigate(R.id.action_nav_my_advertisement_to_nav_item_detail, bundle)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.fragment_item, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = mValues[position]
        storage = Firebase.storage
        holder.mIdView.text = item.title
        holder.mContentView.text = "€ " + item.price.toString()
        if (item.imagePath.isNotEmpty()) {
            //Download image
            val storageRef = storage.reference
            storageRef.child(item.imagePath).downloadUrl.addOnSuccessListener {
                Picasso.get().load(it).noFade().placeholder(R.drawable.progress_animation)
                    .into(holder.mImage, object : com.squareup.picasso.Callback {
                        override fun onSuccess() {
                        }

                        override fun onError(e: java.lang.Exception?) {
                        }
                    })
            }.addOnFailureListener {
                Log.e("IMAGE", "Error in download image")
            }
        } else {
            holder.mImage.setImageResource(R.drawable.sport_category_foreground)
        }

        with(holder.mView) {
            tag = item
            setOnClickListener(mOnClickListener)
        }

        holder.mEditButton.setOnClickListener {
            it.findNavController().navigate(
                R.id.action_nav_my_advertisement_to_nav_edit_item,
                bundleOf("item_id1" to item.id, "deep_link" to true, "edit" to true)
            )
        }
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
