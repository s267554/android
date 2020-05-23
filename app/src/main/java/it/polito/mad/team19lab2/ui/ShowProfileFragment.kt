package it.polito.mad.team19lab2.ui

import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.ImageView
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.ktx.storage
import com.squareup.picasso.Picasso
import it.polito.mad.team19lab2.R
import it.polito.mad.team19lab2.data.UserModel
import it.polito.mad.team19lab2.viewModel.UserViewModel
import kotlinx.android.synthetic.main.fragment_show_profile.*
import kotlinx.android.synthetic.main.fragment_show_profile.image_view
import kotlinx.android.synthetic.main.fragment_show_profile.roundCardView

class ShowProfileFragment :Fragment() {

    private lateinit var user: UserModel
    private var currentUser = FirebaseAuth.getInstance().currentUser
    private lateinit var storage: FirebaseStorage
    private val userVm: UserViewModel by viewModels()
    private lateinit var userId: String

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_show_profile, container, false)
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
        })
        roundCardView.viewTreeObserver.addOnGlobalLayoutListener (object: ViewTreeObserver.OnGlobalLayoutListener{
            override fun onGlobalLayout() {
                roundCardView.radius =  roundCardView.height.toFloat() / 2.0F
                roundCardView.viewTreeObserver.removeOnGlobalLayoutListener(this)
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
            Log.e("IMAGE", "Error in download image")
        }
    }
}