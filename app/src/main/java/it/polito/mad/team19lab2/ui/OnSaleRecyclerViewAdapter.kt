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

    override fun onBindViewHolder(holder: ViewHolder, position: Int, payloads: MutableList<Any>) {
        if (payloads.isEmpty()) {
            super.onBindViewHolder(holder, position, payloads)
        } else {
            val o = payloads[0] as Bundle
            val item=onSaleItems.get(position)
            for (key in o.keySet()) {
                if (key == "title") {
                    holder.itemTitle.setText(item.title)
                }
                if (key == "price") {
                    holder.itemPrice.setText("€ "+item.price.toString())
                }
                if(key=="imagePath"){
                    storage = Firebase.storage
                    val storageRef = storage.reference
                    storageRef.child(item.imagePath).downloadUrl.addOnSuccessListener {
                        Picasso.get().load(it).noFade().placeholder(R.drawable.progress_animation)
                            .into(holder.itemImage, object : com.squareup.picasso.Callback {
                                override fun onSuccess() {
                                }

                                override fun onError(e: java.lang.Exception?) {
                                }
                            })
                    }.addOnFailureListener {
                    }
                }
            with(holder.cv) {
                tag = onSaleItems[position]
                setOnClickListener(onSaleCL)
            }
            }
        }
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        holder.bind(onSaleItems[position])
        with(holder.cv) {
            tag = onSaleItems[position]
            val b = findViewById<Button>(R.id.item_list_edit_button)
            b.visibility = View.GONE
            setOnClickListener(onSaleCL)
        }
    }


    fun onNewData(newData: ArrayList<ItemModel>?) {
        val diffResult = DiffUtil.calculateDiff(MyDiffUtilCallback(newData, onSaleItems))
        diffResult.dispatchUpdatesTo(this)
        this.onSaleItems.clear()
        if (newData != null) {
            this.onSaleItems.addAll(newData)
        }
    }

    fun reloadData(newData: ArrayList<ItemModel>?) {
        this.onSaleItems.clear()
        if (newData != null) {
            this.onSaleItems.addAll(newData)
        }
    }


    inner class ViewHolder(val cv: View): RecyclerView.ViewHolder(cv) {
        val itemTitle: TextView = cv.item_title
        val itemPrice: TextView = cv.content
        val itemImage: ImageView = cv.item_image_preview

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
                }
            } else {
                this.itemImage.setImageResource(R.mipmap.launcher_icon_no_text)
            }
        }

    }
}

class MyDiffUtilCallback(newList: ArrayList<ItemModel>?, oldList: ArrayList<ItemModel>?) :
    DiffUtil.Callback() {
    var newList: ArrayList<ItemModel>?
    var oldList: ArrayList<ItemModel>?
    override fun getOldListSize(): Int {
        return if (oldList != null) oldList!!.size else 0
    }

    override fun getNewListSize(): Int {
        return if (newList != null) newList!!.size else 0
    }

    override fun areItemsTheSame(
        oldItemPosition: Int,
        newItemPosition: Int
    ): Boolean {
        return newList!![newItemPosition].id==oldList!![oldItemPosition].id
    }

    override fun areContentsTheSame(
        oldItemPosition: Int,
        newItemPosition: Int
    ): Boolean {
        val result: Int = newList!![newItemPosition].compareTo(oldList!![oldItemPosition])
        return result == 0
    }

    @Nullable
    override fun getChangePayload(oldItemPosition: Int, newItemPosition: Int): Any? {
        val newContact: ItemModel = newList!![newItemPosition]
        val oldContact: ItemModel = oldList!![oldItemPosition]
        val diff = Bundle()
        if (!newContact.title.equals(oldContact.title)) {
            diff.putString("title", newContact.title)
        }
        if (!newContact.price.equals(oldContact.price)) {
            diff.putFloat("price", newContact.price)
        }
        if (newContact.imageVersion!=oldContact.imageVersion) {
            diff.putString("imagePath", newContact.imagePath)
        }
        return if (diff.size() == 0) {
            null
        } else
            diff
    }

    init {
        this.newList = newList
        this.oldList = oldList
    }
}