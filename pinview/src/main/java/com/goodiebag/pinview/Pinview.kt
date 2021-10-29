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
import android.graphics.Color
import android.graphics.PorterDuff
import android.graphics.Typeface
import android.text.Editable
import android.text.InputFilter
import android.text.TextWatcher
import android.util.AttributeSet
import android.view.Gravity
import android.view.KeyEvent
import android.view.View
import android.view.View.OnLayoutChangeListener
import android.view.inputmethod.InputMethodManager
import android.widget.LinearLayout
import android.widget.TextView
import androidx.annotation.ColorInt
import androidx.annotation.DrawableRes
import androidx.core.content.ContextCompat
import java.util.*
import kotlin.math.abs
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
    private val pinTextViewList: MutableList<TextView> = ArrayList()
    private var mPinWidth = 50
    private var mTextSize = 12
    private var mPinHeight = 50
    private var mSplitWidth = 20
    private var mCursorVisible = false
    private var mDelPressed = false
    private var mTypeFace: Typeface? = null

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

    private var lastAppliedPinHeight = 0 // For auto adjusting to square pin sizes

    enum class InputType {
        TEXT, NUMBER
    }

    /**
     * Interface for onDataEntered event.
     */
    interface PinViewEventListener {
        fun onDataEntered(pinview: Pinview, fromUser: Boolean)
    }

    var mClickListener: OnClickListener? = null
    var currentFocus: View? = null // Will be null if there are no pin-views
    var filters = arrayOfNulls<InputFilter>(1)
    lateinit var params: LayoutParams

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
            for (pin in pinTextViewList) {
                pin.maxWidth = mPinWidth
                if (pin.length() == 0) {
                    pin.requestFocus()
                    openKeyboardIfForced()
                    focused = true
                    break
                }
            }
            if (!focused && pinTextViewList.size > 0) { // Focus the last view
                pinTextViewList[pinTextViewList.size - 1].requestFocus()
            }
            mClickListener?.onClick(this@Pinview)
        }
        // Bring up the keyboard
        val firstEditText: View? = pinTextViewList.firstOrNull() // list is empty, if pinLength==0
        firstEditText?.postDelayed({ openKeyboard() }, 200)
        updateEnabledState()
        // View v, int left, int top, int right, int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom
        val listener = OnLayoutChangeListener { _, _, _, _, _, _, _, _, _ ->
            if (pinTextViewList.isNotEmpty()) {
                val lastPin = pinTextViewList.last()
                val containerEndCoordinate = this@Pinview.let { it.x + it.width }
                val lastPinEndCoordinate = lastPin.x + lastPin.width
                if (lastPin.width > mPinWidth) {
                    // Turn off auto width distribution, because otherwise they always scale to fit available space
                    useFixedWidthPins()
                } else if (autoAdjustWidth && lastPinEndCoordinate > containerEndCoordinate) {
                    if (autoAdjustWidth) {
                        // Pin is too wide, we need to reduce it
                        useWeightedWidthPins()
                    }
                } else if (autoAdjustToSquareFormat && abs(lastPin.width - lastAppliedPinHeight) > 0.1f) {
                    // allow some difference, in case something moves on layout and reduce risk of infinite layout loop, and because its floats and equal is bad
                    // Check if something changed they layout or sizing
                    lastAppliedPinHeight = lastPin.width
                    params.height = lastAppliedPinHeight
                    updateEditTexts()
                    requestLayout()
                }
            }
        }
        addOnLayoutChangeListener(listener)
    }

    private fun useWeightedWidthPins() {
        params.weight = 1f
        updateEditTexts() // apply the new params
        requestLayout()
    }

    private fun useFixedWidthPins() {
        params.weight = 0f
        updateEditTexts() // apply the new params
        requestLayout()
    }

    /**
     * Creates editTexts and adds it to the pinview based on the pinLength specified.
     */
    private fun createEditTexts() {
        removeAllViews()
        pinTextViewList.clear()
        var editText: TextView

        for (i in 0 until mPinLength) {
            editText = TextView(context)
            editText.textSize = mTextSize.toFloat()
            editText.isFocusableInTouchMode = true // EditText behaviour
            editText.setTextColor(Color.BLACK) // color like EditText instead of greish
            mTypeFace.let { editText.typeface = it }
            pinTextViewList.add(i, editText)
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
        // We expect mTextSize to be sp, but we allow specifying via xml in any dimension resource as standard. Hence the scaling here
        val scaledDensity = resources.displayMetrics.scaledDensity
        mTextSize = (array.getDimensionPixelSize(R.styleable.Pinview_textSize, (mTextSize * scaledDensity).toInt()) / scaledDensity).toInt()
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
    private fun generateOneEditText(styleEditText: TextView, tag: String) {
        params.setMargins(mSplitWidth / 2, mSplitWidth / 2, mSplitWidth / 2, mSplitWidth / 2)

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
            for (et in pinTextViewList) {
                sb.append(et.text.toString())
            }
            return sb.toString()
        }
        set(value) {
            val regex = Regex("[0-9]*") // Allow empty string to clear the fields
            fromSetValue = true

            if (inputType == InputType.NUMBER && !value.matches(regex) || pinTextViewList.isNullOrEmpty()) {
                return
            }

            var lastTagHavingValue = -1
            for (i in pinTextViewList.indices) {
                if (value.length > i) {
                    lastTagHavingValue = i
                    pinTextViewList[i].text = value[i].toString()
                } else {
                    pinTextViewList[i].text = ""
                }
            }
            if (mPinLength > 0) {
                currentFocus = pinTextViewList[mPinLength - 1]
                if (lastTagHavingValue == mPinLength - 1) {
                    currentFocus = pinTextViewList[mPinLength - 1]
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
     * Requests focus on current pin view and opens keyboard if forceKeyboard is enabled.
     * If open keyboard is disabled in XML, use openKeyboard()
     *
     * @return the current focused pin view. It can be used to open soft-keyboard manually.
     */
    fun requestPinEntryFocus(): View {
        val currentTag = max(0, indexOfCurrentFocus)
        val currentEditText = pinTextViewList[currentTag]
        currentEditText.requestFocus()
        openKeyboardIfForced()
        return currentEditText
    }

    private fun openKeyboardIfForced() {
        if (mForceKeyboard) {
            openKeyboard()
        }
    }

    /**
     * Request the keyboard to open on the currently focused view
     */
    @Suppress("MemberVisibilityCanBePrivate")
    fun openKeyboard() {
        val inputMethodManager = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.toggleSoftInput(InputMethodManager.SHOW_FORCED, InputMethodManager.HIDE_IMPLICIT_ONLY)
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
            for (editText in pinTextViewList) {
                if (editText.length() == 0) {
                    if (editText !== view) {
                        editText.requestFocus()
                    } else {
                        currentFocus = view
                    }
                    return
                }
            }
            if (pinTextViewList[pinTextViewList.size - 1] !== view) {
                pinTextViewList[pinTextViewList.size - 1].requestFocus()
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
            for (editText in pinTextViewList) {
                editText.removeTextChangedListener(this)
                editText.transformationMethod = PinTransformationMethod()
                editText.addTextChangedListener(this)
            }
        } else {
            for (editText in pinTextViewList) {
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
                    val nextEditText = pinTextViewList[currentTag + 1]
                    nextEditText.isEnabled = true
                    nextEditText.requestFocus()
                }, delay)
            }

            if (currentTag == mPinLength - 1 && inputType == InputType.NUMBER || currentTag == mPinLength - 1 && mPassword) {
                finalNumberPin = true
            }
        } else if (charSequence.isEmpty()) {
            if (indexOfCurrentFocus < 0) {
                return
            }
            val currentTag = indexOfCurrentFocus
            this.mDelPressed = true

            //For the last cell of the non password text fields. Clear the text without changing the focus.
            if (!this.pinTextViewList[currentTag].text.isNullOrEmpty()) {
                this.pinTextViewList[currentTag].text = ""
            }
        }

        this.pinTextViewList.forEach { item ->
            if (item.text.isNotEmpty()) {
                val index = this.pinTextViewList.indexOf(item) + 1
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

        for (index in pinTextViewList.indices) {
            val editText = pinTextViewList[index]
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
            val currentEditText = pinTextViewList[currentTag].text
            //Last tile of the number pad. Clear the edit text without changing the focus.
            if (inputType == InputType.NUMBER && currentTag == mPinLength - 1 && finalNumberPin ||
                    mPassword && currentTag == mPinLength - 1 && finalNumberPin) {
                if (!currentEditText.isNullOrEmpty()) {
                    this.pinTextViewList[currentTag].text = ""
                }
                finalNumberPin = false
            } else if (currentTag > 0) {
                mDelPressed = true
                if (currentEditText.isNullOrEmpty()) {
                    //Takes it back one tile
                    this.pinTextViewList[currentTag - 1].requestFocus()
                }
                this.pinTextViewList[currentTag].text = ""
            } else {
                //For the first cell

                if (!currentEditText.isNullOrEmpty()) {
                    pinTextViewList[currentTag].text = ""
                }
            }
            return true
        }
        return false
    }

    /**
     * Getters and Setters
     */
    private val indexOfCurrentFocus: Int
        get() = pinTextViewList.indexOf(currentFocus)

    var splitWidth: Int
        get() = mSplitWidth
        set(splitWidth) {
            mSplitWidth = splitWidth
            val margin = splitWidth / 2
            params.setMargins(margin, margin, margin, margin)
            this.pinTextViewList.forEach {
                it.layoutParams = params
            }
        }

    var pinHeight: Int
        get() = mPinHeight
        set(pinHeight) {
            mPinHeight = pinHeight
            params.height = pinHeight
            this.pinTextViewList.forEach {
                it.layoutParams = params
            }
        }

    var pinWidth: Int
        get() = mPinWidth
        set(pinWidth) {
            mPinWidth = pinWidth
            params.width = pinWidth
            this.pinTextViewList.forEach {
                it.layoutParams = params
            }
        }

    /**
     * Ensure the pins fit within the Pinview parent.
     */
    var autoAdjustWidth: Boolean = true
        set(value) {
            field = value
            if (!value) {
                useFixedWidthPins()
            }
            requestLayout()
        }

    /**
     * When reducing size of Pins due to lack of width, also reduce height to keep the pin square.
     */
    var autoAdjustToSquareFormat: Boolean = false
        set(value) {
            field = value
            requestLayout()
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
            this.pinTextViewList.forEach {
                it.hint = mHint
            }
        }

    fun setPinBackgroundRes(@DrawableRes res: Int) {
        pinBackground = res
        this.pinTextViewList.forEach {
            it.setBackgroundResource(res)
        }
    }

    override fun setOnClickListener(l: OnClickListener?) {
        mClickListener = l
    }

    fun getInputType(): InputType {
        return inputType
    }

    fun setInputType(inputType: InputType) {
        this.inputType = inputType
        val keyInputType = keyboardInputType
        pinTextViewList.forEach {
            it.inputType = keyInputType
        }
    }

    fun setPinViewEventListener(listener: PinViewEventListener?) {
        mListener = listener
    }

    fun setPinViewEventListener(listener: (Pinview, Boolean) -> Unit) {
        mListener = object: PinViewEventListener {
            override fun onDataEntered(pinview: Pinview, fromUser: Boolean) {
                listener(pinview, fromUser)
            }
        }
    }

    fun showCursor(status: Boolean) {
        mCursorVisible = status
        this.pinTextViewList.forEach { it.isCursorVisible = status }
    }

    fun setTextSize(textSize: Int) {
        mTextSize = textSize
        updateEditTexts()
    }

    fun setTypeface(typeFace: Typeface?) {
        mTypeFace = typeFace
        updateEditTexts()
    }

    private fun updateEditTexts() {
        for (pin in pinTextViewList) {
            pin.textSize = mTextSize.toFloat()
            pin.layoutParams = params
            pin.requestLayout()
            mTypeFace?.let { pin.typeface = it }
        }
    }

    /**
     * Permit custom changes directly applied to the TextView pin-views.
     *
     * If the applied font has a slight offset, it can be adjusted by applying a padding like
     * <pre>
     * val fontScaledDensity = app.resources.displayMetrics.scaledDensity
     * textView.setPadding(0, (20 * fontScaledDensity).toInt(), 0, 0)
     * </pre>
     * @return
     */
    fun getTextViews(): List<TextView> {
        return Collections.unmodifiableList(pinTextViewList)
    }

    fun setCursorColor(@ColorInt color: Int) {
        this.pinTextViewList.forEach {
            setCursorColor(it, color)
        }
    }

    fun setTextColor(@ColorInt color: Int) {
        this.pinTextViewList.forEach {
            it.setTextColor(color)
        }
    }

    fun setTextPadding(left: Int, top: Int, right: Int, bottom: Int) {
        pinTextViewList.forEach {
            it.setPadding(left, top, right, bottom)
        }
    }

    fun setCursorShape(@DrawableRes shape: Int) {
        pinTextViewList.forEach {
            try {
                val field = TextView::class.java.getDeclaredField("mCursorDrawableRes")
                field.isAccessible = true
                field[it] = shape
            } catch (ignored: Exception) {
            }
        }
    }

    private fun setCursorColor(view: TextView, @ColorInt color: Int) {
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