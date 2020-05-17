package it.polito.mad.team19lab2.ui

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Matrix
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.util.Patterns
import android.view.*
import android.view.inputmethod.InputMethodManager
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.google.android.material.navigation.NavigationView
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.ktx.storage
import com.squareup.picasso.Picasso
import it.polito.mad.team19lab2.MainActivity
import it.polito.mad.team19lab2.R
import it.polito.mad.team19lab2.data.UserModel
import it.polito.mad.team19lab2.viewModel.UserViewModel
import kotlinx.android.synthetic.main.fragment_edit_profile.*
import java.io.ByteArrayOutputStream


class EditProfileFragment : Fragment() {
    private lateinit var user: UserModel
    lateinit var storage: FirebaseStorage
    private lateinit var image: Bitmap

    private val listVOs: MutableList<StateVO> = mutableListOf()
    private var REQUEST_CAMERA: Int = 1805
    private var REQUEST_GALLERY: Int = 1715
    private var imageModified=false
    private var screenRotation = false
    private val userVm: UserViewModel by viewModels()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_edit_profile, container, false)
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
        storage = Firebase.storage
    }


    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        Log.d("SCREEN_ROTATION", "onSaveInstanceState")
        val interestsRecover= mutableListOf<Int>()
        for(i in listVOs.indices)
            if(listVOs[i].isSelected)
                interestsRecover.add(i)
        outState.putIntArray("group19.lab2.INTERESTS",interestsRecover.toIntArray())
        if(this::image.isInitialized){
            outState.putParcelable("group19.lab2.IMG", image)
            outState.putBoolean("group19.lab2.IMGFLAG", imageModified)
        }
    }

    override fun onViewStateRestored(savedInstanceState: Bundle?) {
        super.onViewStateRestored(savedInstanceState)
        if(savedInstanceState!=null) {
            val interestsRestored =
                savedInstanceState.getIntArray("group19.lab2.INTERESTS")!!.toMutableList()
            val selectInterests = this.resources.getStringArray(R.array.categories).toMutableList()
            listVOs.clear()
            var k = 0
            for (i in 0 until selectInterests.size) {
                val stateVO = StateVO()
                stateVO.title = selectInterests[i]
                stateVO.isSelected = false
                if (k < interestsRestored.size && i == interestsRestored[k]) {
                    k++
                    stateVO.isSelected = true
                }
                listVOs.add(stateVO)
            }
            (requireContext() as MainActivity).setInterestsDropdown(listVOs as ArrayList<StateVO>)
            val adapter =
                MyAdapter(
                    requireContext(),
                    0,
                    listVOs
                )
            interestsDropdown.setAdapter(adapter)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        activity?.window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_UNCHANGED)
        Log.d("SCREEN_ROTATION", "onViewCreated")
        if(savedInstanceState==null) {
            userVm.getUser().observe(viewLifecycleOwner, Observer { it ->
                user = it
                fullNameProfileEditText.setText(user.fullname)
                nicknameProfileEditText.setText(user.nickname)
                emailProfileEditText.setText(user.email)
                locationProfileEditText.setText(user.location)
                //IMAGE
                if (user.imagePath.isNullOrEmpty()) {
                    image_view.setImageResource(R.drawable.avatar_foreground)
                } else {
                    downloadFile()
                }
                //SPINNER
                val selectInterests = this.resources.getStringArray(R.array.categories).toMutableList()
                listVOs.clear()
                var k = 0
                for (i in 0 until selectInterests.size) {
                    val stateVO = StateVO()
                    stateVO.title = selectInterests[i]
                    stateVO.isSelected = false
                    if (k < user.interests.size && i == user.interests[k]) {
                        k++
                        stateVO.isSelected = true
                    }
                    listVOs.add(stateVO)
                }
                (requireContext() as MainActivity).setInterestsDropdown(listVOs as ArrayList<StateVO>)
            })
        }
        else{
            val imageRestored: Bitmap? = savedInstanceState.getParcelable("group19.lab2.IMG")
            if (imageRestored != null) {
                image = imageRestored
                image_view.setImageBitmap(imageRestored)
                imageModified = savedInstanceState.getBoolean("group19.lab2.IMGFLAG")
            }
        }
        registerForContextMenu(imageEdit)
        imageRotateProfile.setOnClickListener{
            rotateBitmap()
        }
        imageEdit.setOnClickListener{
            Toast.makeText(this.context, resources.getString(R.string.keep_pressed), Toast.LENGTH_SHORT).show()
        }
        roundCardView.viewTreeObserver.addOnGlobalLayoutListener (object: ViewTreeObserver.OnGlobalLayoutListener{
            override fun onGlobalLayout() {
                roundCardView.radius =  roundCardView.height.toFloat() / 2.0F
                roundCardView.viewTreeObserver.removeOnGlobalLayoutListener(this)
            }
        })
        val adapter =
            MyAdapter(
                requireContext(),
                0,
                listVOs
            )
        interestsDropdown.setAdapter(adapter)
        imageEdit.setOnClickListener {
            Toast.makeText(context, resources.getString(R.string.keep_pressed), Toast.LENGTH_SHORT).show()
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
        context?.let { view?.let { it1 -> hideKeyboardFrom(it, it1) } }
        val navigationView = requireActivity().findViewById<View>(R.id.nav_view) as NavigationView
        val headerView = navigationView.getHeaderView(0)
        if(fullNameProfileEditText.text.toString().isEmpty() || nicknameProfileEditText.text.toString().isEmpty()){
            Toast.makeText(context,resources.getString(R.string.insert_all_required_fields),Toast.LENGTH_SHORT).show()
            return
        }
        if(!isValidEmail()){
            Toast.makeText(context,resources.getString(R.string.invalidEmail),Toast.LENGTH_SHORT).show()
            return
        }
        if(!this::user.isInitialized){
            user = UserModel()
        }
        user.fullname = fullNameProfileEditText.text.toString()
        user.nickname = nicknameProfileEditText.text.toString()
        user.email = emailProfileEditText.text.toString()
        user.location = locationProfileEditText.text.toString()
        val interests = mutableListOf<Int>()
        for (i in listVOs.indices)
            if (listVOs[i].isSelected)
                interests.add(i)
        if (interests.isNotEmpty())
            user.interests = interests
        if(imageModified && image!=null) {
                headerView.findViewById<ImageView>(R.id.header_imageView).setImageBitmap(image)
                val profilePictureRef=storage.reference.child("profilePicture/${user.nickname}")
                val path="profilePicture/${user.nickname}"
                user.imagePath = path
                val baos = ByteArrayOutputStream()
                image.compress(Bitmap.CompressFormat.JPEG, 20, baos)
                val data = baos.toByteArray()
                var uploadTask = profilePictureRef.putBytes(data)
                uploadTask.addOnFailureListener {
                    Log.e("profilePicture", "Error in upload image")
                }.addOnSuccessListener {
                    Log.d("profilePicture", "ProfilePicture loaded correctly")
                    userVm.saveUser(user)
                    headerView.findViewById<TextView>(R.id.header_title_textView).text =
                        nicknameProfileEditText.text
                    headerView.findViewById<TextView>(R.id.header_subtitle_textView).text =
                        fullNameProfileEditText.text
                    Toast.makeText(
                        context,
                        resources.getString(R.string.profile_updated),
                        Toast.LENGTH_SHORT
                    ).show()
                    val navController = findNavController()
                    navController.navigate(R.id.action_editProfileFragment_to_showProfileFragment)
                }
        }
        else {
            userVm.saveUser(user)
            headerView.findViewById<TextView>(R.id.header_title_textView).text =
                nicknameProfileEditText.text
            headerView.findViewById<TextView>(R.id.header_subtitle_textView).text =
                fullNameProfileEditText.text
            Toast.makeText(
                context,
                resources.getString(R.string.profile_updated),
                Toast.LENGTH_SHORT
            ).show()
            val navController = findNavController()
            navController.navigate(R.id.action_editProfileFragment_to_showProfileFragment)
        }
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
                Toast.makeText(this.context, resources.getString(R.string.camera_launched), Toast.LENGTH_SHORT).show()
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
                Toast.makeText(this.context, resources.getString(R.string.gallery_launched), Toast.LENGTH_SHORT).show()
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
                    image = bitmapImage
                    imageModified = true
                }
                REQUEST_GALLERY -> {
                    val uri: Uri? = data.data
                    if (uri != null) {
                        image = MediaStore.Images.Media.getBitmap(activity?.contentResolver, uri)
                        image_view.setImageBitmap(image)
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
        if(image == null){
            Toast.makeText(activity?.applicationContext,resources.getString(R.string.insert_image_before),Toast.LENGTH_SHORT).show()
            return
        }
        val rotatedBitmap: Bitmap = rotateImage(image!!, 90)
        imageModified=true
        image_view.setImageBitmap(rotatedBitmap)
        image = rotatedBitmap
    }

    private fun hideKeyboardFrom(context: Context, view: View) {
        val imm: InputMethodManager = context.getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(view.windowToken, 0)
    }

    private fun downloadFile() {
        val storageRef = storage.reference
        storageRef.child(user.imagePath).downloadUrl.addOnSuccessListener {
            Picasso.get().load(it).noFade().placeholder(R.drawable.progress_animation).into(image_view, object: com.squareup.picasso.Callback {
                override fun onSuccess() {
                    val drawable: BitmapDrawable = image_view.drawable as BitmapDrawable
                    image = drawable.bitmap
                }
                override fun onError(e: java.lang.Exception?) {
                }
            })
        }.addOnFailureListener {
            Log.d("image", "error in download image")
        }
    }

}

class StateVO {
    var title: String? = null
    var isSelected = false
}

