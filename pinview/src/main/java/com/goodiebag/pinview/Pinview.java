package com.goodiebag.pinview;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;

import java.util.ArrayList;
import java.util.List;

import static android.text.InputType.TYPE_TEXT_VARIATION_NORMAL;

/**
 * Created by pavan on 11/01/17.
 */

public class Pinview extends LinearLayout implements TextWatcher, View.OnFocusChangeListener, View.OnKeyListener {
    private final float DENSITY = getContext().getResources().getDisplayMetrics().density;
    //Potential attributes
    private int mPinLength = 4;
    private List<EditText> editTextList = new ArrayList<>();
    private int mPinWidth = 50;
    private int mPinHeight = 50;
    private int mSplitWidth = 20;
    private int mInputType = TYPE_TEXT_VARIATION_NORMAL;
    private Drawable mPinBackground;

    View currentFocus = null;
    int currentTag;

    InputFilter filters[] = new InputFilter[1];
    LinearLayout.LayoutParams params;


    public Pinview(Context context) {
        this(context, null);
    }

    public Pinview(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public Pinview(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        setGravity(Gravity.CENTER);
        //init fields and attributes
        init(context, attrs, defStyleAttr);
        //style and draw it
        styleEditText();
    }

    private void init(Context context, AttributeSet attrs, int defStyleAttr) {
        this.removeAllViews();
        mPinHeight *= DENSITY;
        mPinWidth *= DENSITY;
        mSplitWidth *= DENSITY;
        setWillNotDraw(false);
        initAttributes(context, attrs, defStyleAttr);
        params = new LayoutParams(mPinWidth, mPinHeight);
        setOrientation(HORIZONTAL);
        for (int i = 0; i < mPinLength; i++) {
            editTextList.add(i, new EditText(getContext()));
        }
    }


    private void styleEditText() {
        if (editTextList.size() > 0) {
            EditText styleEditText;
            for (int i = 0; i < mPinLength; i++) {
                styleEditText = editTextList.get(i);
                generateOneEditText(styleEditText, "" + i);
            }

        }
    }

    private void initAttributes(Context context, AttributeSet attrs, int defStyleAttr) {
        if (attrs != null) {
            final TypedArray array = context.obtainStyledAttributes(attrs, R.styleable.Pinview, defStyleAttr, 0);
            mPinBackground = array.getDrawable(R.styleable.Pinview_pinBackground);
            if (mPinBackground == null) {
                mPinBackground = getResources().getDrawable(R.drawable.sample_background);
            }
            mPinLength = array.getInt(R.styleable.Pinview_pinLength, mPinLength);
            mPinHeight = (int) array.getDimension(R.styleable.Pinview_pinHeight, mPinHeight);
            mPinWidth = (int) array.getDimension(R.styleable.Pinview_pinWidth, mPinWidth);
            mSplitWidth = (int) array.getDimension(R.styleable.Pinview_splitWidth, mSplitWidth);
            mInputType = array.getInt(R.styleable.Pinview_android_inputType, mInputType);
        }
    }


    private void generateOneEditText(EditText styleEditText, String tag) {
        params.setMargins(mSplitWidth / 2, mSplitWidth / 2, mSplitWidth / 2, mSplitWidth / 2);
        filters[0] = new InputFilter.LengthFilter(1);
        styleEditText.setFilters(filters);
        styleEditText.setLayoutParams(params);
        styleEditText.setGravity(Gravity.CENTER);
        //StateListDrawable Cannot be shared so clone it before its assigned to any other view.
        Drawable clone = mPinBackground.getConstantState().newDrawable();
        styleEditText.setBackground(clone);
        styleEditText.setTag(tag);
        styleEditText.setInputType(mInputType);
        styleEditText.addTextChangedListener(this);
        styleEditText.setOnFocusChangeListener(this);
        styleEditText.setOnKeyListener(this);
        this.addView(styleEditText);
    }

    private String getResultantString(){
        StringBuilder sb = new StringBuilder();
        for(EditText et : editTextList){
            if(sb.length() <= mPinLength)
                sb.append(et.getText().toString());
        }
        Log.d("answer" , sb.toString());
        return sb.toString();
    }

    @Override
    public void onFocusChange(View view, boolean isFocused) {
        if (isFocused)
            currentFocus = view;
        else{
            view.clearFocus();
        }
        Log.d("focus", "" + view.getTag() + isFocused);
    }

    @Override
    public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

    }

    @Override
    public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
        getResultantString();
        if (charSequence.length() == 1 && currentFocus != null) {
            currentTag = Integer.parseInt(currentFocus.getTag().toString());
            if (currentTag < mPinLength - 1) {
                editTextList.get(currentTag).clearFocus();
                editTextList.get(currentTag + 1).requestFocus();
            }
            else {
                //Last Pin box has been reached.
            }
        }
        Log.d("Focus", "Tag" + currentTag);
    }

    @Override
    public void afterTextChanged(Editable editable) {

    }

    @Override
    public boolean onKey(View view, int i, KeyEvent keyEvent) {
        if ((keyEvent.getAction() == KeyEvent.ACTION_DOWN) &&
                (i == KeyEvent.KEYCODE_DEL)) {
            // Perform action on Del press
            currentTag = Integer.parseInt(currentFocus.getTag().toString());
            if (currentTag > 0) {
                editTextList.get(currentTag).setText("");
                editTextList.get(currentTag).clearFocus();
                editTextList.get(currentTag - 1).requestFocus();
            } else {
                //currentTag has reached zero
                editTextList.get(currentTag).setText("");
            }

            return true;
        }
        return false;
    }

    private void refresh(){
        removeAllViews();
        styleEditText();
        invalidate();
    }

    public int getSplitWidth() {
        return mSplitWidth;
    }

    public void setSplitWidth(int splitWidth) {
        this.mSplitWidth = splitWidth;
        refresh();
    }

    public int getPinHeight() {
        return mPinHeight;
    }

    public void setPinHeight(int pinHeight) {
        this.mPinHeight = pinHeight;
        refresh();
    }

    public int getPinWidth() {
        return mPinWidth;
    }

    public void setPinWidth(int pinWidth) {
        this.mPinWidth = pinWidth;
        refresh();
    }

    public int getPinLength() {
        return mPinLength;
    }

    public void setPinLength(int pinLength) {
        this.mPinLength = pinLength;
        refresh();
    }

    public int getInputType() {
        return mInputType;
    }

    public void setInputType(int inputType) {
        this.mInputType = inputType;
        refresh();
    }

    public Drawable getPinBackground() {
        return mPinBackground;
    }

    public void setPinBackground(Drawable pinBackground) {
        this.mPinBackground = pinBackground;
        refresh();
    }
}
