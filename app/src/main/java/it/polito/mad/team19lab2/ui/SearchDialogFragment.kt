package it.polito.mad.team19lab2.ui

import android.app.Activity
import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import android.os.Bundle
import android.text.Editable
import android.text.InputFilter
import android.text.TextWatcher
import android.view.View
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager
import android.widget.AutoCompleteTextView
import android.widget.EditText
import androidx.appcompat.app.AppCompatDialogFragment
import androidx.fragment.app.DialogFragment
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import it.polito.mad.team19lab2.R
import it.polito.mad.team19lab2.utilities.DropdownAdapter
import it.polito.mad.team19lab2.utilities.PriceInputFilter

class SearchDialogFragment: AppCompatDialogFragment(){

    // Use this instance of the interface to deliver action events
    internal lateinit var listener: NoticeDialogListener

    //widgets
    lateinit var itemsCategory:MutableList<String>
    lateinit var categoryEditText:AutoCompleteTextView
    lateinit var  priceEditText :TextInputEditText
    lateinit var categoryTextField:TextInputLayout

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
            priceEditText = view.findViewById(R.id.priceSearchText)
            categoryTextField = view.findViewById(R.id.categorySearchField)
            val adapter = DropdownAdapter(
                requireContext(),
                R.layout.list_item,
                itemsCategory
            )
            categoryEditText?.setAdapter(adapter)
            //PRICE MANAGEMENT
            val inputFilter =  arrayOf<InputFilter>(
                PriceInputFilter(
                    10,
                    2
                )
            )
            priceEditText.filters = inputFilter




        builder.setView(view)
                // Add action buttons
                .setPositiveButton("Ok",
                    DialogInterface.OnClickListener { dialog, id ->
                        val title=view.findViewById<EditText>(R.id.titleSearchText).text?.toString()
                        val category=view.findViewById<AutoCompleteTextView>(R.id.categorySearchDropdown).text?.toString()
                        val price=view.findViewById<EditText>(R.id.priceSearchText).text?.toString()
                        val location=view.findViewById<EditText>(R.id.locationSearchText).text?.toString()
                        listener.onDialogPositiveClick(title,category,price,location)
                    })
                .setNegativeButton("cancel",
                    DialogInterface.OnClickListener { dialog, id ->
                        listener.onDialogNegativeClick(this)
                    })
            view.findViewById<EditText>(R.id.titleSearchText).setText(R.string.app_name)
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
            price: String?,
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
