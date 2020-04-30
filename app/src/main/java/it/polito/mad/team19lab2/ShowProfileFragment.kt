package it.polito.mad.team19lab2

import android.graphics.BitmapFactory
import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import kotlinx.android.synthetic.main.fragment_show_profile.*
import kotlinx.android.synthetic.main.fragment_show_profile.image_view
import kotlinx.android.synthetic.main.fragment_show_profile.roundCardView
import org.json.JSONObject
import java.io.File

class ShowProfileFragment :Fragment() {

    private var user: UserInfo = UserInfo()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_show_profile, container, false)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
        //SHARED PREFERENCES
        val sharedPref = activity?.getSharedPreferences(
            "it.polito.mad.team19lab2.profile", 0)
        //read preferences
        if(sharedPref!=null) {
            val profile = sharedPref.getString("profile", "notFound")
            if (profile != "notFound") {
                val jo = JSONObject(profile)
                user.fullname = jo.get("FULL_NAME") as String
                user.nickname = jo.get("NICK_NAME") as String
                user.email_address = jo.get("EMAIL") as String
                if(jo.has("LOCATION"))
                    user.location_area = jo.get("LOCATION") as String
                if(jo.has("INTERESTS")){
                val jsoninterests = jo.getJSONArray("INTERESTS")
                    user.interests.clear()
                    for (i in 0 until jsoninterests.length())
                        user.interests.add(jsoninterests.getInt(i))
                }
                if(jo.has("RATING"))
                user.rating = (jo.get("RATING") as Int).toFloat()
                if(jo.has("IMG"))
                    user.img = jo.get("IMG") as Int
            }
        }

    }


    override fun onViewCreated (view: View, savedInstanceState : Bundle?){
        super.onViewCreated(view, savedInstanceState)
        val file = File(context?.filesDir, "myimage.png")
        if (file.exists()) {
            user.image = BitmapFactory.decodeFile(file.absolutePath)
        }

        if (user.image == null) {
            image_view.setImageResource(user.img)
        }
        else {
            image_view.setImageBitmap(user.image)
        }
        name_view.text = user.fullname
        nickname_view.text = user.nickname
        email_view.text = user.email_address
        location_view.text = user.location_area
        interests_view.text = buildInterestsString()
        // To show a default value
        ratingBar.rating= 2.5F
        //Round image management
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
    //populate and restore bundle do not make the updating visible, only the property are updated


}