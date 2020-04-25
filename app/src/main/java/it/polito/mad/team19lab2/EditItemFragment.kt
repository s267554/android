package it.polito.mad.team19lab2

import android.app.Activity
import android.app.DatePickerDialog
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Matrix
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.*
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.android.material.textfield.TextInputLayout
import kotlinx.android.synthetic.main.fragment_edit_item.*
import kotlinx.android.synthetic.main.fragment_edit_item.image_view
import kotlinx.android.synthetic.main.fragment_edit_item.roundCardView
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.util.*



class EditItemFragment : Fragment() {
    private var item: ItemInfo = ItemInfo()
    private var REQUESTCAMERA: Int = 1805
    private var REQUESTGALLERY: Int = 1715
    private var imageModified = false
    private var id_item: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
        Log.d("spinner", "onCreate")
        id_item = "item_id"
        val sharedPref = activity?.getSharedPreferences(
            "it.polito.mad.team19lab2"+id_item, 0)
        if (sharedPref != null) {
            val currentItem = sharedPref.getString("item", "notFound")
            if (currentItem != "notFound") {
                val jo = JSONObject(currentItem)
                item.title = jo.get("TITLE").toString()
                item.description = jo.get("DESCRIPTION").toString()
                item.location = jo.get("LOCATION").toString()
                item.price = jo.get("PRICE").toString().toFloat()
                item.expiryDate = jo.get("DATE").toString()
                item.category = jo.get("CATEGORY").toString()
                //item.path = jo.get("PATH").toString()
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
        activity?.window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        val file = File(activity?.applicationContext?.filesDir, "$id_item.png")
        if(file.exists()) {
            item.image = MediaStore.Images.Media.getBitmap(activity?.contentResolver,Uri.fromFile(file))
            image_view.setImageBitmap(item.image)
        }

        //Round image management
        roundCardView.viewTreeObserver.addOnGlobalLayoutListener (object: ViewTreeObserver.OnGlobalLayoutListener{
            override fun onGlobalLayout() {
                roundCardView.radius =  roundCardView.height.toFloat() / 2.0F
                roundCardView.viewTreeObserver.removeOnGlobalLayoutListener(this)
            }
        })
        Log.d("spinner", "onViewCreated")
        titleEditText.setText(item.title)
        descriptionEditText.setText(item.description)
        locationEditText.setText(item.location)
        priceEditText.setText(item.price.toString())
        dateEditText.setText(item.expiryDate)
        val categoryEditText = view.findViewById<AutoCompleteTextView>(R.id.categoryDropdown)

        //VALIDATION
        if(item.title.isEmpty())
            titleTextField.error = getString(R.string.notEmpty)
        if(item.location.isEmpty())
            locationTextField.error = getString(R.string.notEmpty)
        if(item.price.isNaN())
            priceTextField.error = getString(R.string.notEmpty)
        if(item.expiryDate.isEmpty())
            dateTextField.error = getString(R.string.notEmpty)
        if(item.category.isEmpty()) {
            categoryTextField.error = getString(R.string.notEmpty)
        }
        else{
            if(item.category != "other")
                subCategoryTextField.visibility=View.VISIBLE
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
        val year = c.get(Calendar.YEAR)
        val month = c.get(Calendar.MONTH)
        val day = c.get(Calendar.DAY_OF_MONTH)
        dateEditText.setOnClickListener {
            val datePickerDialog = activity?.let {
                DatePickerDialog(
                    it,
                    DatePickerDialog.OnDateSetListener { _, year, monthOfYear, dayOfMonth ->
                        dateEditText.setText("$dayOfMonth/$monthOfYear/$year")
                    }, year, month, day)
            }
            datePickerDialog?.datePicker?.minDate = System.currentTimeMillis() - 1000
            datePickerDialog?.show()
        }
        Log.d("spinner", "eccomi")
        //SPINNER MANAGEMENT
        val items = resources.getStringArray(R.array.categories).toMutableList()
        Log.d("spinner", items.toString())
        val adapter = DropdownAdapter(requireContext(), R.layout.list_item, items)
        categoryEditText?.setAdapter(adapter)
        categoryEditText.setText(item.category, false)
        categoryEditText.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(p0: Editable?) {
                if (p0 != null) {
                    if (p0.isEmpty() || p0.isBlank()) {
                        categoryTextField.error = getString(R.string.notEmpty)
                        subCategoryTextField.visibility=View.GONE
                    } else {
                        categoryTextField.error = null
                        if(p0.toString()=="other"){
                            subCategoryTextField.visibility=View.GONE
                            return
                        }
                        for(category in items){
                            Log.d("spinner", category)
                            Log.d("spinner", p0.toString())
                            if(p0.toString() == category){
                                var subcategories = resources.getStringArray(R.array.sub1).toMutableList()
                                var subAdapter= DropdownAdapter(requireContext(), R.layout.list_item, subcategories)
                                subCategoryDropdown?.setAdapter(subAdapter)
                                subCategoryTextField.visibility=View.VISIBLE
                                return
                            }
                        }
                        subCategoryTextField.visibility=View.GONE
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
            Toast.makeText(this.context, "Keep pressed", Toast.LENGTH_SHORT).show()
        }
        view.findViewById<AutoCompleteTextView>(R.id.categoryDropdown).requestFocus();
    }

    companion object {
        fun newInstance() = EditItemFragment()
    }

    override fun onCreateOptionsMenu(menu: Menu, menuInflater: MenuInflater) {
        menuInflater.inflate(R.menu.item_edit_menu, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if(item.itemId == R.id.save_item_action)
            saveItem()
        return super.onOptionsItemSelected(item)
    }

    private fun saveItem(){
        val b=Bundle()
        populateBundle(b)

        // Update or create the image
        if(imageModified && item.image!=null){
            val fileForBundle = File(activity?.applicationContext?.filesDir, "$id_item.png")
            try {
                FileOutputStream(fileForBundle.absoluteFile).use { out ->
                    item.image!!.compress(Bitmap.CompressFormat.PNG, 100, out)
                }
            } catch (e: IOException) {
                e.printStackTrace()
            }
            item.path = "${activity?.applicationContext?.filesDir.toString()}$id_item.png"
        }

        // Update or create Shared Prefs
        var category=categoryDropdown.text.toString()
        if(subCategoryDropdown.text.isNotEmpty()){
            category+=", ${subCategoryDropdown.text}"
        }
        Log.d("xxx", "Save pressed")
        val sharedPref = activity?.getSharedPreferences(
            "it.polito.mad.team19lab2"+id_item, 0)
        val jo = JSONObject()
        jo.put("TITLE",titleEditText.text.toString())
        jo.put("DESCRIPTION",descriptionEditText.text.toString())
        jo.put("LOCATION",locationEditText.text.toString())
        jo.put("PRICE",priceEditText.text.toString())
        jo.put("DATE",dateEditText.text.toString())
        jo.put("CATEGORY", category)
        //jo.put("PATH", item.path)
        with (sharedPref!!.edit()) {
            putString("item", jo.toString())
            commit()
        }
        val navController = findNavController()
        navController.navigate(R.id.action_nav_edit_item_to_nav_item_detail, b)
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
                Toast.makeText(this.context, "Camera Launched", Toast.LENGTH_SHORT).show()
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
                Toast.makeText(this.context, "Gallery Launched", Toast.LENGTH_SHORT).show()
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
                    item.image = bitmapImage
                    image_view.setImageBitmap(item.image)
                    imageModified = true
                }
                REQUESTGALLERY -> {
                    val uri: Uri? = data.data
                    if (uri != null) {
                        item.image = MediaStore.Images.Media.getBitmap(activity?.contentResolver, uri)
                        image_view.setImageBitmap(item.image)
                        imageModified = true
                    }
                }
            }
        }
    }


    fun rotateImage(source: Bitmap, angle: Int): Bitmap {
        val matrix = Matrix()
        matrix.postRotate(angle.toFloat())
        return Bitmap.createBitmap(
            source, 0, 0, source.width, source.height,
            matrix, true
        )
    }

    private fun rotateBitmap() {
        if(item.image == null){
            Toast.makeText(activity?.applicationContext,"insert an image before",Toast.LENGTH_SHORT).show()
            return
        }
        val rotatedBitmap: Bitmap = rotateImage(item.image!!, 90)
        imageModified=true
        image_view.setImageBitmap(rotatedBitmap)
        item.image = rotatedBitmap
    }
}
