package it.polito.mad.team19lab2.ui

import InterestedUsersRecycleViewAdapter
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.TextView
import android.widget.Toast
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.ktx.storage
import com.ms.square.android.expandabletextview.ExpandableTextView
import com.squareup.picasso.Picasso
import it.polito.mad.team19lab2.R
import it.polito.mad.team19lab2.data.ItemModel
import it.polito.mad.team19lab2.data.UserShortModel
import it.polito.mad.team19lab2.viewModel.ItemViewModel
import it.polito.mad.team19lab2.viewModel.UserViewModel
import kotlinx.android.synthetic.main.item_details_fragment.*


class ItemDetailsFragment : Fragment() {

    private val TAG = "ITEM_DETAIL_FRAGMENT"
    private lateinit var item: ItemModel
    lateinit var storage: FirebaseStorage
    private val itemVm: ItemViewModel by viewModels()
    private val userVm: UserViewModel by viewModels()
    private lateinit var idItem : String
    private lateinit var sellerUser:String
    private var user = FirebaseAuth.getInstance().currentUser
    private var interestedUsers = ArrayList<UserShortModel>()
    private var listenerSet=false
    var f: Boolean = false
    var favourite: Boolean = false
    lateinit var navC:NavController

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
        val recycler: RecyclerView = view.findViewById(R.id.userList)
        var usersLabel = view.findViewById<TextView>(R.id.interestedUsers)
        itemVm.getItem(idItem).observe(viewLifecycleOwner, Observer { it ->
            item = it
            if(it.userId == user?.uid ?: ""){
                //CASE I AM THE USER
                setHasOptionsMenu(true)
                buyButton.visibility = View.GONE
                fab.visibility=View.GONE
                seller_textView.visibility=View.GONE
                materialCardView3.visibility=View.GONE

                itemVm.getInterestedUsers(idItem).observe(viewLifecycleOwner, Observer { users ->
                    interestedUsers= users as ArrayList<UserShortModel>
                    Log.d("userList", users.toString())
                    with(recycler) {
                        layoutManager = androidx.recyclerview.widget.LinearLayoutManager(context)
                        adapter =  InterestedUsersRecycleViewAdapter(interestedUsers)
                    }
                    if(interestedUsers.size == 0) {
                        noUsers.visibility = View.VISIBLE
                    }
                })
            }
            else{
                //CASE I AM NOT THE USER
                userList.visibility=View.GONE
                usersLabel.visibility=View.GONE
                userVm.getUser(item.userId).observe(viewLifecycleOwner, Observer { user ->
                    sellerUser=user.id
                    seller_fullname.setText(user.fullname)
                    seller_nickname.setText(user.nickname)
                })
                Log.d("USER", "i'm not the user")
                setHasOptionsMenu(false)
                buyButton.setOnClickListener {
                    item.state = "Sold"
                    itemVm.saveItem(item)
                    Toast.makeText(this.context, "Item bought", Toast.LENGTH_SHORT).show()
                }
                val fab: FloatingActionButton = view.findViewById(R.id.fab)
                itemVm.getInterestedUsers(idItem).observe(viewLifecycleOwner, Observer { users ->
                    interestedUsers= users as ArrayList<UserShortModel>
                    for(intUser in interestedUsers){
                        if(intUser.id == user?.uid ?: ""){
                            favourite=true
                            fab.setImageResource(R.drawable.baseline_favorite_black_48dp)
                            break
                        }
                    }
                })
                fab.setOnClickListener {
                    Log.d(TAG, "iditem: $idItem")
                    if(!favourite) {
                        itemVm.addInterestedUser(idItem)
                        favourite=true
                        fab.setImageResource(R.drawable.baseline_favorite_black_48dp)
                        Toast.makeText(this.context, "Owner notified of your interest", Toast.LENGTH_LONG).show()
                    }
                    else{
                        itemVm.removeInterestedUser(idItem)
                        favourite=false
                        fab.setImageResource(R.drawable.baseline_favorite_border_black_48dp)
                        Toast.makeText(this.context, "Item removed from your interests", Toast.LENGTH_LONG).show()
                    }
                }
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


            seller_view_button.setOnClickListener {
                Log.d("seller", sellerUser+ "  <  "+it.toString())
                if(it!=null&&!sellerUser.isEmpty()){
                    Log.d("seller", findNavController().graph.toString())
                    Log.d("seller", sellerUser)
                    Navigation.findNavController(requireView()).navigate(R.id.action_nav_item_detail_to_nav_show_profile,bundleOf("user_id" to sellerUser))
                } }




        // TODO: if I am already present in users list then change Action Button to "check" symbol
    }

    fun navigateToUserProfile(){
        findNavController().
        navigate(R.id.action_nav_item_detail_to_nav_show_profile,bundleOf("user_id" to item.userId))
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
