/*
MIT License
Copyright (c) 2017 GoodieBag
Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:
The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.
THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
 */
package com.goodiebag.pinview

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.PorterDuff
import android.graphics.Rect
import android.text.Editable
import android.text.InputFilter
import android.text.TextWatcher
import android.text.method.TransformationMethod
import android.util.AttributeSet
import android.util.Log
import android.view.Gravity
import android.view.KeyEvent
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import androidx.annotation.ColorInt
import androidx.annotation.DrawableRes
import androidx.core.content.ContextCompat
import java.util.*
import kotlin.math.max

/**
 * This class implements a pinview for android.
 * It can be used as a widget in android to take passwords/OTP/pins etc.
 * It is extended from a LinearLayout, implements TextWatcher, FocusChangeListener and OnKeyListener.
 * Supports drawableItem/selectors as a background for each pin box.
 * A listener is wired up to monitor complete data entry.
 * Can toggle cursor visibility.
 * Supports numpad and text keypad.
 * Flawless focus change to the consecutive pinbox.
 * Date : 11/01/17
 *
 * @author Krishanu
 * @author Pavan
 * @author Koushik
 */
class Pinview @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0) : LinearLayout(context, attrs, defStyleAttr), TextWatcher, View.OnFocusChangeListener, View.OnKeyListener {
    private val DENSITY = getContext().resources.displayMetrics.density

    /**
     * Attributes
     */
    private var mPinLength = 4
    private val editTextList: MutableList<EditText>? = ArrayList()
    private var mPinWidth = 50
    private var mTextSize = 12
    private var mPinHeight = 50
    private var mSplitWidth = 20
    private var mCursorVisible = false
    private var mDelPressed = false

    @get:DrawableRes
    @DrawableRes
    var pinBackground = R.drawable.sample_background
        private set
    private var mPassword = false
    private var mHint: String? = ""
    private var inputType = InputType.TEXT
    private var finalNumberPin = false
    private var mListener: PinViewEventListener? = null
    private var fromSetValue = false
    private var mForceKeyboard = true

    enum class InputType {
        TEXT, NUMBER
    }

    /**
     * Interface for onDataEntered event.
     */
    interface PinViewEventListener {
        fun onDataEntered(pinview: Pinview?, fromUser: Boolean)
    }

    var mClickListener: OnClickListener? = null
    var currentFocus: View? = null
    var filters = arrayOfNulls<InputFilter>(1)
    var params: LayoutParams? = null

    /**
     * A method to take care of all the initialisations.
     *
     * @param context
     * @param attrs
     * @param defStyleAttr
     */
    private fun init(context: Context, attrs: AttributeSet?, defStyleAttr: Int) {
        removeAllViews()
        mPinHeight *= DENSITY.toInt()
        mPinWidth *= DENSITY.toInt()
        mSplitWidth *= DENSITY.toInt()
        setWillNotDraw(false)
        initAttributes(context, attrs, defStyleAttr)
        params = LayoutParams(mPinWidth, mPinHeight)
        orientation = HORIZONTAL
        createEditTexts()
        super.setOnClickListener {
            var focused = false
            for (editText in editTextList!!) {
                if (editText.length() == 0) {
                    editText.requestFocus()
                    openKeyboard()
                    focused = true
                    break
                }
            }
            if (!focused && editTextList.size > 0) { // Focus the last view
                editTextList[editTextList.size - 1].requestFocus()
            }
            if (mClickListener != null) {
                mClickListener!!.onClick(this@Pinview)
            }
        }
        // Bring up the keyboard
        val firstEditText: View? = editTextList?.first()
        firstEditText?.postDelayed({ openKeyboard() }, 200)
        updateEnabledState()
    }

    /**
     * Creates editTexts and adds it to the pinview based on the pinLength specified.
     */
    private fun createEditTexts() {
        removeAllViews()
        editTextList!!.clear()
        var editText: EditText

        for (i in 0 until mPinLength) {
            editText = EditText(context)
            editText.textSize = mTextSize.toFloat()
            editTextList.add(i, editText)
            this.addView(editText)
            generateOneEditText(editText, "" + i)
        }
        setTransformation()
    }

    /**
     * This method gets the attribute values from the XML, if not found it takes the default values.
     *
     * @param context
     * @param attrs
     * @param defStyleAttr
     */
    private fun initAttributes(context: Context, attrs: AttributeSet?, defStyleAttr: Int) {
        if (attrs == null) {
            return
        }

        val array = context.obtainStyledAttributes(attrs, R.styleable.Pinview, defStyleAttr, 0)
        pinBackground = array.getResourceId(R.styleable.Pinview_pinBackground, pinBackground)
        mPinLength = array.getInt(R.styleable.Pinview_pinLength, mPinLength)
        mPinHeight = array.getDimension(R.styleable.Pinview_pinHeight, mPinHeight.toFloat()).toInt()
        mPinWidth = array.getDimension(R.styleable.Pinview_pinWidth, mPinWidth.toFloat()).toInt()
        mSplitWidth = array.getDimension(R.styleable.Pinview_splitWidth, mSplitWidth.toFloat()).toInt()
        mTextSize = array.getDimension(R.styleable.Pinview_textSize, mTextSize.toFloat()).toInt()
        mCursorVisible = array.getBoolean(R.styleable.Pinview_cursorVisible, mCursorVisible)
        mPassword = array.getBoolean(R.styleable.Pinview_password, mPassword)
        mForceKeyboard = array.getBoolean(R.styleable.Pinview_forceKeyboard, mForceKeyboard)
        mHint = array.getString(R.styleable.Pinview_hint)
        val its = InputType.values()
        inputType = its[array.getInt(R.styleable.Pinview_inputType, 0)]
        array.recycle()
    }

    /**
     * Takes care of styling the editText passed in the param.
     * tag is the index of the editText.
     *
     * @param styleEditText
     * @param tag
     */
    @SuppressLint("ClickableViewAccessibility")
    private fun generateOneEditText(styleEditText: EditText, tag: String) {
        params!!.setMargins(mSplitWidth / 2, mSplitWidth / 2, mSplitWidth / 2, mSplitWidth / 2)

        filters[0] = InputFilter.LengthFilter(1)

        styleEditText.filters = filters
        styleEditText.layoutParams = params
        styleEditText.gravity = Gravity.CENTER
        styleEditText.isCursorVisible = mCursorVisible

        if (!mCursorVisible) {
            styleEditText.isClickable = false
            styleEditText.hint = mHint
            styleEditText.setOnTouchListener { _, _ -> // When back space is pressed it goes to delete mode and when u click on an edit Text it should get out of the delete mode
                mDelPressed = false
                false
            }
        }
        styleEditText.apply {
            setBackgroundResource(pinBackground)
            setPadding(0, 0, 0, 0)
            this.tag = tag
            inputType = keyboardInputType
            addTextChangedListener(this@Pinview)
            onFocusChangeListener = this@Pinview
            setOnKeyListener(this@Pinview)
        }
    }

    private val keyboardInputType: Int
        get() {
            return when (inputType) {
                InputType.NUMBER -> android.text.InputType.TYPE_CLASS_NUMBER or android.text.InputType.TYPE_NUMBER_VARIATION_PASSWORD
                InputType.TEXT -> android.text.InputType.TYPE_CLASS_TEXT
            }
        }

    /**
     * Returns the value of the Pinview
     *
     * @return
     */// Allow empty string to clear the fields
    /**
     * Sets the value of the Pinview
     *
     * @param value
     */
    var value: String
        get() {
            val sb = StringBuilder()
            for (et in editTextList!!) {
                sb.append(et.text.toString())
            }
            return sb.toString()
        }
        set(value) {
            val regex = Regex("[0-9]*") // Allow empty string to clear the fields
            fromSetValue = true

            if (inputType == InputType.NUMBER && !value.matches(regex) || editTextList.isNullOrEmpty()) {
                return
            }

            var lastTagHavingValue = -1
            for (i in editTextList.indices) {
                if (value.length > i) {
                    lastTagHavingValue = i
                    editTextList[i].setText(value[i].toString())
                } else {
                    editTextList[i].setText("")
                }
            }
            if (mPinLength > 0) {
                if (lastTagHavingValue < mPinLength - 1) {
                    currentFocus = editTextList[lastTagHavingValue + 1]
                } else {
                    currentFocus = editTextList[mPinLength - 1]
                    if (inputType == InputType.NUMBER || mPassword) {
                        this.finalNumberPin = true
                    }
                    this.mListener?.onDataEntered(this, false)
                }
                currentFocus?.requestFocus()
            }
            fromSetValue = false
            updateEnabledState()
        }

    /**
     * Requsets focus on current pin view and opens keyboard if forceKeyboard is enabled.
     *
     * @return the current focused pin view. It can be used to open softkeyboard manually.
     */
    fun requestPinEntryFocus(): View? {
        val currentTag = max(0, indexOfCurrentFocus)
        val currentEditText = editTextList?.get(currentTag)
        currentEditText?.requestFocus()
        openKeyboard()
        return currentEditText
    }

    private fun openKeyboard() {
        if (mForceKeyboard) {
            val inputMethodManager = context.getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager
            inputMethodManager?.toggleSoftInput(InputMethodManager.SHOW_FORCED, InputMethodManager.HIDE_IMPLICIT_ONLY)
        }
    }

    /**
     * Clears the values in the Pinview
     */
    fun clearValue() {
        value = ""
    }

    override fun onFocusChange(view: View, isFocused: Boolean) {
        if (isFocused && !mCursorVisible) {
            if (mDelPressed) {
                currentFocus = view
                mDelPressed = false
                return
            }
            for (editText in editTextList!!) {
                if (editText.length() == 0) {
                    if (editText !== view) {
                        editText.requestFocus()
                    } else {
                        currentFocus = view
                    }
                    return
                }
            }
            if (editTextList[editTextList.size - 1] !== view) {
                editTextList[editTextList.size - 1].requestFocus()
            } else {
                currentFocus = view
            }
        } else if (isFocused && mCursorVisible) {
            currentFocus = view
        } else {
            view.clearFocus()
        }
    }

    /**
     * Handles the character transformation for password inputs.
     */
    private fun setTransformation() {
        if (mPassword) {
            for (editText in editTextList!!) {
                editText.removeTextChangedListener(this)
                editText.transformationMethod = PinTransformationMethod()
                editText.addTextChangedListener(this)
            }
        } else {
            for (editText in editTextList!!) {
                editText.removeTextChangedListener(this)
                editText.transformationMethod = null
                editText.addTextChangedListener(this)
            }
        }
    }

    override fun beforeTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {}

    /**
     * Fired when text changes in the editTexts.
     * Backspace is also identified here.
     *
     * @param charSequence
     * @param start
     * @param i1
     * @param count
     */
    override fun onTextChanged(charSequence: CharSequence, start: Int, i1: Int, count: Int) {

        if (charSequence.length == 1 && currentFocus != null) {
            val currentTag = indexOfCurrentFocus
            if (currentTag < mPinLength - 1) {
                var delay: Long = 1
                if (mPassword) delay = 25
                postDelayed({
                    val nextEditText = editTextList?.get(currentTag + 1)
                    nextEditText?.isEnabled = true
                    nextEditText?.requestFocus()
                }, delay)
            }

            if (currentTag == mPinLength - 1 && inputType == InputType.NUMBER || currentTag == mPinLength - 1 && mPassword) {
                finalNumberPin = true
            }
        } else if (charSequence.isEmpty()) {

            val currentTag = indexOfCurrentFocus
            this.mDelPressed = true

            //For the last cell of the non password text fields. Clear the text without changing the focus.
            if (!this.editTextList?.get(currentTag)?.text.isNullOrEmpty()) {
                this.editTextList?.get(currentTag)?.setText("")
            }
        }

        this.editTextList?.forEach { item ->
            if (item.text.isNotEmpty()) {
                val index = this.editTextList.indexOf(item) + 1
                if (!this.fromSetValue && index == mPinLength) {
                    this.mListener?.onDataEntered(this, true)
                }
            }
        }

        updateEnabledState()
    }

    /**
     * Disable views ahead of current focus, so a selector can change the drawing of those views.
     */
    private fun updateEnabledState() {
        val currentTag = max(0, indexOfCurrentFocus)
        for (index in editTextList!!.indices) {
            val editText = editTextList[index]
            editText.isEnabled = index <= currentTag
        }
    }

    override fun afterTextChanged(editable: Editable) {}

    /**
     * Monitors keyEvent.
     *
     * @param view
     * @param i
     * @param keyEvent
     * @return
     */
    override fun onKey(view: View, i: Int, keyEvent: KeyEvent): Boolean {
        if (keyEvent.action == KeyEvent.ACTION_UP && i == KeyEvent.KEYCODE_DEL) {
            // Perform action on Del press
            val currentTag = indexOfCurrentFocus
            val currentEditText = editTextList?.get(currentTag)?.text
            //Last tile of the number pad. Clear the edit text without changing the focus.
            if (inputType == InputType.NUMBER && currentTag == mPinLength - 1 && finalNumberPin ||
                    mPassword && currentTag == mPinLength - 1 && finalNumberPin) {
                if (!currentEditText.isNullOrEmpty()) {
                    this.editTextList?.get(currentTag)?.setText("")
                }
                finalNumberPin = false
            } else if (currentTag > 0) {
                mDelPressed = true
                if (currentEditText.isNullOrEmpty()) {
                    //Takes it back one tile
                    this.editTextList?.get(currentTag - 1)?.requestFocus()
                }
                this.editTextList?.get(currentTag)?.setText("")
            } else {
                //For the first cell

                if (!currentEditText.isNullOrEmpty()) {
                    editTextList?.get(currentTag)?.setText("")
                }
            }
            return true
        }
        return false
    }

    /**
     * A class to implement the transformation mechanism
     */
    private inner class PinTransformationMethod : TransformationMethod {
        private val BULLET = '\u2022'
        override fun getTransformation(source: CharSequence, view: View): CharSequence {
            return PasswordCharSequence(source)
        }

        override fun onFocusChanged(view: View, sourceText: CharSequence, focused: Boolean, direction: Int, previouslyFocusedRect: Rect) {}
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

    /**
     * Getters and Setters
     */
    private val indexOfCurrentFocus: Int
        get() = editTextList!!.indexOf(currentFocus)

    var splitWidth: Int
        get() = mSplitWidth
        set(splitWidth) {
            mSplitWidth = splitWidth
            val margin = splitWidth / 2
            params?.setMargins(margin, margin, margin, margin)
            for (editText in editTextList!!) {
                editText.layoutParams = params
            }
        }

    var pinHeight: Int
        get() = mPinHeight
        set(pinHeight) {
            mPinHeight = pinHeight
            params?.height = pinHeight
            for (editText in editTextList!!) {
                editText.layoutParams = params
            }
        }

    var pinWidth: Int
        get() = mPinWidth
        set(pinWidth) {
            mPinWidth = pinWidth
            params?.width = pinWidth
            for (editText in editTextList!!) {
                editText.layoutParams = params
            }
        }

    var pinLength: Int
        get() = mPinLength
        set(pinLength) {
            mPinLength = pinLength
            createEditTexts()
        }

    var isPassword: Boolean
        get() = mPassword
        set(password) {
            mPassword = password
            setTransformation()
        }

    var hint: String?
        get() = mHint
        set(mHint) {
            this.mHint = mHint
            for (editText in editTextList!!) editText.hint = mHint
        }

    fun setPinBackgroundRes(@DrawableRes res: Int) {
        pinBackground = res
        for (editText in editTextList!!) editText.setBackgroundResource(res)
    }

    override fun setOnClickListener(l: OnClickListener?) {
        mClickListener = l
    }

    fun getInputType(): InputType {
        return inputType
    }

    fun setInputType(inputType: InputType) {
        this.inputType = inputType
        val it = keyboardInputType
        for (editText in editTextList!!) {
            editText.inputType = it
        }
    }

    fun setPinViewEventListener(listener: PinViewEventListener?) {
        mListener = listener
    }

    fun showCursor(status: Boolean) {
        mCursorVisible = status
        if (editTextList == null || editTextList.isEmpty()) {
            return
        }
        for (edt in editTextList) {
            edt.isCursorVisible = status
        }
    }

    fun setTextSize(textSize: Int) {
        mTextSize = textSize
        if (editTextList == null || editTextList.isEmpty()) {
            return
        }
        for (edt in editTextList) {
            edt.textSize = mTextSize.toFloat()
        }
    }

    fun setCursorColor(@ColorInt color: Int) {
        if (editTextList == null || editTextList.isEmpty()) {
            return
        }
        for (edt in editTextList) {
            setCursorColor(edt, color)
        }
    }

    fun setTextColor(@ColorInt color: Int) {
        if (editTextList == null || editTextList.isEmpty()) {
            return
        }
        for (edt in editTextList) {
            edt.setTextColor(color)
        }
    }

    fun setCursorShape(@DrawableRes shape: Int) {
        if (editTextList == null || editTextList.isEmpty()) {
            return
        }
        for (edt in editTextList) {
            try {
                val f = TextView::class.java.getDeclaredField("mCursorDrawableRes")
                f.isAccessible = true
                f[edt] = shape
            } catch (ignored: Exception) {
            }
        }
    }

    private fun setCursorColor(view: EditText, @ColorInt color: Int) {
        try {
            // Get the cursor resource id
            var field = TextView::class.java.getDeclaredField("mCursorDrawableRes")
            field.isAccessible = true
            val drawableResId = field.getInt(view)

            // Get the editor
            field = TextView::class.java.getDeclaredField("mEditor")
            field.isAccessible = true
            val editor = field[view]

            // Get the drawable and set a color filter
            val drawable = ContextCompat.getDrawable(view.context, drawableResId)
            drawable?.setColorFilter(color, PorterDuff.Mode.SRC_IN)
            val drawables = arrayOf(drawable, drawable)

            // Set the drawables
            field = editor.javaClass.getDeclaredField("mCursorDrawable")
            field.isAccessible = true
            field[editor] = drawables
        } catch (ignored: Exception) {
        }
    }

    init {
        gravity = Gravity.CENTER
        init(context, attrs, defStyleAttr)
    }
}