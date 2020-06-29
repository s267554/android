package it.polito.mad.team19lab2.ui

import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import androidx.appcompat.app.AppCompatDialogFragment
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.viewModels
import it.polito.mad.team19lab2.R
import it.polito.mad.team19lab2.viewModel.UserViewModel
import kotlinx.android.synthetic.main.rate_dialog_layout.*

class RateAndCommentDialog(val userId:String="",val itemId:String=""): AppCompatDialogFragment(){

    // Use this instance of the interface to deliver action events
    private lateinit var listener: NoticeDialogListener
    private val uservm:UserViewModel by viewModels()
    private var firstime=true
    private lateinit var builder:AlertDialog.Builder
    private var dialogview:View?=null
    private var user_nick=""
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return activity?.let {
            builder = AlertDialog.Builder(it)
            // Get the layout inflater
            val inflater = requireActivity().layoutInflater
            // Inflate and set the layout for the dialog
            // Pass null as the parent view because its going in the dialog layout
            dialogview =inflater.inflate(R.layout.rate_dialog_layout, null)
            activity?.window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN)
            builder.setView(dialogview)
                // Add action buttons
                .setPositiveButton("Ok",
                    DialogInterface.OnClickListener { dialog, id ->
                        listener.onDialogPositiveClick(userId,itemId,commentEditText.text.toString(),ratingBar.rating,user_nick)
                    })
                .setNeutralButton(R.string.cancel,
                    DialogInterface.OnClickListener { dialog, id ->
                        listener.onDialogNegativeClick(this)
                    })
                .setIcon(R.drawable.ic_rate)
                .setTitle(resources.getString(R.string.rate_title))

            builder.create()
        } ?: throw IllegalStateException("Activity cannot be null")
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        Log.d("ccccc",dialog?.toString()+"fff")
        val s=uservm.getUser(userId).observe(viewLifecycleOwner, androidx.lifecycle.Observer {
            dialog?.setTitle(resources.getString(R.string.rate_title)+ " " +it.nickname)
            Log.d("ccccc",dialog?.toString()+"fff")
            user_nick=it.nickname
        } )

        return dialogview
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

    }

    /* The activity that creates an instance of this dialog fragment must
     * implement this interface in order to receive event callbacks.
     * Each method passes the DialogFragment in case the host needs to query it. */
    interface NoticeDialogListener {
        fun onDialogPositiveClick(
            userId:String,
            itemId:String,
            comment: String?,
            rate:Float,
            uuser_nick:String
        )
        fun onDialogNegativeClick(dialog: DialogFragment)
    }
    override fun onDestroyView() {
        dialogview = null
        super.onDestroyView()
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
