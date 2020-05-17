package it.polito.mad.team19lab2.ui

import android.os.Bundle
import android.util.Log
import android.view.*
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
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
    lateinit var storage: FirebaseStorage
    private val userVm: UserViewModel by viewModels()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_show_profile, container, false)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
        storage = Firebase.storage
    }

    override fun onViewCreated (view: View, savedInstanceState : Bundle?){
        super.onViewCreated(view, savedInstanceState)
        userVm.getUser().observe(viewLifecycleOwner, Observer { it ->
            user = it
            if (user.imagePath.isNullOrEmpty()) {
                image_view.setImageResource(R.drawable.avatar_foreground)
            }
            else{
                downloadFile()
            }
            name_view.text = user.fullname
            nickname_view.text = user.nickname
            email_view.text = user.email
            location_view.text = user.location
            interests_view.text = buildInterestsString()
            ratingBar.rating= user.rating
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
        navController.navigate(R.id.action_showProfileFragment_to_editProfileFragment)
    }

    private fun downloadFile() {
        val storageRef = storage.reference
        storageRef.child(user.imagePath).downloadUrl.addOnSuccessListener {
            Picasso.get().load(it).noFade().placeholder(R.drawable.progress_animation).into(image_view)
        }.addOnFailureListener {
            Log.d("image", "error in download image")
        }
    }
}