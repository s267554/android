package it.polito.mad.team19lab2

import android.app.DatePickerDialog
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.*
import android.view.View.OnTouchListener
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.EditText
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.android.material.textfield.TextInputLayout
import kotlinx.android.synthetic.main.fragment_edit_item.*
import java.util.*


class EditItemFragment : Fragment() {
    private var item: ItemInfo = ItemInfo()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
        arguments?.let {
            item.title  = it.getString("group19.lab2.TITLE").toString()
            item.location = it.getString("group19.lab2.LOCATION").toString()
            item.expiryDate = it.getString("group19.lab2.EXPIRY_DATE").toString()
            item.category= it.getString("group19.lab2.CATEGORY").toString()
            item.description = it.getString("group19.lab2.CATEGORY").toString()
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
        val categoryEditText = view.findViewById<EditText>(R.id.dateEditText)
        //categoryEditText.setText(item.category)

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
        (categoryEditText as? AutoCompleteTextView)?.setAdapter(adapter)

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

}
