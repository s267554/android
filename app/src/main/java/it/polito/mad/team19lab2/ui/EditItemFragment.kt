package it.polito.mad.team19lab2.ui

import android.app.Activity
import android.app.DatePickerDialog
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Matrix
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.text.Editable
import android.text.InputFilter
import android.text.TextWatcher
import android.util.Log
import android.util.LruCache
import android.view.*
import android.view.inputmethod.InputMethodManager
import android.widget.AutoCompleteTextView
import android.widget.Toast
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
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
import it.polito.mad.team19lab2.viewModel.ItemViewModel
import kotlinx.android.synthetic.main.fragment_edit_item.*
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
    private var timestamp: Timestamp= Timestamp.now()
    private val itemVm: ItemViewModel by viewModels()
    private lateinit var image: Bitmap
    lateinit var storage: FirebaseStorage
    var user = FirebaseAuth.getInstance().currentUser

    companion object {
        private val maxMemory : Long = Runtime.getRuntime().maxMemory() / 1024;
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
                    categoryEditText.setText(item.category, false)
                    if(!item.state.isNullOrEmpty())
                        stateSpinner.setText(item.state)
                    else
                        stateSpinner.setText("Available")
                    if (item.imagePath.isNullOrEmpty()) {
                        image_view.setImageResource(R.drawable.sport_category_foreground)
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
                    if (item.category.isEmpty()) {
                        categoryTextField.error = getString(R.string.notEmpty)
                    } else {
                        if (item.category != "other") {
                            subCategoryTextField.visibility = View.VISIBLE
                            manageSubDropdown(item.category, itemsCategory)
                            subCategoryDropdown.setText(item.subcategory, false)
                        }
                    }
                })
            } else {
                item = ItemModel()
                item.id = "${System.currentTimeMillis()}-${user?.uid ?: ""}"
                item.userId = user?.uid ?: ""
                image_view.setImageResource(R.drawable.sport_category_foreground)
                titleTextField.error = getString(R.string.notEmpty)
                locationTextField.error = getString(R.string.notEmpty)
                priceTextField.error = getString(R.string.notEmpty)
                dateTextField.error = getString(R.string.notEmpty)
                categoryTextField.error = getString(R.string.notEmpty)
                stateSpinner.setText("Available")
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
        timestamp=Timestamp(Date(calendar.timeInMillis))
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
                        val calendar: Calendar = GregorianCalendar(year, monthOfYear, dayOfMonth+1)
                        timestamp=Timestamp(Date(calendar.timeInMillis))
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
                        manageSubDropdown(p0.toString(), itemsCategory)
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
            //outState.putParcelable("group19.lab2.TMPIMG", image)
            outState.putBoolean("group19.lab2.IMGFLAG", imageModified)
        }
        if(this::item.isInitialized)
            outState.putParcelable("group19.lab2.ITEM", item)
    }

    private fun addBitmapToMemoryCache(key: String?, bitmap: Bitmap?) {
        mMemoryCache!!.put(key, bitmap)
    }

    private fun getBitmapFromMemCache(key: String?): Bitmap? {
        return mMemoryCache!!.get(key)
    }

    override fun onViewStateRestored(savedInstanceState: Bundle?) {
        super.onViewStateRestored(savedInstanceState)
        if (savedInstanceState != null){
            getBitmapFromMemCache("TMPIMG").run {
                Log.d("IMAGE", this.toString())
                if (this != null) {
                    image = this
                    image_view.setImageBitmap(this)
                    imageModified = savedInstanceState.getBoolean("group19.lab2.IMGFLAG")
                }
                else{
                    image_view.setImageResource(R.drawable.sport_category_foreground)
                }
                item= savedInstanceState.getParcelable("group19.lab2.ITEM")!!
                return
            }//savedInstanceState.getParcelable("group19.lab2.TMPIMG")
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
        item.title=titleEditText.text.toString()
        item.description=descriptionEditText.text.toString()
        item.location = locationEditText.text.toString()
        item.price = priceEditText.text.toString().toFloat()
        item.expiryDate = dateEditText.text.toString()
        item.category = categoryDropdown.text.toString()
        item.subcategory = subCategoryDropdown.text.toString()
        item.state = stateDropdown.text.toString()
        item.expireDatestamp=timestamp

        // Update or create the image
        if(imageModified && image!=null) {
            val itemPictureRef=storage.reference.child("itemPicture/${idItem}")
            val path="itemPicture/${idItem}"
            item.imagePath = path
            val baos = ByteArrayOutputStream()
            image.compress(Bitmap.CompressFormat.JPEG, 20, baos)
            val data = baos.toByteArray()
            var uploadTask = itemPictureRef.putBytes(data)
            uploadTask.addOnFailureListener {
                Log.e("profilePicture", "Error in upload image")
            }.addOnSuccessListener {
                Log.d("profilePicture", "ProfilePicture loaded correctly")
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

    private fun populateBundle(b:Bundle){
        b.putString("group19.lab2.TITLE", titleEditText.text.toString())
        b.putString("group19.lab2.DESCRIPTION", descriptionEditText.text.toString())
        b.putString("group19.lab2.CATEGORY",item.category)
        b.putString("group19.lab2.LOCATION",locationEditText.text.toString())
        b.putFloat("group19.lab2.PRICE", priceEditText.text.toString().toFloat())
        b.putString("group19.lab2.EXPIRY_DATE", dateEditText.text.toString())
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
                        if(image.byteCount > 1000000){

                        }
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

    private fun manageSubDropdown(chosenCategory: String, categories : MutableList<String>){
        if(chosenCategory == "other"){
            subCategoryDropdown.setText("")
            subCategoryTextField.visibility=View.GONE
            return
        }
        var index = 1
        for(category in categories){
            if(chosenCategory == category){
                val subId = resources.getIdentifier("sub${index}", "array", context?.packageName)
                val subcategories=resources.getStringArray(subId).toMutableList()
                val subAdapter=
                    DropdownAdapter(
                        requireContext(),
                        R.layout.list_item,
                        subcategories
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
        storageRef.child(item.imagePath).downloadUrl.addOnSuccessListener {
            Picasso.get().load(it).noFade().placeholder(R.drawable.progress_animation).into(image_view, object: com.squareup.picasso.Callback {
                override fun onSuccess() {
                    val drawable: BitmapDrawable? = image_view?.drawable as BitmapDrawable?
                    if(drawable!=null)
                        image = drawable.bitmap!!
                }
                override fun onError(e: java.lang.Exception?) {
                    Log.e("IMAGE","something went wrong")
                }
            })
        }.addOnFailureListener {
            Log.d("image", "error in download image")
        }
    }
}