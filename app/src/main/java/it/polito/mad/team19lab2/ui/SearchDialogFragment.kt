package it.polito.mad.team19lab2.ui

import android.app.Activity
import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import android.os.Bundle
import android.text.InputFilter
import android.view.View
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager
import android.widget.AutoCompleteTextView
import android.widget.EditText
import android.widget.NumberPicker
import androidx.appcompat.app.AppCompatDialogFragment
import androidx.fragment.app.DialogFragment
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import it.polito.mad.team19lab2.R
import it.polito.mad.team19lab2.utilities.DropdownAdapter
import it.polito.mad.team19lab2.utilities.PriceInputFilter
import kotlinx.android.synthetic.main.fragment_edit_item.*

class SearchDialogFragment(var title: String?=null,var category: String?=null,var max:String?=null,var min:String?=null,var location: String?=null): AppCompatDialogFragment(){

    // Use this instance of the interface to deliver action events
    internal lateinit var listener: NoticeDialogListener

    //widgets
    lateinit var itemsCategory:MutableList<String>
    lateinit var categoryEditText:AutoCompleteTextView
    lateinit var  minprice :TextInputEditText
    lateinit var  maxprice :TextInputEditText
    lateinit var categoryTextField:TextInputLayout
    lateinit var locationEditText:TextInputEditText
    lateinit var titleTextview:TextInputEditText

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return activity?.let {
            val builder = AlertDialog.Builder(it)
            // Get the layout inflater
            val inflater = requireActivity().layoutInflater;

            // Inflate and set the layout for the dialog
            // Pass null as the parent view because its going in the dialog layout
            val view: View =inflater.inflate(R.layout.search_dialog_layout, null)
            activity?.window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN)
            itemsCategory = resources.getStringArray(R.array.categories).toMutableList()
            categoryEditText = view.findViewById(R.id.categorySearchDropdown)
            minprice = view.findViewById(R.id.minpriceEditText)
            maxprice = view.findViewById(R.id.maxpriceEditText)
            categoryTextField = view.findViewById(R.id.categorySearchField)
            locationEditText=view.findViewById(R.id.locationSearchText)
            titleTextview=view.findViewById(R.id.titleSearchText)
            if(!title.isNullOrEmpty())
                titleTextview.setText(title)
            if(!category.isNullOrEmpty())
                categoryEditText.setText(category)
            if(!min.isNullOrEmpty())
                minprice.setText(min)
            if(!max.isNullOrEmpty())
                maxprice.setText(max)
            if(!location.isNullOrEmpty())
                locationEditText.setText(location)

            val inputFilter =  arrayOf<InputFilter>(
                PriceInputFilter(
                    10,
                    2
                )
            )
            minprice.filters = inputFilter
            maxprice.filters = inputFilter




            val adapter = DropdownAdapter(
                requireContext(),
                R.layout.list_item,
                itemsCategory
            )
            categoryEditText?.setAdapter(adapter)
            builder.setView(view)
                // Add action buttons
                .setPositiveButton("Ok",
                    DialogInterface.OnClickListener { dialog, id ->
                        val title=titleTextview.text.toString()
                        val category=categoryEditText.text?.toString()
                        val minprice=this.minprice.text.toString()
                        val maxprice=this.maxprice.text.toString()
                        val location=locationEditText.text?.toString()
                        listener.onDialogPositiveClick(title,category,minprice,maxprice,location)
                    })
                .setNegativeButton("cancel",
                    DialogInterface.OnClickListener { dialog, id ->
                        listener.onDialogNegativeClick(this)
                    })
            .setIcon(R.drawable.ic_search_black_24dp)
            .setTitle(R.string.search)
            builder.create()
        } ?: throw IllegalStateException("Activity cannot be null")
    }

    /* The activity that creates an instance of this dialog fragment must
     * implement this interface in order to receive event callbacks.
     * Each method passes the DialogFragment in case the host needs to query it. */
    interface NoticeDialogListener {
        fun onDialogPositiveClick(
            title: String?,
            category: String?,
            minprice: String?,
            maxprice: String?,
            location: String?
        )
        fun onDialogNegativeClick(dialog: DialogFragment)
    }

    // Override the Fragment.onAttach() method to instantiate the NoticeDialogListener
    override fun onAttach(context: Context) {
        super.onAttach(context)
        // Verify that the host activity implements the callback interface
        try {
            // Instantiate the NoticeDialogListener so we can send events to the host
            listener = parentFragment as NoticeDialogListener
        } catch (e: ClassCastException) {
            // The activity doesn't implement the interface, throw exception
            throw ClassCastException((parentFragment.toString() +
                    " must implement NoticeDialogListener"))
        }
    }



    private fun hideKeyboardFrom(context: Context, view: View) {
        val imm: InputMethodManager = context.getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(view.windowToken, 0)
    }
}
