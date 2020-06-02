package it.polito.mad.team19lab2.ui

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Matrix
import android.graphics.drawable.BitmapDrawable
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.util.LruCache
import android.util.Patterns
import android.view.*
import android.view.inputmethod.InputMethodManager
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.tasks.Task
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
import java.util.*
import kotlin.collections.ArrayList


class EditProfileFragment : Fragment() {
    private lateinit var user: UserModel
    lateinit var storage: FirebaseStorage
    private lateinit var image: Bitmap

    private val listVOs: MutableList<StateVO> = mutableListOf()
    private var REQUEST_CAMERA: Int = 1805
    private var REQUEST_GALLERY: Int = 1715
    private var imageModified=false
    private val userVm: UserViewModel by viewModels()
    private lateinit var userId : String
    private lateinit var supportMapFragment: SupportMapFragment
    private lateinit var client: FusedLocationProviderClient
    private lateinit var gMap: GoogleMap

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_edit_profile, container, false)
    }

    companion object {
        private val maxMemory : Long = Runtime.getRuntime().maxMemory() / 1024
        private val cacheSize = (maxMemory/4).toInt()
        private val mMemoryCache = object : LruCache<String, Bitmap>(cacheSize) {
            override fun sizeOf(key: String?, bitmap: Bitmap): Int {
                return bitmap.byteCount / 1024
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
        storage = Firebase.storage
        client= context?.let { LocationServices.getFusedLocationProviderClient(it) }!!
    }

    private fun getCurrentLocation(){
        var task: Task<Location> = client.lastLocation
        task.addOnSuccessListener {location ->
            if(location!=null){
                supportMapFragment.getMapAsync {
                    var latLng = com.google.android.gms.maps.model.LatLng(location.latitude, location.longitude)
                    it.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 10F))
                }
            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if(requestCode == 44){
            if(grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                getCurrentLocation()
            }
        }
    }


    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        val interestsRecover= mutableListOf<Int>()
        for(i in listVOs.indices)
            if(listVOs[i].isSelected)
                interestsRecover.add(i)
        outState.putIntArray("group19.lab2.INTERESTS",interestsRecover.toIntArray())
        outState.putParcelable("group19.lab2.USER", user)
        if(this::image.isInitialized){
            addBitmapToMemoryCache("IMG", image)
            outState.putBoolean("group19.lab2.IMGFLAG", imageModified)
        }
    }

    override fun onViewStateRestored(savedInstanceState: Bundle?) {
        super.onViewStateRestored(savedInstanceState)
        if(savedInstanceState!=null) {
            user = savedInstanceState.getParcelable("group19.lab2.USER")!!
            val interestsRestored = savedInstanceState.getIntArray("group19.lab2.INTERESTS")!!.toMutableList()
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
            val imageRestored: Bitmap? = getBitmapFromMemCache("IMG")
            if (imageRestored != null) {
                image = imageRestored
                image_view.setImageBitmap(imageRestored)
                imageModified = savedInstanceState.getBoolean("group19.lab2.IMGFLAG")
            }
        }
    }

    private fun addBitmapToMemoryCache(key: String?, bitmap: Bitmap?) {
        mMemoryCache.put(key, bitmap)
    }

    private fun getBitmapFromMemCache(key: String?): Bitmap? {
        return mMemoryCache.get(key)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        activity?.window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_UNCHANGED)
        userId = arguments?.getString("user_id").toString()
        supportMapFragment = ( childFragmentManager.findFragmentById(R.id.google_maps) as SupportMapFragment)
        supportMapFragment.getMapAsync {
            gMap = it
            gMap.setOnCameraMoveStartedListener {
                val i : ViewParent = view as ViewParent
                i.requestDisallowInterceptTouchEvent(true)
            }
            gMap.setOnCameraIdleListener {
                val i : ViewParent = view as ViewParent
                i.requestDisallowInterceptTouchEvent(false)
            }
            gMap.setOnMapClickListener {point->
                var markerPosition = MarkerOptions()
                markerPosition.position(point)
                gMap.clear()
                gMap.animateCamera(CameraUpdateFactory.newLatLngZoom(point, 10F))
                gMap.addMarker(markerPosition)
                val geocoder = Geocoder(context, Locale.getDefault())
                val addresses: List<Address> = geocoder.getFromLocation(point.latitude, point.longitude, 1)
                val streetName: String = addresses[0].getAddressLine(0).split(",")[0]
                val cityName = addresses[0].locality
                val countryCode = addresses[0].countryCode
                val finalLocation= "${cityName}, $countryCode"
                locationProfileEditText.setText(finalLocation)
            }
        }
        if(savedInstanceState==null) {
            userVm.getUser(userId).observe(viewLifecycleOwner, Observer { it ->
                user = it
                fullNameProfileEditText.setText(user.fullname)
                nicknameProfileEditText.setText(user.nickname)
                emailProfileEditText.setText(user.email)
                locationProfileEditText.setText(user.location)
                //IMAGE
                if (user.imagePath.isEmpty()) {
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
                if(user.location.isNullOrEmpty()){
                    if(ActivityCompat.checkSelfPermission(requireContext(), android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                        getCurrentLocation()
                    }
                    else{
                        ActivityCompat.requestPermissions(requireContext() as Activity, Array(1){android.Manifest.permission.ACCESS_FINE_LOCATION}, 44)
                    }
                }
                else{
                    pointInMap(locationProfileEditText.text)
                }
                (requireContext() as MainActivity).setInterestsDropdown(listVOs as ArrayList<StateVO>)
            })
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
        locationProfileEditText.setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus) {
                pointInMap(locationProfileEditText.text)
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
        clEditProfile.visibility=View.GONE
        progressBar.visibility=View.VISIBLE
        if(this::image.isInitialized && imageModified) {
                headerView.findViewById<ImageView>(R.id.header_imageView).setImageBitmap(image)
                val profilePictureRef=storage.reference.child("profilePicture/${user.id}")
                val path="profilePicture/${user.id}"
                user.imagePath = path
                val baos = ByteArrayOutputStream()
                image.compress(Bitmap.CompressFormat.JPEG, 100, baos)
                val data = baos.toByteArray()
                var uploadTask = profilePictureRef.putBytes(data)
                uploadTask.addOnFailureListener {
                }.addOnSuccessListener {
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
        if(!this::image.isInitialized){
            Toast.makeText(activity?.applicationContext,resources.getString(R.string.insert_image_before),Toast.LENGTH_SHORT).show()
            return
        }
        val rotatedBitmap: Bitmap = rotateImage(image, 90)
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
        val imView= view?.findViewById<ImageView>(R.id.image_view)
        storageRef.child(user.imagePath).downloadUrl.addOnSuccessListener {
            Picasso.get().load(it).noFade().placeholder(R.drawable.progress_animation).into(imView, object: com.squareup.picasso.Callback {
                override fun onSuccess() {
                    val drawable: BitmapDrawable? = image_view?.drawable as BitmapDrawable?
                    if(drawable!=null)
                        image = drawable.bitmap
                }
                override fun onError(e: java.lang.Exception?) {
                }
            })
        }.addOnFailureListener {
        }
    }

    private fun pointInMap(location: Editable?){
        val geocoder = Geocoder(context, Locale.getDefault())
        gMap.clear()
        if(!location.isNullOrEmpty()) {
            val addresses: List<Address> =
                geocoder.getFromLocationName(location.toString(), 1)
            if(addresses.isNotEmpty() && addresses[0].hasLatitude() && addresses[0].hasLongitude()) {
                val markerPosition = MarkerOptions()
                val point = LatLng(addresses[0].latitude, addresses[0].longitude)
                markerPosition.position(point)
                gMap.animateCamera(CameraUpdateFactory.newLatLngZoom(point, 10F))
                gMap.addMarker(markerPosition)
                val cityName = addresses[0].locality
                val countryCode = addresses[0].countryCode
                val finalLocation= "${cityName}, $countryCode"
                locationProfileEditText.setText(finalLocation)
            }
        }
    }
}

class StateVO {
    var title: String? = null
    var isSelected = false
}


