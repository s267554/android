package it.polito.mad.team19lab2.ui

import android.content.ClipData
import android.os.Bundle
import android.util.Log
import android.view.*
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.ktx.storage
import com.ms.square.android.expandabletextview.ExpandableTextView
import com.squareup.picasso.Picasso
import it.polito.mad.team19lab2.R
import it.polito.mad.team19lab2.data.ItemModel
import it.polito.mad.team19lab2.viewModel.ItemViewModel
import kotlinx.android.synthetic.main.item_details_fragment.*


class ItemDetailsFragment : Fragment() {

    private lateinit var item: ItemModel
    lateinit var storage: FirebaseStorage
    private val itemVm: ItemViewModel by viewModels()
    private lateinit var idItem : String
    private var user = FirebaseAuth.getInstance().currentUser

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        storage = Firebase.storage
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.item_details_fragment, container, false)
    }

    override fun onViewCreated (view: View, savedInstanceState : Bundle?){
        super.onViewCreated(view, savedInstanceState)
        val descriptionExpandable = view.findViewById<ExpandableTextView>(R.id.expand_text_view)
        roundCardView.viewTreeObserver.addOnGlobalLayoutListener (object: ViewTreeObserver.OnGlobalLayoutListener{
            override fun onGlobalLayout() {
                roundCardView.radius =  roundCardView.height.toFloat() / 2.0F
                roundCardView.viewTreeObserver.removeOnGlobalLayoutListener(this)
            }
        })
        if (arguments != null){
            idItem = arguments?.getString("item_id1").toString()
        }
        itemVm.getItem(idItem).observe(viewLifecycleOwner, Observer {
            item = it
            Log.d("USER", it.userId)
            Log.d("USER", user?.uid)
            if(it.userId == user?.uid ?: ""){
                //CASE I AM THE USER
                Log.d("USER", "i'm the user")
                setHasOptionsMenu(true)
            }
            else{
                //CASE I AM NOT THE USER
                Log.d("USER", "i'm not the user")
                setHasOptionsMenu(false)
            }
            if(item.imagePath.isNullOrEmpty()){
                image_view.setImageResource(R.drawable.sport_category_foreground)
            }
            else{
                downloadFile()
            }
            titleTextView.text = item.title
            descriptionExpandable.text = item.description
            locationTextView.text = item.location
            priceTextView.text = item.price.toString() + " €"
            expireTextView.text = item.expiryDate
            categoryTextView.text = item.category
            subCategoryTextView.text=item.subcategory
        })
    }

    override fun onCreateOptionsMenu(menu: Menu, menuInflater: MenuInflater) {
        menuInflater.inflate(R.menu.item_details_menu, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if(item.itemId == R.id.edit_item_action)
            editItem()
        return super.onOptionsItemSelected(item)
    }

    private fun editItem(){
        findNavController().navigate(R.id.action_nav_item_detail_to_nav_edit_item, bundleOf("item_id1" to idItem))
    }

    private fun downloadFile() {
        val storageRef = storage.reference
        storageRef.child(item.imagePath).downloadUrl.addOnSuccessListener {
            Picasso.get().load(it).noFade().placeholder(R.drawable.progress_animation).into(image_view)
        }.addOnFailureListener {
            Log.d("image", "error in download image")
        }
    }
}
