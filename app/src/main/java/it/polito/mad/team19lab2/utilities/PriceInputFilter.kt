package it.polito.mad.team19lab2.utilities

import android.text.InputFilter
import android.text.Spanned
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
        val finalString=dest.subSequence(0, dstart).toString()+source+dest.toString().subSequence(dstart, dest.toString().length).toString()
        val matcher: Matcher = mPattern.matcher(finalString)
        return if (!matcher.matches()) "" else null
    }
}