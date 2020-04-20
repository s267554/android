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
import java.io.File
import java.util.*



class EditItemFragment : Fragment() {
    private var item: ItemInfo = ItemInfo()
    private var REQUESTCAMERA: Int = 1805
    private var REQUESTGALLERY: Int = 1715
    private var imageModified = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
        arguments?.let {
            item.path = it.getString("group19.lab2.PATH").toString()
            item.title  = it.getString("group19.lab2.TITLE").toString()
            item.location = it.getString("group19.lab2.LOCATION").toString()
            item.expiryDate = it.getString("group19.lab2.EXPIRY_DATE").toString()
            item.category= it.getString("group19.lab2.CATEGORY").toString()
            item.description = it.getString("group19.lab2.DESCRIPTION").toString()
            item.price = it.getFloat("group19.lab2.PRICE")
        }

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_edit_item, container, false)
    }

    override fun onViewCreated (view: View, savedInstanceState : Bundle?){
        super.onViewCreated(view, savedInstanceState)
//       To be completed
//        val file = File(activity?.applicationContext?.filesDir, "myimage.png")
//        if(file.exists()) {
//            item.image = MediaStore.Images.Media.getBitmap(activity?.contentResolver,Uri.fromFile(file))
//            image_view.setImageBitmap(item.image)
//        }

        val titleEditText=view.findViewById<EditText>(R.id.titleEditText)
        titleEditText.setText(item.title)
        val descriptionEditText = view.findViewById<EditText>(R.id.descriptionEditText)
        descriptionEditText.setText(item.description)
        val locationEditText = view.findViewById<EditText>(R.id.locationEditText)
        locationEditText.setText(item.location)
        val priceEditText = view.findViewById<EditText>(R.id.priceEditText)
        priceEditText.setText(item.price.toString())
        val dateEditText = view.findViewById<EditText>(R.id.dateEditText)
        dateEditText.setText(item.expiryDate)
        val categoryEditText = view.findViewById<AutoCompleteTextView>(R.id.categoryDropdown)
        categoryEditText.setText(item.category, false)

        //VALIDATION
        if(item.title.isEmpty())
            view.findViewById<TextInputLayout>(R.id.titleTextField).error = getString(R.string.notEmpty)
        if(item.location.isEmpty())
            view.findViewById<TextInputLayout>(R.id.locationTextField).error = getString(R.string.notEmpty)
        if(item.price.isNaN())
            view.findViewById<TextInputLayout>(R.id.priceTextField).error = getString(R.string.notEmpty)
        if(item.expiryDate.isEmpty())
            view.findViewById<TextInputLayout>(R.id.dateTextField).error = getString(R.string.notEmpty)
        if(item.category.isEmpty())
            view.findViewById<TextInputLayout>(R.id.categoryTextField).error = getString(R.string.notEmpty)
        titleEditText.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(p0: Editable?) {
                if (p0 != null) {
                    if(p0.isEmpty() || p0.isBlank()){
                        view.findViewById<TextInputLayout>(R.id.titleTextField).error = getString(R.string.notEmpty)
                    }
                }
            }
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }
            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                if(!p0.isNullOrBlank() && !p0.isNullOrEmpty())
                    view.findViewById<TextInputLayout>(R.id.titleTextField).error = null
            }
        })
        priceEditText.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(p0: Editable?) {
                if (p0 != null) {
                    if(p0.isEmpty() || p0.isBlank()){
                        view.findViewById<TextInputLayout>(R.id.priceTextField).error = getString(R.string.notEmpty)
                    }
                }
            }
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }
            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                if(!p0.isNullOrBlank() && !p0.isNullOrEmpty())
                    view.findViewById<TextInputLayout>(R.id.priceTextField).error = null
            }
        })
        locationEditText.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(p0: Editable?) {
                if (p0 != null) {
                    if(p0.isEmpty() || p0.isBlank()){
                        view.findViewById<TextInputLayout>(R.id.locationTextField).error = getString(R.string.notEmpty)
                    }
                }
            }
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }
            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                if(!p0.isNullOrBlank() && !p0.isNullOrEmpty())
                    view.findViewById<TextInputLayout>(R.id.locationTextField).error = null
            }
        })
        dateEditText.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(p0: Editable?) {
                if (p0 != null) {
                    if(p0.isEmpty() || p0.isBlank()){
                        view.findViewById<TextInputLayout>(R.id.dateTextField).error = getString(R.string.notEmpty)
                    }
                    else{
                        view.findViewById<TextInputLayout>(R.id.dateTextField).error = null
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
            datePickerDialog?.datePicker?.minDate = System.currentTimeMillis() - 1000;
            datePickerDialog?.show()
        }

        //SPINNER MANAGEMENT
        val items = listOf("Category1", "Category2", "Category3", "Category4")
        val adapter = ArrayAdapter(requireContext(), R.layout.list_item, items)
        categoryEditText?.setAdapter(adapter)

        registerForContextMenu(imageEdit)
        imageRotate.setOnClickListener{
            rotateBitmap()
        }
        imageEdit.setOnClickListener{
            Toast.makeText(this.context, "Keep pressed", Toast.LENGTH_SHORT).show()
        }

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
        if (inflater != null) {
            inflater.inflate(R.menu.pic_selection_menu, menu)
        }
    }

    override fun onContextItemSelected(item: MenuItem): Boolean {
        when(item.itemId){

            R.id.option_camera -> {
                Toast.makeText(this.context, "Camera Lauched", Toast.LENGTH_SHORT).show()
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
                    image_view.setImageBitmap(bitmapImage)
                    item.image = bitmapImage
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

    private fun rotateImage(source: Bitmap, angle: Int): Bitmap {
        val matrix = Matrix()
        matrix.postRotate(angle.toFloat())
        return Bitmap.createBitmap(
            source, 0, 0, source.width, source.height,
            matrix, true
        )
    }

    fun rotateBitmap() {
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
