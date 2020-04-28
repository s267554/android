package it.polito.mad.team19lab2

import android.text.InputFilter
import android.text.Spanned
import android.util.Log
import java.util.regex.Matcher
import java.util.regex.Pattern


class PriceInputFilter(digitsBeforeZero: Int, digitsAfterZero: Int) :
    InputFilter {
    private var mPattern: Pattern = Pattern.compile("[0-9]{0,$digitsBeforeZero}+((\\.[0-9]{0,$digitsAfterZero})?)||(\\.)?")
    override fun filter(
        source: CharSequence,
        start: Int,
        end: Int,
        dest: Spanned,
        dstart: Int,
        dend: Int
    ): CharSequence? {
        var finalString=dest.subSequence(0, dstart).toString()+source+dest.toString().subSequence(dstart, dest.toString().length).toString()
        Log.d("filter", finalString)
        val matcher: Matcher = mPattern.matcher(finalString)
        return if (!matcher.matches()) "" else null
    }
}