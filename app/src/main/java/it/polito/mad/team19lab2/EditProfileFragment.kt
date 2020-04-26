package it.polito.mad.team19lab2

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.util.Patterns
import android.view.*
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.google.android.material.navigation.NavigationView
import kotlinx.android.synthetic.main.fragment_edit_profile.*
import kotlinx.android.synthetic.main.fragment_edit_profile.imageEdit
import kotlinx.android.synthetic.main.fragment_edit_profile.image_view
import kotlinx.android.synthetic.main.fragment_edit_profile.roundCardView
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream
import java.io.IOException


class EditProfileFragment : Fragment() {
    var u:UserInfo= UserInfo()

    private val listVOs: MutableList<StateVO> = mutableListOf()
    private var REQUEST_CAMERA: Int = 1805
    private var REQUEST_GALLERY: Int = 1715
    private var imageModified=false

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_edit_profile, container, false)
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
        val sharedPref = activity?.getSharedPreferences(
            "it.polito.mad.team19lab2.profile", 0)
        if(sharedPref!=null) {
            val profile = sharedPref.getString("profile", "notFound")
            if (profile != "notFound") {
                val jo = JSONObject(profile)
                u.fullname = jo.get("FULL_NAME") as String
                u.nickname = jo.get("NICK_NAME") as String
                u.email_address = jo.get("EMAIL") as String
                if(jo.has("LOCATION"))
                    u.location_area = jo.get("LOCATION") as String
                if(jo.has("INTERESTS")){
                    val jsoninterests = jo.getJSONArray("INTERESTS")
                    u.interests.clear()
                    for (i in 0 until jsoninterests.length())
                        u.interests.add(jsoninterests.getInt(i))
                }
                if(jo.has("RATING"))
                    u.rating = (jo.get("RATING") as Int).toFloat()
                if(jo.has("IMG"))
                    u.img = jo.get("IMG") as Int
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        //image management
        val file = File(context?.filesDir, "myimage.png")
        if (file.exists()) {
            u.image = BitmapFactory.decodeFile(file.absolutePath)
        }
        if (u.image == null) {
            image_view.setImageResource(u.img)
        }
        else {
            image_view.setImageBitmap(u.image)
        }
        registerForContextMenu(imageEdit)
        imageRotateProfile.setOnClickListener{
            rotateBitmap()
        }
        imageEdit.setOnClickListener{
            Toast.makeText(this.context, "Keep pressed", Toast.LENGTH_SHORT).show()
        }
        roundCardView.viewTreeObserver.addOnGlobalLayoutListener (object: ViewTreeObserver.OnGlobalLayoutListener{
            override fun onGlobalLayout() {
                roundCardView.radius =  roundCardView.height.toFloat() / 2.0F
                roundCardView.viewTreeObserver.removeOnGlobalLayoutListener(this)
            }
        })

        //SET VALUE
        fullNameProfileEditText.setText(u.fullname)
        nicknameProfileEditText.setText(u.nickname)
        emailProfileEditText.setText(u.email_address)
        locationProfileEditText.setText(u.location_area)
        imageEdit.setOnClickListener {
            Toast.makeText(context, "Keep pressed", Toast.LENGTH_SHORT).show()
        }
        //Initial validation
        if(fullNameProfileEditText == null || fullNameProfileEditText.text?.isEmpty() == true){
            fullNameProfile.error=getString(R.string.notEmpty)
        }
        if(nicknameProfileEditText == null || nicknameProfileEditText.text?.isEmpty() == true){
            nicknameProfile.error=getString(R.string.notEmpty)
        }
        if(emailProfileEditText == null || emailProfileEditText.text?.isEmpty() == true){
            emailProfile.error=getString(R.string.notEmpty)
        }
        //SPINNER
        val selectInterests=this.resources.getStringArray(R.array.categories).toMutableList()
        listVOs.clear()
        var k = 0
        for (i in 0 until selectInterests.size) {
            val stateVO = StateVO()
            stateVO.title = selectInterests[i]
            stateVO.isSelected = false
            if(k < u.interests.size&&i == u.interests[k]){
                k++
                stateVO.isSelected = true
            }
            listVOs.add(stateVO)
        }
        val adapter = MyAdapter(requireContext(), 0, listVOs)
        interestsDropdown.setAdapter(adapter)
        //VALIDATION
        fullNameProfileEditText.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(p0: Editable?) {
                if (p0 != null) {
                    if(p0.isEmpty() || p0.isBlank()){
                        fullNameProfile.error = getString(R.string.notEmpty)
                    }
                }
            }
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }
            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                if(!p0.isNullOrBlank() && !p0.isNullOrEmpty())
                    fullNameProfile.error = null
            }
        })
        nicknameProfileEditText.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(p0: Editable?) {
                if (p0 != null) {
                    if(p0.isEmpty() || p0.isBlank()){
                        nicknameProfile.error = getString(R.string.notEmpty)
                    }
                }
            }
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }
            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                if(!p0.isNullOrBlank() && !p0.isNullOrEmpty())
                    nicknameProfile.error = null
            }
        })
        emailProfileEditText.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(p0: Editable?) {
                if (p0 != null) {
                    if(p0.isEmpty() || p0.isBlank()){
                        emailProfile.error = getString(R.string.notEmpty)
                    }
                }
            }
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }
            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                if(!p0.isNullOrBlank() && !p0.isNullOrEmpty())
                    emailProfile.error = null
            }
        })
        emailProfileEditText.setOnFocusChangeListener { _, hasFocus ->
            if(!hasFocus){
                if(!isValidEmail())
                    emailProfile.error=getString(R.string.invalidEmail)
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.profile_edit_menu, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if(item.itemId == R.id.save_profile_action)
            saveProfile()
        else if(item.itemId == R.id.cancel_profile_action)
            cancelModification()
        return super.onOptionsItemSelected(item)
    }

    private fun saveProfile(){
        val navigationView = requireActivity().findViewById<View>(R.id.nav_view) as NavigationView
        val headerView = navigationView.getHeaderView(0)
        if(fullNameProfileEditText.text.toString().isEmpty() || nicknameProfileEditText.text.toString().isEmpty()){
            Toast.makeText(context,"Insert all required fields",Toast.LENGTH_SHORT).show()
            return
        }
        if(!isValidEmail()){
            Toast.makeText(context,"enter valid email",Toast.LENGTH_SHORT).show()
            return
        }
        if(imageModified && u.image!=null){
            val fileForBundle = File(requireContext().filesDir, "myimage.png")
            try {
                FileOutputStream(fileForBundle.absoluteFile).use { out ->
                    u.image!!.compress(Bitmap.CompressFormat.PNG, 100, out)
                    headerView.findViewById<ImageView>(R.id.header_imageView).setImageBitmap(u.image)
                }
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
        val sharedPref = activity?.getSharedPreferences(
            "it.polito.mad.team19lab2.profile", 0)
        val jo = JSONObject()
        jo.put("FULL_NAME",fullNameProfileEditText.text.toString())
        jo.put("NICK_NAME",nicknameProfileEditText.text.toString())
        jo.put("LOCATION",locationProfileEditText.text.toString())
        jo.put("EMAIL",emailProfileEditText.text.toString())
        val interests= mutableListOf<Int>()
        for(i in listVOs.indices)
            if(listVOs[i].isSelected)
                interests.add(i)
        Log.d("interest", interests.toString())
        if(interests.isNotEmpty())
            jo.put("INTERESTS", JSONObject.wrap(interests))
        with (sharedPref!!.edit()) {
            putString("profile", jo.toString())
            commit()
        }
        headerView.findViewById<TextView>(R.id.header_title_textView).text=nicknameProfileEditText.text
        headerView.findViewById<TextView>(R.id.header_subtitle_textView).text=fullNameProfileEditText.text

        val navController = findNavController()
        navController.navigate(R.id.action_editProfileFragment_to_showProfileFragment)
    }

    private fun isValidEmail(): Boolean {
        val target = emailProfileEditText.text.toString()
        return if (target.isEmpty()) {
            false
        } else {
            Patterns.EMAIL_ADDRESS.matcher(target).matches()
        }
    }

    private fun cancelModification(){
        val navController = findNavController()
        navController.navigateUp()//(R.id.action_editProfileFragment_to_showProfileFragment)
    }

    override fun onCreateContextMenu(menu: ContextMenu, v: View, menuInfo: ContextMenu.ContextMenuInfo?) {
        super.onCreateContextMenu(menu, v, menuInfo)
        val inflater: MenuInflater? = activity?.menuInflater
        inflater?.inflate(R.menu.pic_selection_menu, menu)
    }

    override fun onContextItemSelected(item: MenuItem): Boolean {
        when(item.itemId){
            R.id.option_camera -> {
                Toast.makeText(this.context, "Camera Launched", Toast.LENGTH_SHORT).show()
                Intent(MediaStore.ACTION_IMAGE_CAPTURE).also { takePictureIntent ->
                    activity?.packageManager?.let {
                        takePictureIntent.resolveActivity(it)?.also {
                            startActivityForResult(takePictureIntent, REQUEST_CAMERA)
                        }
                    }
                }
                return true
            }
            R.id.option_gallery -> {
                Toast.makeText(this.context, "Gallery Launched", Toast.LENGTH_SHORT).show()
                Intent(Intent.ACTION_GET_CONTENT).setType("image/*").also { getPictureIntent ->
                    activity?.packageManager?.let {
                        getPictureIntent.resolveActivity(it)?.also {
                            startActivityForResult(getPictureIntent, REQUEST_GALLERY)
                        }
                    }
                }
                return true
            }
            else -> return super.onContextItemSelected(item)
        }
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if(resultCode == Activity.RESULT_OK && data != null) {
            when (requestCode) {
                REQUEST_CAMERA -> {
                    val bitmapImage: Bitmap = (data.extras?.get("data")) as Bitmap
                    image_view.setImageBitmap(bitmapImage)
                    u.image = bitmapImage
                    imageModified = true
                }
                REQUEST_GALLERY -> {
                    val uri: Uri? = data.data
                    if (uri != null) {
                        u.image = MediaStore.Images.Media.getBitmap(activity?.contentResolver, uri)
                        image_view.setImageBitmap(u.image)
                        imageModified = true
                    }
                }
            }
        }
    }

    private fun rotateImage(source: Bitmap, angle: Int): Bitmap {
        val matrix = Matrix()
        matrix.postRotate(angle.toFloat())
        return Bitmap.createBitmap(
            source, 0, 0, source.width, source.height,
            matrix, true
        )
    }

    private fun rotateBitmap() {
        if(u.image == null){
            Toast.makeText(activity?.applicationContext,"insert an image before",Toast.LENGTH_SHORT).show()
            return
        }
        val rotatedBitmap: Bitmap = rotateImage(u.image!!, 90)
        imageModified=true
        image_view.setImageBitmap(rotatedBitmap)
        u.image = rotatedBitmap
    }
}

class StateVO {
    var title: String? = null
    var isSelected = false
}

