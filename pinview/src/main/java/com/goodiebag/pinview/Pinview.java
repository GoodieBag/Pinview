package com.goodiebag.pinview;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.support.annotation.DrawableRes;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.MotionEvent;
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
    private boolean mCursorVisible = false;
    private boolean mDelPressed = false;
    private
    @DrawableRes
    int mPinBackground = R.drawable.sample_background;

    OnClickListener mClickListener;

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
        super.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                boolean focused = false;
                for (EditText editText : editTextList) {
                    if (editText.length() == 0) {
                        editText.requestFocus();
                        focused = true;
                        break;
                    }
                }
                if (!focused && editTextList.size() > 0) {
                    editTextList.get(editTextList.size() - 1).requestFocus();
                }
                if (mClickListener != null) {
                    mClickListener.onClick(Pinview.this);
                }
            }
        });
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
            //array.
            mPinBackground = array.getResourceId(R.styleable.Pinview_pinBackground, mPinBackground);
            mPinLength = array.getInt(R.styleable.Pinview_pinLength, mPinLength);
            mPinHeight = (int) array.getDimension(R.styleable.Pinview_pinHeight, mPinHeight);
            mPinWidth = (int) array.getDimension(R.styleable.Pinview_pinWidth, mPinWidth);
            mSplitWidth = (int) array.getDimension(R.styleable.Pinview_splitWidth, mSplitWidth);
            mInputType = array.getInt(R.styleable.Pinview_android_inputType, mInputType);
            mCursorVisible = array.getBoolean(R.styleable.Pinview_cursorVisible, mCursorVisible);
            array.recycle();
        }
    }


    private void generateOneEditText(EditText styleEditText, String tag) {
        params.setMargins(mSplitWidth / 2, mSplitWidth / 2, mSplitWidth / 2, mSplitWidth / 2);
        filters[0] = new InputFilter.LengthFilter(1);
        styleEditText.setFilters(filters);
        styleEditText.setLayoutParams(params);
        styleEditText.setGravity(Gravity.CENTER);
        styleEditText.setCursorVisible(mCursorVisible);
        //StateListDrawable Cannot be shared so clone it before its assigned to any other view.
        //Drawable clone = mPinBackground.mutate(); //.getConstantState().newDrawable();
        if (!mCursorVisible) {
            styleEditText.setClickable(false);
            //styleEditText.setFocusableInTouchMode(false);
            styleEditText.setHint("0");
            styleEditText.setOnTouchListener(new OnTouchListener() {
                @Override
                public boolean onTouch(View view, MotionEvent motionEvent) {
                    return false;
                }
            });
        }

        styleEditText.setBackgroundResource(mPinBackground);
        styleEditText.setPadding(0, 0, 0, 0);
        styleEditText.setTag(tag);
        styleEditText.setInputType(mInputType);
        styleEditText.addTextChangedListener(this);
        styleEditText.setOnFocusChangeListener(this);
        styleEditText.setOnKeyListener(this);
        this.addView(styleEditText);
    }

    private String getResultantString() {
        StringBuilder sb = new StringBuilder();
        for (EditText et : editTextList) {
            if (sb.length() <= mPinLength)
                sb.append(et.getText().toString());
        }
        Log.d("answer", sb.toString());
        return sb.toString();
    }

    @Override
    public void onFocusChange(View view, boolean isFocused) {
        if (isFocused) {
            if (!mCursorVisible) {
                if(mDelPressed){
                    currentFocus=view;
                    mDelPressed=false;
                    return;
                }
                for (final EditText editText : editTextList) {
                    if (editText.length() == 0) {
                        if (editText != view) {
                            editText.requestFocus();
                        } else {
                            currentFocus = view;
                        }
                        return;
                    }
                }
                if (editTextList.get(editTextList.size() - 1) != view) {
                    editTextList.get(editTextList.size() - 1).requestFocus();
                } else {
                    currentFocus = view;
                }
                return;
            }
            else{
                currentFocus=view;
            }
        } else {
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
                //editTextList.get(currentTag).clearFocus();
                editTextList.get(currentTag + 1).requestFocus();
                Log.d("TChange", "" + currentTag);
            } else {
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
        if ((keyEvent.getAction() == KeyEvent.ACTION_UP) && (i == KeyEvent.KEYCODE_DEL)) {
            // Perform action on Del press
            currentTag = Integer.parseInt(currentFocus.getTag().toString());
            if(editTextList.get(currentTag).length()>0) {
                editTextList.get(currentTag).setText("");
                return true;
            }

            if (currentTag > 0) {
                mDelPressed = true;
                editTextList.get(currentTag - 1).requestFocus();
            }
            return true;
        }
        return false;
    }

    private void refresh() {
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

    public
    @DrawableRes
    int getPinBackground() {
        return mPinBackground;
    }

    public void setPinBackgroundRes(@DrawableRes int res) {
        this.mPinBackground = res;
        refresh();
    }

    @Override
    public void setOnClickListener(OnClickListener l) {
        mClickListener = l;
    }
}
