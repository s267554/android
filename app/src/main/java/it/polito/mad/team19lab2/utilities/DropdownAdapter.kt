package it.polito.mad.team19lab2.utilities

import android.content.Context
import android.widget.ArrayAdapter
import android.widget.Filter


class DropdownAdapter(context: Context, resource: Int, objects: List<*>) : ArrayAdapter<Any?>(
    context,
    resource,
    objects
) {

    override fun getFilter(): Filter {
        return object : Filter() {
            override fun performFiltering(constraint: CharSequence?): FilterResults? {
                return null
            }

            override fun publishResults(
                constraint: CharSequence?,
                results: FilterResults?
            ) {
            }
        }
    }
}