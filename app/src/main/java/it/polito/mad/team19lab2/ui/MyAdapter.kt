package it.polito.mad.team19lab2.ui

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.CheckBox
import android.widget.TextView
import it.polito.mad.team19lab2.MainActivity
import it.polito.mad.team19lab2.R

class MyAdapter(
    context: Context,
    resource: Int,
    objects: List<StateVO>
) :
    ArrayAdapter<StateVO?>(context, resource, objects) {
    private val mContext: Context = context
    private val listState: ArrayList<StateVO> = objects as ArrayList<StateVO>
    private var isFromView = false

    override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup?): View {
        return getCustomView(position, convertView)
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        return getCustomView(position, convertView)
    }

    private fun getCustomView(position: Int, convertView: View?): View {//creo i view holder
        var convertView: View? = convertView
        val holder: ViewHolder
        if (convertView == null) {
            val layoutInflator = LayoutInflater.from(mContext)
            convertView = layoutInflator.inflate(R.layout.spinner_item, null)//the view is a single itme of the spinner
            holder = ViewHolder()
            holder.mTextView = convertView.findViewById(R.id.text)//binding of the holder fields with the layout's views
            holder.mCheckBox = convertView.findViewById(R.id.checkbox)
            convertView.tag=holder //set the tag, so for the next i'm always generating the same holder with the same binding
        } else {
            holder = convertView.tag as ViewHolder//set tag for next
        }
        holder.mTextView!!.text = listState[position].title//ora setto il testo
        // To check weather checked event fire from getview() or user input
        isFromView = true
        holder.mCheckBox!!.isChecked = listState[position].isSelected//setto la checkboxe in base al campo selected dell'item (tipo stateVO)
        isFromView = false
        holder.mCheckBox!!.visibility = View.VISIBLE
        holder.mCheckBox!!.tag = position
        //setto il checkedchange listener, uso il tag che rappresenta la posizione per sapere quale bottone è statp checkato dall'utente
        //e aggiorno la mia lista listState
        holder.mCheckBox!!.setOnCheckedChangeListener { buttonView, isChecked ->
            buttonView.tag as Int
            if (!isFromView) {
                listState[position].isSelected = isChecked
            }
            (mContext as MainActivity).setInterestsDropdown(listState)
        }
        return convertView!!
    }

    private class ViewHolder {
        var mTextView: TextView? = null
        var mCheckBox: CheckBox? = null
    }


}