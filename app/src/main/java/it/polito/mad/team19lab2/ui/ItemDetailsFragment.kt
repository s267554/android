package it.polito.mad.team19lab2.ui

import BuyersRecycleViewAdapter
import InterestedUsersRecycleViewAdapter
import android.app.AlertDialog
import android.content.DialogInterface
import android.graphics.Color
import android.location.Address
import android.location.Geocoder
import android.os.Bundle
import android.view.*
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.core.os.bundleOf
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import androidx.fragment.app.findFragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.Navigation
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.ktx.storage
import com.ms.square.android.expandabletextview.ExpandableTextView
import com.squareup.picasso.Picasso
import it.polito.mad.team19lab2.R
import it.polito.mad.team19lab2.data.ItemModel
import it.polito.mad.team19lab2.data.ReviewModel
import it.polito.mad.team19lab2.data.UserModel
import it.polito.mad.team19lab2.data.UserShortModel
import it.polito.mad.team19lab2.utilities.WorkaroundMapFragment
import it.polito.mad.team19lab2.viewModel.ItemViewModel
import it.polito.mad.team19lab2.viewModel.UserViewModel
import kotlinx.android.synthetic.main.item_details_fragment.*
import kotlinx.coroutines.*
import java.util.*
import kotlin.collections.ArrayList

class ItemDetailsFragment : Fragment(), BuyersRecycleViewAdapter.SellItemClick,RateAndCommentDialog.NoticeDialogListener  {


    private lateinit var item: ItemModel
    lateinit var storage: FirebaseStorage
    private val itemVm: ItemViewModel by viewModels()
    private val userVm: UserViewModel by viewModels()
    private lateinit var idItem : String
    private lateinit var sellerUser:String
    private lateinit var currentuser: UserModel
    private var user = FirebaseAuth.getInstance().currentUser
    private var interestedUsers = ArrayList<UserShortModel>()
    private var buyers = ArrayList<UserShortModel>()
    var favourite: Boolean = false
    private lateinit var gMap: GoogleMap

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
        userVm.getProfile().observe(viewLifecycleOwner,
            androidx.lifecycle.Observer {
                currentuser=it as UserModel
            }
        )
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
        val recyclerBuyer: RecyclerView = view.findViewById(R.id.buyerList)
        val usersLabel = view.findViewById<TextView>(R.id.interestedUsers)
        itemVm.getItem(idItem).observe(viewLifecycleOwner, Observer { it ->
            item = it
            if(it.userId == user?.uid ?: ""){
                //CASE I AM THE USER
                if (item.state != 2)
                    setHasOptionsMenu(true)
                else
                    setHasOptionsMenu(false)
                buyButton.visibility = View.GONE
                fab.visibility=View.GONE
                seller_textView.visibility=View.GONE
                materialCardView3.visibility=View.GONE

                if(item.state!=2) {
                    itemVm.getInterestedUsers(idItem)
                        .observe(viewLifecycleOwner, Observer { users ->
                            interestedUsers = users as ArrayList<UserShortModel>
                            with(recycler) {
                                layoutManager =
                                    androidx.recyclerview.widget.LinearLayoutManager(context)
                                adapter = InterestedUsersRecycleViewAdapter(interestedUsers)
                            }
                            if (interestedUsers.size == 0) {
                                noUsers.visibility = View.VISIBLE
                            }
                            else
                                noUsers.visibility = View.GONE
                        })
                    itemVm.getBuyers(idItem).observe(viewLifecycleOwner, Observer { users ->
                        buyers = users as ArrayList<UserShortModel>
                        with(recyclerBuyer) {
                            layoutManager =
                                androidx.recyclerview.widget.LinearLayoutManager(context)
                            adapter = BuyersRecycleViewAdapter(buyers) as BuyersRecycleViewAdapter
                            (adapter as BuyersRecycleViewAdapter).setSellItemClickListener(this@ItemDetailsFragment)
                        }
                        if (buyers.size == 0) {
                            noBuyers.visibility = View.VISIBLE
                        }
                        else
                            noBuyers.visibility = View.GONE
                    })
                    if(item.state==1){
                        statusTextView.setText(R.string.blocked)
                    }
                    else{
                        statusTextView.setText(R.string.available)
                    }
                }
                else{
                    statusTextView.setText(R.string.sold)
                    userList.visibility=View.GONE
                    usersLabel.visibility=View.GONE
                    buyerList.visibility=View.GONE
                    noUsers.visibility=View.GONE
                    noBuyers.visibility=View.GONE
                    view.findViewById<TextView>(R.id.buyers).visibility=View.GONE
                }
            }
            else{
                //CASE I AM NOT THE USER
                statusIcon.visibility=View.GONE
                statusLabel.visibility=View.GONE
                statusTextView.visibility=View.GONE
                userList.visibility=View.GONE
                usersLabel.visibility=View.GONE
                buyerList.visibility=View.GONE
                view.findViewById<TextView>(R.id.buyers).visibility=View.GONE
                userVm.getUser(item.userId).observe(viewLifecycleOwner, Observer { user ->
                    sellerUser=user.id
                    seller_fullname.text = user.fullname
                    seller_nickname.text = user.nickname
                })
                setHasOptionsMenu(false)
                if(item.state!=0){
                    buyButton.isClickable = false
                    buyButton.isEnabled = false
                    if(item.state==1) {
                        buyButton.setBackgroundColor(Color.GRAY)
                        buyButton.setText(R.string.blocked)
                        buyButton.setOnClickListener(null)
                    }
                    else{
                        buyButton.setOnClickListener(null)
                        if(item.buyerId == user?.uid) {
                            buyButton.setText(R.string.bought)
                        }
                        else
                            buyButton.setText(R.string.sold)
                        buyButton.setBackgroundColor(Color.GRAY)
                    }
                }
                else{
                    itemVm.getBuyers(idItem).observe(viewLifecycleOwner, Observer { users ->
                        var flag = 0
                        for (u in users) {
                            if (u.id == user?.uid ?: "") {
                                flag = 1
                                break
                            }
                        }
                        if (flag == 1) {
                            buyButton.setBackgroundColor(Color.GRAY)
                            buyButton.setText(R.string.waiting_for_approval)
                            buyButton.isClickable = false
                            buyButton.isEnabled = false
                            buyButton.setOnClickListener(null)
                        }
                        else{
                            buyButton.setOnClickListener {
                                AlertDialog.Builder(requireContext())
                                    .setTitle(R.string.confirm_purchase)
                                    .setMessage(R.string.confirm_purchase_text)
                                    .setIcon(android.R.drawable.ic_dialog_alert)
                                    .setNeutralButton(R.string.cancel, null)
                                    .setPositiveButton(R.string.yes
                                    ) { _, _ ->
                                        itemVm.saveBuyer(item)
                                        buyButton.isClickable = false
                                        buyButton.isEnabled = false
                                        buyButton.setBackgroundColor(Color.GRAY)
                                        buyButton.setText(R.string.waiting_for_approval)
                                        buyButton.setOnClickListener(null)
                                    }.show()
                            }
                        }
                    })
                }

                val fab: FloatingActionButton = view.findViewById(R.id.fab)
                if(item.state != 2)
                    fab.visibility = View.VISIBLE
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
                    if(!favourite) {
                        itemVm.addInterestedUser(idItem)
                        userVm.addInterestedItem(idItem)
                        favourite=true
                        fab.setImageResource(R.drawable.baseline_favorite_black_48dp)
                        Toast.makeText(this.context,  R.string.item_add_favourites, Toast.LENGTH_LONG).show()
                    }
                    else{
                        itemVm.removeInterestedUser(idItem)
                        userVm.removeInterestedItem(idItem)
                        favourite=false
                        fab.setImageResource(R.drawable.baseline_favorite_border_black_48dp)
                        Toast.makeText(this.context, R.string.item_removed_favourites, Toast.LENGTH_LONG).show()
                    }
                }
                fab.setOnLongClickListener {
                    if(favourite)
                        Toast.makeText(context, R.string.fab_remove_from_interests_on_long, Toast.LENGTH_SHORT).show()
                    else
                        Toast.makeText(context, R.string.fab_add_to_interests_on_long, Toast.LENGTH_SHORT).show()
                    return@setOnLongClickListener true
                }
            }
            if(item.imagePath.isEmpty()){
                image_view.setImageResource(R.mipmap.launcher_icon_no_text)
            }
            else{
                downloadFile()
            }
            titleTextView.text = item.title
            descriptionExpandable.text = item.description
            locationTextView.text = item.location
            priceTextView.text = "${item.price.toString()} €"
            expireTextView.text = item.expiryDate
            categoryTextView.text = resources.getStringArray(R.array.categories)[item.category]
            val sub = resources.getIdentifier("sub${item.category}", "array", context?.packageName)
            if (item.subcategory != -1)
                subCategoryTextView.text= resources.getStringArray(sub)[item.subcategory]
            if(item.location.isNotEmpty()){
                val supportMapFragment2 : SupportMapFragment = ( childFragmentManager.findFragmentById(R.id.google_maps) as WorkaroundMapFragment)
                supportMapFragment2.getMapAsync {map ->
                    gMap = map
                    gMap.uiSettings.isMapToolbarEnabled = false
                    var mScrollView = scrollParentShowItem
                    (childFragmentManager.findFragmentById(R.id.google_maps) as WorkaroundMapFragment?)
                        ?.setListener(object : WorkaroundMapFragment.OnTouchListener {
                            override fun onTouch() {
                                mScrollView.requestDisallowInterceptTouchEvent(true)
                            }
                        })
                    if(item.userId != user?.uid ?: ""){
                        gMap.setOnMapClickListener {
                            val dialog = LocationRouteDialog.newInstance(item.location)
                            dialog.show(childFragmentManager, "showRouteDialog")
                        }
                    }
                    //pointsinmap()
                    val geocoder = Geocoder(context, Locale.getDefault())
                    gMap.clear()
                    if(item.location.isNotEmpty()) {
                        CoroutineScope(Dispatchers.Default).launch {
                            val systime = System.currentTimeMillis()
                            val addresses = GlobalScope.async {
                                try {
                                    geocoder.getFromLocationName(item.location, 1)
                                } catch (e: Exception) {
                                    ArrayList<Address>()
                                }
                            }
                            withContext(Dispatchers.Main) {
                                addresses.await()?.let { addresses ->
                                    if(addresses.isNotEmpty() && addresses[0].hasLatitude() && addresses[0].hasLongitude()) {
                                        val markerPosition = MarkerOptions()
                                        val point = LatLng(addresses[0].latitude, addresses[0].longitude)
                                        markerPosition.position(point)
                                        gMap.animateCamera(CameraUpdateFactory.newLatLngZoom(point, 10F))
                                        gMap.addMarker(markerPosition)
                                    }
                                    else{
                                        view.findViewById<View>(R.id.google_maps)?.visibility=View.GONE
                                    }
                                }
                            }
                        }
                    }


                }
            }
            else{
                view.findViewById<View>(R.id.google_maps).visibility=View.GONE
            }
            materialCardView3.setOnClickListener{
                Navigation.findNavController(requireView()).navigate(R.id.action_nav_item_detail_to_nav_show_profile,bundleOf("user_id" to sellerUser))
            }
            if(!item.reviewed&&item.state==2) {
                seller_view_button.text = resources.getString(R.string.rate_and_commment)
                seller_view_button.setOnClickListener {
                    if(it!=null&&!sellerUser.isEmpty()){
                        val d=RateAndCommentDialog(item.userId,item.id)
                        d.show(it.findFragment<Fragment>().childFragmentManager,"search dialog")
                    } }
            }else{seller_view_button.visibility=View.GONE}
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
        val imView= view?.findViewById<ImageView>(R.id.image_view)
        storageRef.child(item.imagePath).downloadUrl.addOnSuccessListener {
            Picasso.get().load(it).noFade().placeholder(R.drawable.progress_animation).into(imView)
        }.addOnFailureListener {
        }
    }

    override fun sellButtonOnClick(v: View?, position: Int) {
        AlertDialog.Builder(requireContext())
            .setTitle(R.string.confirm_sell)
            .setMessage(R.string.confirm_sell_text)
            .setIcon(android.R.drawable.ic_dialog_alert)
            .setNeutralButton(R.string.cancel, null)
            .setPositiveButton(R.string.yes
            ) { _, _ ->
                item.state=2
                item.buyerId= buyers[position].id
                itemVm.sellItem(item)
            }.show()
    }

    private fun pointInMap(location: String){
        val geocoder = Geocoder(context, Locale.getDefault())
        gMap.clear()
        if(location.isNotEmpty()) {
            val addresses: List<Address> =
                geocoder.getFromLocationName(location, 1)
            if(addresses.isNotEmpty() && addresses[0].hasLatitude() && addresses[0].hasLongitude()) {
                val markerPosition = MarkerOptions()
                val point = LatLng(addresses[0].latitude, addresses[0].longitude)
                markerPosition.position(point)
                gMap.animateCamera(CameraUpdateFactory.newLatLngZoom(point, 10F))
                gMap.addMarker(markerPosition)
            }
            else{
                view?.findViewById<View>(R.id.google_maps)?.visibility=View.GONE
            }
        }
    }

    override fun onDialogPositiveClick(
        userId: String,
        itemId: String,
        comment: String?,
        rate: Float,
        user_nick: String
    ) {
        var c=""
        if(!comment.isNullOrEmpty())
            c=comment
        val r= ReviewModel(itemId,userId,c,rate,user_nick,currentuser.id,currentuser.nickname)
        var u=userVm.addReview(r)
    }

    override fun onDialogNegativeClick(dialog: DialogFragment) {
        return;
    }

}
