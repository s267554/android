package it.polito.mad.team19lab2.ui

import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import android.os.Bundle
import android.text.InputFilter
import android.view.View
import android.view.WindowManager
import android.widget.AutoCompleteTextView
import androidx.appcompat.app.AppCompatDialogFragment
import androidx.fragment.app.DialogFragment
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import it.polito.mad.team19lab2.R
import it.polito.mad.team19lab2.utilities.DropdownAdapter
import it.polito.mad.team19lab2.utilities.PriceInputFilter

class SearchDialogFragment(var title: String?=null, var category: Int = -1,
                           private var min:String?=null, private var max:String?=null, var location: String?=null): AppCompatDialogFragment(){

    // Use this instance of the interface to deliver action events
    private lateinit var listener: NoticeDialogListener

    //widgets
    private lateinit var itemsCategory:MutableList<String>
    private lateinit var categoryEditText:AutoCompleteTextView
    private lateinit var  minPrice :TextInputEditText
    private lateinit var  maxPrice :TextInputEditText
    private lateinit var categoryTextField:TextInputLayout
    private lateinit var locationEditText:TextInputEditText
    private lateinit var titleTextView:TextInputEditText

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return activity?.let {
            val builder = AlertDialog.Builder(it)
            // Get the layout inflater
            val inflater = requireActivity().layoutInflater

            // Inflate and set the layout for the dialog
            // Pass null as the parent view because its going in the dialog layout
            val view: View =inflater.inflate(R.layout.search_dialog_layout, null)
            activity?.window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN)
            itemsCategory = resources.getStringArray(R.array.categories).toMutableList()
            categoryEditText = view.findViewById(R.id.categorySearchDropdown)
            minPrice = view.findViewById(R.id.minpriceEditText)
            maxPrice = view.findViewById(R.id.maxpriceEditText)
            categoryTextField = view.findViewById(R.id.categorySearchField)
            locationEditText=view.findViewById(R.id.locationSearchText)
            titleTextView=view.findViewById(R.id.titleSearchText)
            if(!title.isNullOrEmpty())
                titleTextView.setText(title)
            if(category != -1)
                categoryEditText.setText(resources.getStringArray(R.array.categories)[category])
            if(!min.isNullOrEmpty())
                minPrice.setText(min)
            if(!max.isNullOrEmpty())
                maxPrice.setText(max)
            if(!location.isNullOrEmpty())
                locationEditText.setText(location)

            val inputFilter =  arrayOf<InputFilter>(
                PriceInputFilter(
                    10,
                    2
                )
            )
            minPrice.filters = inputFilter
            maxPrice.filters = inputFilter




            val adapter = DropdownAdapter(
                requireContext(),
                R.layout.list_item,
                itemsCategory
            )
            categoryEditText.setAdapter(adapter)
            builder.setView(view)
                // Add action buttons
                .setPositiveButton("Ok",
                    DialogInterface.OnClickListener { dialog, id ->
                        val title=titleTextView.text.toString()
                        val category=categoryEditText.text?.toString()
                        val minprice=this.minPrice.text.toString()
                        val maxprice=this.maxPrice.text.toString()
                        val location=locationEditText.text?.toString()
                        listener.onDialogPositiveClick(title,category,minprice,maxprice,location)
                    })
                .setNeutralButton("cancel",
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
}
