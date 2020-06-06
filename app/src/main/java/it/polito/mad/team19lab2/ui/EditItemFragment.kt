package it.polito.mad.team19lab2.ui

import android.app.Activity
import android.app.DatePickerDialog
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
import android.text.InputFilter
import android.text.TextWatcher
import android.util.Log
import android.util.LruCache
import android.view.*
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.AutoCompleteTextView
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.tasks.Task
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.ktx.storage
import com.squareup.picasso.Picasso
import it.polito.mad.team19lab2.R
import it.polito.mad.team19lab2.data.ItemModel
import it.polito.mad.team19lab2.utilities.DropdownAdapter
import it.polito.mad.team19lab2.utilities.PriceInputFilter
import it.polito.mad.team19lab2.utilities.WorkaroundMapFragment
import it.polito.mad.team19lab2.viewModel.ItemViewModel
import kotlinx.android.synthetic.main.fragment_edit_item.*
import kotlinx.android.synthetic.main.fragment_edit_item.imageEdit
import kotlinx.android.synthetic.main.fragment_edit_item.image_view
import kotlinx.android.synthetic.main.fragment_edit_item.progressBar
import kotlinx.android.synthetic.main.fragment_edit_item.roundCardView
import kotlinx.android.synthetic.main.fragment_edit_profile.*
import java.io.ByteArrayOutputStream
import java.util.*


class EditItemFragment : Fragment() {
    private lateinit var item: ItemModel
    private var REQUESTCAMERA: Int = 1805
    private var REQUESTGALLERY: Int = 1715
    private var imageModified = false
    private lateinit var idItem: String
    private var year: Int = 0
    private var month: Int = 0
    private var day: Int=0
    private var newtimestamp: Timestamp?= null
    private val itemVm: ItemViewModel by viewModels()
    private lateinit var image: Bitmap
    lateinit var storage: FirebaseStorage
    var user = FirebaseAuth.getInstance().currentUser
    private lateinit var catArray : MutableList<String>
    private lateinit var subCatArray: MutableList<String>

    private lateinit var supportMapFragment: SupportMapFragment
    private lateinit var client: FusedLocationProviderClient
    private lateinit var gMap: GoogleMap


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
        storage = Firebase.storage
        setHasOptionsMenu(true)
        client= context?.let { LocationServices.getFusedLocationProviderClient(it) }!!
        catArray = resources.getStringArray(R.array.categories).toMutableList()
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


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_edit_item, container, false)
    }

    override fun onViewCreated (view: View, savedInstanceState : Bundle?){
        super.onViewCreated(view, savedInstanceState)
        activity?.window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN)
        idItem = arguments?.getString("item_id1").toString()
        supportMapFragment = ( childFragmentManager.findFragmentById(R.id.google_maps) as WorkaroundMapFragment)
        //Round image management
        roundCardView.viewTreeObserver.addOnGlobalLayoutListener (object: ViewTreeObserver.OnGlobalLayoutListener{
            override fun onGlobalLayout() {
                roundCardView.radius =  roundCardView.height.toFloat() / 2.0F
                roundCardView.viewTreeObserver.removeOnGlobalLayoutListener(this)
            }
        })

        //STATE SPINNER
        val stateSpinner = view.findViewById<AutoCompleteTextView>(R.id.stateDropdown)
        val stateValue = resources.getStringArray(R.array.item_state).toMutableList()
        // CATEGORY SPINNER
        val itemsCategory = resources.getStringArray(R.array.categories).toMutableList()
        val categoryEditText = view.findViewById<AutoCompleteTextView>(R.id.categoryDropdown)
        if(savedInstanceState==null) {
            if (idItem !== "-1") {
                itemVm.getItem(idItem).observe(viewLifecycleOwner, androidx.lifecycle.Observer {
                    item = it
                    titleEditText.setText(item.title)
                    descriptionEditText.setText(item.description)
                    locationEditText.setText(item.location)
                    priceEditText.setText(item.price.toString())
                    dateEditText.setText(item.expiryDate)
                    categoryEditText.setText(itemsCategory[item.category], false)

                    val stateArray = resources.getStringArray(R.array.item_state).toMutableList()
                    stateSpinner.setText(stateArray[item.state])
                    if (item.imagePath.isEmpty()) {
                        image_view.setImageResource(R.mipmap.launcher_icon_no_text)
                    } else {
                        downloadFile()
                    }
                    //VALIDATION
                    if (item.title.isEmpty())
                        titleTextField.error = getString(R.string.notEmpty)
                    if (item.location.isEmpty())
                        locationTextField.error = getString(R.string.notEmpty)
                    if (item.price.isNaN())
                        priceTextField.error = getString(R.string.notEmpty)
                    if (item.expiryDate.isEmpty())
                        dateTextField.error = getString(R.string.notEmpty)
                    if (item.category == -1) {
                        categoryTextField.error = getString(R.string.notEmpty)
                    } else {
                        if (item.category != (catArray.size-1)){
                            subCategoryTextField.visibility = View.VISIBLE
                            manageSubDropdown(item.category)
                            if(item.subcategory != -1 ) {
                                val sub = resources.getIdentifier(
                                    "sub${item.category}",
                                    "array",
                                    context?.packageName
                                )
                                subCatArray = resources.getStringArray(sub).toMutableList()
                                subCategoryDropdown.setText(subCatArray[item.subcategory], false)
                            }
                        }
                    }
                    //MAPS
                    supportMapFragment.getMapAsync {
                        gMap = it
                        gMap.uiSettings.isMapToolbarEnabled = false
                        var mScrollView = scrollParentEditItem //parent scrollview in xml, give your scrollview id value
                        (childFragmentManager.findFragmentById(R.id.google_maps) as WorkaroundMapFragment?)
                            ?.setListener(object : WorkaroundMapFragment.OnTouchListener {
                                override fun onTouch() {
                                    mScrollView.requestDisallowInterceptTouchEvent(true)
                                }
                            })
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
                            locationEditText.setText(finalLocation)
                        }
                        if(item.location.isNullOrEmpty()){
                            if(ActivityCompat.checkSelfPermission(requireContext(), android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                                getCurrentLocation()
                            }
                            else{
                                ActivityCompat.requestPermissions(requireContext() as Activity, Array(1){android.Manifest.permission.ACCESS_FINE_LOCATION}, 44)
                            }
                        }
                        else{
                            pointInMap(locationEditText.text)
                        }
                    }
                })
            } else {
                item = ItemModel()
                item.id = "${System.currentTimeMillis()}-${user?.uid ?: ""}"
                item.userId = user?.uid ?: ""
                image_view.setImageResource(R.mipmap.launcher_icon_no_text)
                titleTextField.error = getString(R.string.notEmpty)
                locationTextField.error = getString(R.string.notEmpty)
                priceTextField.error = getString(R.string.notEmpty)
                dateTextField.error = getString(R.string.notEmpty)
                categoryTextField.error = getString(R.string.notEmpty)
                stateSpinner.setText(stateValue[0])
            }
        }
        else{
            supportMapFragment.getMapAsync {
                gMap = it
                gMap.uiSettings.isMapToolbarEnabled = false
                var mScrollView = scrollParentEditItem //parent scrollview in xml, give your scrollview id value
                (childFragmentManager.findFragmentById(R.id.google_maps) as WorkaroundMapFragment?)
                    ?.setListener(object : WorkaroundMapFragment.OnTouchListener {
                        override fun onTouch() {
                            mScrollView.requestDisallowInterceptTouchEvent(true)
                        }
                    })
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
                    locationEditText.setText(finalLocation)
                }
                if(locationEditText.text.isNullOrEmpty()){
                    if(ActivityCompat.checkSelfPermission(requireContext(), android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                        getCurrentLocation()
                    }
                    else{
                        ActivityCompat.requestPermissions(requireContext() as Activity, Array(1){android.Manifest.permission.ACCESS_FINE_LOCATION}, 44)
                    }
                }
                else{
                    pointInMap(locationEditText.text)
                }
            }
        }
        titleEditText.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(p0: Editable?) {
                if (p0 != null) {
                    if(p0.isEmpty() || p0.isBlank()){
                        titleTextField.error = getString(R.string.notEmpty)
                    }
                }
            }
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }
            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                if(!p0.isNullOrBlank() && !p0.isNullOrEmpty())
                    titleTextField.error = null
            }
        })
        priceEditText.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(p0: Editable?) {
                if (p0 != null) {
                    if(p0.isEmpty() || p0.isBlank()){
                        priceTextField.error = getString(R.string.notEmpty)
                    }
                }
            }
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }
            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                if(!p0.isNullOrBlank() && !p0.isNullOrEmpty())
                    priceTextField.error = null
            }
        })
        locationEditText.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(p0: Editable?) {
                if (p0 != null) {
                    if(p0.isEmpty() || p0.isBlank()){
                        locationTextField.error = getString(R.string.notEmpty)
                    }
                }
            }
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }
            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                if(!p0.isNullOrBlank() && !p0.isNullOrEmpty())
                    locationTextField.error = null
            }
        })
        locationEditText.setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus) {
                pointInMap(locationEditText.text)
            }
        }
        locationEditText.setOnEditorActionListener(
            object : TextView.OnEditorActionListener {
                override fun onEditorAction(
                    v: TextView?,
                    actionId: Int,
                    event: KeyEvent?
                ): Boolean {
                    if (actionId == EditorInfo.IME_ACTION_SEARCH || actionId == EditorInfo.IME_ACTION_DONE || event != null && event.action === KeyEvent.ACTION_DOWN && event.keyCode === KeyEvent.KEYCODE_ENTER
                    ) {
                        if (event == null || !event.isShiftPressed) {
                            pointInMap(locationEditText.text)
                            return true // consume.
                        }
                    }
                    return false // pass on to other listeners.
                }
            }
        )
        dateEditText.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(p0: Editable?) {
                if (p0 != null) {
                    if(p0.isEmpty() || p0.isBlank()){
                        dateTextField.error = getString(R.string.notEmpty)
                    }
                    else{
                        dateTextField.error = null
                    }
                }
            }
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }
            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }
        })
        //END VALIDATION

        //DATE PICKER MANAGEMENT
        val c = Calendar.getInstance()
        year = if(dateEditText.text.isNullOrBlank()) c.get(Calendar.YEAR) else dateEditText.text.toString().split("/")[2].toInt()
        month = if(dateEditText.text.isNullOrBlank()) c.get(Calendar.MONTH) else dateEditText.text.toString().split("/")[1].toInt()-1
        day = if(dateEditText.text.isNullOrBlank() && day == 0) c.get(Calendar.DAY_OF_MONTH) else dateEditText.text.toString().split("/")[0].toInt()
        val calendar: Calendar = GregorianCalendar(year, month, day+1)
        //timestamp=Timestamp(Date(calendar.timeInMillis))

        dateEditText.setOnClickListener {
            val datePickerDialog = activity?.let {
                DatePickerDialog(
                    it,
                    DatePickerDialog.OnDateSetListener { picker, year, monthOfYear, dayOfMonth ->
                        val finalMonth=monthOfYear+1
                        val finalDate="$dayOfMonth/$finalMonth/$year"
                        this.year=year
                        this.month=monthOfYear
                        this.day=dayOfMonth
                        val cal: Calendar = GregorianCalendar(year, monthOfYear, dayOfMonth+1)
                        newtimestamp=Timestamp(Date(cal.timeInMillis))
                        dateEditText.setText(finalDate)
                    }, year, month, day)

            }
            datePickerDialog?.datePicker?.minDate = System.currentTimeMillis() - 1000
            datePickerDialog?.show()
        }
        //SPINNER MANAGEMENT
        val adapter = DropdownAdapter(
            requireContext(),
            R.layout.list_item,
            itemsCategory
        )
        categoryEditText?.setAdapter(adapter)
        categoryEditText.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(p0: Editable?) {
                if (p0 != null) {
                    if (p0.isEmpty() || p0.isBlank()) {
                        categoryTextField.error = getString(R.string.notEmpty)
                        subCategoryTextField.visibility=View.GONE
                    } else {
                        categoryTextField.error = null
                        manageSubDropdown(itemsCategory.indexOf(p0.toString()))
                    }
                }
            }

            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }
        })

        //STATE SPINNER MANAGEMENT
        val dropAdapter = DropdownAdapter(
            requireContext(),
            R.layout.list_item,
            stateValue
        )
        stateSpinner?.setAdapter(dropAdapter)
        stateSpinner.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(p: Editable?) {
                if (p != null) {
                    if (p.isEmpty() || p.isBlank()) {
                        categoryTextField.error = getString(R.string.notEmpty)
                    } else {
                        categoryTextField.error = null
                    }
                }
            }

            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }
        })

        //IMAGE MANAGEMENT
        registerForContextMenu(imageEdit)
        imageRotate.setOnClickListener{
            rotateBitmap()
        }
        imageEdit.setOnClickListener{
            Toast.makeText(this.context, resources.getString(R.string.keep_pressed), Toast.LENGTH_SHORT).show()
        }

        //PRICE MANAGEMENT
        val inputFilter =  arrayOf<InputFilter>(
            PriceInputFilter(
                10,
                2
            )
        )
        priceEditText.filters = inputFilter
    }

    override fun onCreateOptionsMenu(menu: Menu, menuInflater: MenuInflater) {
        menuInflater.inflate(R.menu.item_edit_menu, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if(item.itemId == R.id.save_item_action)
            saveItem()
        return super.onOptionsItemSelected(item)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        if(this::image.isInitialized){
            addBitmapToMemoryCache("TMPIMG", image)
            outState.putBoolean("group19.lab2.IMGFLAG", imageModified)
        }
        if(this::item.isInitialized)
            outState.putParcelable("group19.lab2.ITEM", item)
    }

    private fun addBitmapToMemoryCache(key: String?, bitmap: Bitmap?) {
        mMemoryCache.put(key, bitmap)
    }

    private fun getBitmapFromMemCache(key: String?): Bitmap? {
        return mMemoryCache.get(key)
    }

    override fun onViewStateRestored(savedInstanceState: Bundle?) {
        super.onViewStateRestored(savedInstanceState)
        if (savedInstanceState != null){
            getBitmapFromMemCache("TMPIMG").run {
                if (this != null) {
                    image = this
                    image_view.setImageBitmap(this)
                    imageModified = savedInstanceState.getBoolean("group19.lab2.IMGFLAG")
                }
                else{
                    image_view.setImageResource(R.mipmap.launcher_icon_no_text)
                }
                item= savedInstanceState.getParcelable("group19.lab2.ITEM")!!
                return
            }
        }
    }

    private fun saveItem(){
        //force close keyborard
        context?.let { view?.let { it1 -> hideKeyboardFrom(it, it1) } }
        //check validity of fields
        if(categoryDropdown.text.isNullOrBlank() || dateEditText.text.isNullOrBlank()
            || priceEditText.text.isNullOrBlank() || titleEditText.text.isNullOrBlank()
            || locationEditText.text.isNullOrBlank()){
            Toast.makeText(context,resources.getString(R.string.insert_all_required_fields),Toast.LENGTH_SHORT).show()
            return
        }
        if(idItem!=="-1"){
            item.id = idItem
        }
        val catArray = resources.getStringArray(R.array.categories)

        item.title=titleEditText.text.toString()
        item.description=descriptionEditText.text.toString()
        item.location = locationEditText.text.toString()
        item.price = priceEditText.text.toString().toFloat()
        item.expiryDate = dateEditText.text.toString()
        item.category = catArray.indexOf(categoryDropdown.text.toString())
        if (this::subCatArray.isInitialized)
            item.subcategory = subCatArray.indexOf(subCategoryDropdown.text.toString())
        val stateArray = resources.getStringArray(R.array.item_state)
        item.state = stateArray.indexOf(stateDropdown.text.toString())
        if(newtimestamp!=null)
            item.expireDatestamp= newtimestamp as Timestamp
        clEditItem.visibility=View.GONE
        progressBar.visibility=View.VISIBLE
        // Update or create the image
        if(imageModified && this::image.isInitialized) {
            val time = System.currentTimeMillis()
            var itemPictureRef = storage.reference.child("itemPicture/${time}")
            var path = "itemPicture/${time}"
            if(idItem != "-1") {
                itemPictureRef = storage.reference.child("itemPicture/${idItem}")
                path = "itemPicture/${idItem}"
            }
            item.imageVersion+=1
            item.imagePath = path
            val baos = ByteArrayOutputStream()
            image.compress(Bitmap.CompressFormat.JPEG, 100, baos)
            val data = baos.toByteArray()
            val uploadTask = itemPictureRef.putBytes(data)
            uploadTask.addOnFailureListener {
            }.addOnSuccessListener {
                itemVm.saveItem(item)
                navigateAfterSave()
            }
        }
        else {
            itemVm.saveItem(item)
            navigateAfterSave()
        }
    }

    private fun navigateAfterSave(){
        if(arguments?.getBoolean("deep_link")==true){
            val msg:String?
            if(arguments?.getBoolean("edit")==true)
                msg= resources.getString(R.string.item_update_message)
            else
                msg=resources.getString(R.string.item_create_message)
            Toast.makeText(context,msg, Toast.LENGTH_SHORT).show()
            val bundle = bundleOf("item_id1" to item.id)
            findNavController().navigate(R.id.action_nav_edit_item_to_nav_item_detail, bundle)
        }
        else {
            val  msg= resources.getString(R.string.item_update_message)
            Toast.makeText(context,msg, Toast.LENGTH_SHORT).show()
            val bundle = bundleOf("item_id1" to item.id)
            findNavController().navigate(R.id.action_nav_edit_item_to_nav_item_detail, bundle)
        }
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
                            startActivityForResult(takePictureIntent, REQUESTCAMERA)
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
                            startActivityForResult(getPictureIntent, REQUESTGALLERY)
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
                REQUESTCAMERA -> {
                    val bitmapImage: Bitmap = (data.extras?.get("data")) as Bitmap
                    image = bitmapImage
                    image_view.setImageBitmap(image)
                    imageModified = true
                }
                REQUESTGALLERY -> {
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

    private fun manageSubDropdown(chosenCategory: Int){
        if(chosenCategory == (catArray.size-1)){
            subCategoryDropdown.setText("")
            subCategoryTextField.visibility=View.GONE
            return
        }
        var index = 0
        for(category in catArray){
            if(chosenCategory == catArray.indexOf(category)){
                val subId = resources.getIdentifier("sub${index}", "array", context?.packageName)
                subCatArray=resources.getStringArray(subId).toMutableList()
                val subAdapter=
                    DropdownAdapter(
                        requireContext(),
                        R.layout.list_item,
                        subCatArray
                    )
                subCategoryDropdown.setText("")
                subCategoryDropdown?.setAdapter(subAdapter)
                subCategoryTextField.visibility=View.VISIBLE
                return
            }
            index++
        }
        subCategoryDropdown.setText("")
        subCategoryTextField.visibility=View.GONE
    }

    private fun hideKeyboardFrom(context: Context, view: View) {
        val imm: InputMethodManager = context.getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(view.windowToken, 0)
    }

    private fun downloadFile() {
        val storageRef = storage.reference
        val imView= view?.findViewById<ImageView>(R.id.image_view)
        storageRef.child(item.imagePath).downloadUrl.addOnSuccessListener {
            Picasso.get().load(it).noFade().placeholder(R.drawable.progress_animation).into(imView, object: com.squareup.picasso.Callback {
                override fun onSuccess() {
                    val drawable: BitmapDrawable? = image_view?.drawable as BitmapDrawable?
                    if(drawable != null)
                        image = drawable.bitmap!!
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
                locationEditText.setText(finalLocation)
            }
            else{
                Toast.makeText(requireContext(), R.string.location_not_found, Toast.LENGTH_LONG).show()
            }
        }
    }

}
