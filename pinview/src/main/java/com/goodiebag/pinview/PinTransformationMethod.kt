package com.goodiebag.pinview

import android.graphics.Rect
import android.text.method.TransformationMethod
import android.view.View

/**
 * Created by Farshid Roohi.
 * Pinview | Copyrights 3/24/20.
 */
class PinTransformationMethod : TransformationMethod {

    private val BULLET:Char = '\u2022'

    override fun getTransformation(source: CharSequence, view: View): CharSequence {
        return PasswordCharSequence(source)
    }

    override fun onFocusChanged(view: View, sourceText: CharSequence, focused: Boolean, direction: Int, previouslyFocusedRect: Rect?) {}
    private inner class PasswordCharSequence(private val source: CharSequence) : CharSequence {
        override val length: Int
            get() = source.length


        override fun get(index: Int): Char {
            return BULLET
        }


        override fun subSequence(startIndex: Int, endIndex: Int): CharSequence {
            return PasswordCharSequence(source.subSequence(startIndex, endIndex))
        }

    }
}