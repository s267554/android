package it.polito.mad.team19lab2.ui

import android.content.Context
import android.opengl.Visibility
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.contentValuesOf
import androidx.core.os.bundleOf
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.findFragment
import androidx.navigation.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.ktx.storage
import com.squareup.picasso.Picasso
import it.polito.mad.team19lab2.MainActivity
import it.polito.mad.team19lab2.R
import it.polito.mad.team19lab2.data.ItemModel
import kotlinx.android.synthetic.main.fragment_item.view.*


class BoughtItemsRecyclerViewAdapter(private val boughtItems: ArrayList<ItemModel>,private val childFragManager: FragmentManager):

    RecyclerView.Adapter<BoughtItemsRecyclerViewAdapter.ViewHolder> ( ){

    override fun getItemCount() = boughtItems.size
    private val boughtCL: View.OnClickListener
    private val rateOnClickListener: View.OnClickListener
    private val rateOnLongClickListener: View.OnLongClickListener
    lateinit var storage: FirebaseStorage

    init {
        boughtCL = View.OnClickListener { v ->
            val item = v.tag as ItemModel
            val bundle = bundleOf("item_id1" to item.id)
            v.findNavController().navigate(R.id.action_nav_bought_items_to_nav_item_detail, bundle)
        }
        rateOnClickListener = View.OnClickListener { v ->
            val item = v.tag as ItemModel
            val d=RateAndCommentDialog(item.userId,item.id)
            d.show(v.findFragment<Fragment>().childFragmentManager,"search dialog")

            //b.findFragment<Fragment>()
        }
        rateOnLongClickListener = View.OnLongClickListener { v ->
            Toast.makeText(v.context, R.string.rate_button_long_click, Toast.LENGTH_SHORT).show()
            return@OnLongClickListener true
        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val vh = LayoutInflater.from(parent.context)
            .inflate(R.layout.fragment_item, parent, false)
        return ViewHolder(vh)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(boughtItems[position])
        with(holder.cv) {
            tag = boughtItems[position]
            val b = findViewById<Button>(R.id.item_list_edit_button)
            setOnClickListener(boughtCL)
            if(!boughtItems[position].reviewed) {
                b.text = resources.getString(R.string.rate_and_commment)
                b.tag = boughtItems[position]
                b.setOnClickListener(rateOnClickListener)
                b.setOnLongClickListener(rateOnLongClickListener)

            }
            else{
                b.visibility=View.GONE
            }
        }
    }

    inner class ViewHolder(val cv: View): RecyclerView.ViewHolder(cv) {
        val itemTitle: TextView = cv.item_title
        val itemPrice: TextView = cv.content
        val itemImage: ImageView = cv.item_image_preview

        fun bind(item: ItemModel){
            itemTitle.text = item.title
            itemPrice.text = "??? " + item.price.toString()
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