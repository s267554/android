package it.polito.mad.team19lab2.ui

import android.app.Activity
import android.content.pm.PackageManager
import android.location.Address
import android.location.Geocoder
import android.os.Bundle
import android.text.Editable
import android.util.Log
import android.view.*
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.app.ActivityCompat
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.ktx.storage
import com.squareup.picasso.Picasso
import it.polito.mad.team19lab2.R
import it.polito.mad.team19lab2.data.ItemModel
import it.polito.mad.team19lab2.data.ReviewModel
import it.polito.mad.team19lab2.data.UserModel
import it.polito.mad.team19lab2.utilities.WorkaroundMapFragment
import it.polito.mad.team19lab2.viewModel.UserViewModel
import kotlinx.android.synthetic.main.fragment_edit_profile.*
import kotlinx.android.synthetic.main.fragment_show_profile.*
import kotlinx.android.synthetic.main.fragment_show_profile.image_view
import kotlinx.android.synthetic.main.fragment_show_profile.roundCardView
import java.util.*
import kotlin.collections.ArrayList

class ShowProfileFragment :Fragment() {

    private lateinit var user: UserModel
    private var currentUser = FirebaseAuth.getInstance().currentUser
    private lateinit var storage: FirebaseStorage
    private val userVm: UserViewModel by viewModels()
    private lateinit var userId: String
    private lateinit var gMap: GoogleMap
    private lateinit var reviewArray:ArrayList<ReviewModel>

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        val view= inflater.inflate(R.layout.fragment_show_profile, container, false)
        return view;
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        storage = Firebase.storage
    }

    override fun onViewCreated (view: View, savedInstanceState : Bundle?){
        super.onViewCreated(view, savedInstanceState)
        userId = if (arguments != null){
            arguments?.getString("user_id").toString()
        } else{
            currentUser?.uid ?: ""
        }
        userVm.getUser(userId).observe(viewLifecycleOwner, Observer {
            user = it
            if (user.imagePath.isEmpty()) {
                image_view.setImageResource(R.drawable.avatar_foreground)
            }
            else{
                downloadFile()
            }
            name_view.text = user.fullname
            nickname_view.text = user.nickname
            interests_view.text = buildInterestsString()
            ratingBar.rating= user.rating
            if(user.id == currentUser?.uid ?: ""){
                setHasOptionsMenu(true)
                email_view.text = user.email
                location_view.text = user.location
            }
            else{
                setHasOptionsMenu(false)
                textView4.visibility=View.GONE
                textView5.visibility=View.GONE
                emailIcon.visibility=View.GONE
                email_view.visibility=View.GONE
                locationIcon.visibility=View.GONE
                location_view.visibility=View.GONE
            }
            if(user.location.isNotEmpty()){
                val supportMapFragment2 : SupportMapFragment = ( childFragmentManager.findFragmentById(R.id.google_maps) as WorkaroundMapFragment)
                supportMapFragment2.getMapAsync {map ->
                    gMap = map
                    gMap.uiSettings.isMapToolbarEnabled = false
                    var mScrollView = scrollParentShowProfile //parent scrollview in xml, give your scrollview id value
                    (childFragmentManager.findFragmentById(R.id.google_maps) as WorkaroundMapFragment?)
                        ?.setListener(object : WorkaroundMapFragment.OnTouchListener {
                            override fun onTouch() {
                                mScrollView.requestDisallowInterceptTouchEvent(true)
                            }
                        })
                    pointInMap(user.location)
                }
            }
            else{
                view.findViewById<View>(R.id.google_maps).visibility=View.GONE
            }
        })
        roundCardView.viewTreeObserver.addOnGlobalLayoutListener (object: ViewTreeObserver.OnGlobalLayoutListener{
            override fun onGlobalLayout() {
                roundCardView.radius =  roundCardView.height.toFloat() / 2.0F
                roundCardView.viewTreeObserver.removeOnGlobalLayoutListener(this)
            }
        })
        val recycler: RecyclerView = view.findViewById(R.id.commentList)
        userVm.getReviews(userId).observe(viewLifecycleOwner, androidx.lifecycle.Observer {
            reviewArray= it as ArrayList<ReviewModel>
            with(recycler) {
                layoutManager = LinearLayoutManager(context)
                adapter = CommentsRecyclerViewAdapter(reviewArray)
            }
            if(reviewArray.count() != 0) {
                view.findViewById<TextView>(R.id.noCommentsToShow)?.visibility = View.GONE
                view.findViewById<ConstraintLayout>(R.id.commentContainer)?.visibility = View.VISIBLE
            }else{
                view.findViewById<TextView>(R.id.noCommentsToShow)?.visibility = View.VISIBLE
                view.findViewById<ConstraintLayout>(R.id.commentContainer)?.visibility = View.GONE
            }
        })
    }

    private fun buildInterestsString():String{
        val selectInterests= resources.getStringArray(R.array.categories).toMutableList()
        var s = ""
        for (i in user.interests.indices) {
            s += selectInterests[user.interests[i]]
            if(i!=user.interests.size-1)
                s= "$s, "
        }
        return s
    }

    override fun onCreateOptionsMenu(menu: Menu, menuInflater: MenuInflater) {
        menuInflater.inflate(R.menu.item_details_menu, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if(item.itemId == R.id.edit_item_action)
            editProfile()
        return super.onOptionsItemSelected(item)
    }

    private fun editProfile(){
        val navController = findNavController()
        navController.navigate(R.id.action_showProfileFragment_to_editProfileFragment,  bundleOf("user_id" to userId))
    }

    private fun downloadFile() {
        val storageRef = storage.reference
        val imView= view?.findViewById<ImageView>(R.id.image_view)
        storageRef.child(user.imagePath).downloadUrl.addOnSuccessListener {
            Picasso.get().load(it).noFade().placeholder(R.drawable.progress_animation).into(imView)
        }.addOnFailureListener {
        }
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
        }
    }
}