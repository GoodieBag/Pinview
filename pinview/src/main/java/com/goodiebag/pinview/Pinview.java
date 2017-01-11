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

/**
 * Created by pavan on 11/01/17.
 */

public class Pinview extends LinearLayout implements TextWatcher, View.OnFocusChangeListener, View.OnKeyListener {
    private final float DENSITY = getContext().getResources().getDisplayMetrics().density;
    //Potential attributes
    private int mPins = 4;
    private List<EditText> editTextList = new ArrayList<>();
    private int mPinWidth = 50;
    private int mPinHeight = 50;
    private int mSplitWidth = 20;
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
        initAttributes(context, attrs, defStyleAttr);
        params = new LayoutParams(mPinWidth, mPinHeight);
        for (int i = 0; i < mPins; i++) {
            editTextList.add(i, new EditText(getContext()));
        }
    }


    private void styleEditText() {
        if (editTextList.size() > 0) {
            EditText styleEditText;
            for (int i = 0; i < mPins; i++) {
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
            mPins = array.getInt(R.styleable.Pinview_pins, mPins);
            mPinHeight = (int) array.getDimension(R.styleable.Pinview_pinHeight, mPinHeight);
            mPinWidth = (int) array.getDimension(R.styleable.Pinview_pinWidth, mPinWidth);
            mSplitWidth = (int) array.getDimension(R.styleable.Pinview_splitWidth, mSplitWidth);
        }
    }


    private void generateOneEditText(EditText styleEditText, String tag) {
        params.setMargins(mSplitWidth / 2, mSplitWidth / 2, mSplitWidth / 2, mSplitWidth / 2);
        filters[0] = new InputFilter.LengthFilter(1);
        styleEditText.setFilters(filters);
        styleEditText.setLayoutParams(params);
        styleEditText.setGravity(Gravity.CENTER);
        styleEditText.setBackground(mPinBackground);
        styleEditText.setTag(tag);
        styleEditText.addTextChangedListener(this);
        styleEditText.setOnFocusChangeListener(this);
        styleEditText.setOnKeyListener(this);
        this.addView(styleEditText);
    }

    private String getResultantString(){
        StringBuilder sb = new StringBuilder();
        for(EditText et : editTextList){
            if(sb.length() <= 4)
                sb.append(et.getText().toString());
        }
        Log.d("answer" , sb.toString());
        return sb.toString();
    }

    @Override
    public void onFocusChange(View view, boolean isFocused) {
        if (isFocused)
            currentFocus = view;
    }

//    @Override
//    protected void drawableStateChanged() {
//        super.drawableStateChanged();
//        if (mPinBackground != null && mPinBackground.isStateful()) {
//            int[] state = getDrawableState();
//            for (EditText txt : editTextList) {
//                if (txt == currentFocus)
//                    currentFocus.getBackground().setState(state);
//                else
//                    txt.getBackground().setState(new int[]{});
//            }
//        }
//        invalidate();
//    }

    @Override
    public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

    }

    @Override
    public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
        getResultantString();
        if (charSequence.length() == 1 && currentFocus != null) {
            currentTag = Integer.parseInt(currentFocus.getTag().toString());
            if (currentTag < mPins - 1) {
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
                editTextList.get(currentTag - 1).requestFocus();
            } else {
                //currentTag has reached zero
            }

            return true;
        }
        return false;
    }
}
