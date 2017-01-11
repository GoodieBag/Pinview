package com.goodiebag.pinview;

import android.content.Context;
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

import com.goodiebag.pinview.Utils.Utils;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by pavan on 11/01/17.
 */

public class Pinview extends LinearLayout implements TextWatcher, View.OnFocusChangeListener, View.OnKeyListener{
    //Potential attributes
    private int mFields = 4;
    private List<EditText> editTextList = new ArrayList<>();
    private int mEditTextWidth = 50;
    private int mEditTextHeight = 50;
    private int mWidthBetweenFields = 20;


    View currentFocus = null;
    int currentTag;

    InputFilter filters[] = new InputFilter[1];
    LinearLayout.LayoutParams params = new LayoutParams(Utils.dpToPx(getContext(),mEditTextWidth),Utils.dpToPx(getContext(),mEditTextHeight));


    public Pinview(Context context) {
        this(context, null);
    }

    public Pinview(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public Pinview(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        setGravity(Gravity.CENTER);
        initEditText();
        styleEditText();

    }

    private void initEditText() {
        this.removeAllViews();
        for (int i = 0; i < mFields; i++) {
            editTextList.add(i, new EditText(getContext()));

        }
    }

    private void styleEditText() {
        if (editTextList.size() > 0) {
            EditText styleEditText;
            for (int i = 0; i < mFields; i++) {
                styleEditText = editTextList.get(i);
                generateOneEditText(styleEditText,""+i);
            }

        }
    }


    private void generateOneEditText(EditText styleEditText, String tag){
        params.setMargins(mWidthBetweenFields/2,mWidthBetweenFields/2,mWidthBetweenFields/2,mWidthBetweenFields/2);
        filters[0] = new InputFilter.LengthFilter(1);
        styleEditText.setFilters(filters);
        styleEditText.setLayoutParams(params);
        styleEditText.setGravity(Gravity.CENTER);
        styleEditText.setBackgroundResource(R.drawable.sample_background);
        styleEditText.setTag(tag);
        styleEditText.addTextChangedListener(this);
        styleEditText.setOnFocusChangeListener(this);
        styleEditText.setOnKeyListener(this);
        this.addView(styleEditText);
    }

    @Override
    public void onFocusChange(View view, boolean b) {
        if(b) {
            currentFocus = view;
        }

    }

    @Override
    public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

    }

    @Override
    public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
        if(charSequence.length() == 1 && currentFocus !=null ) {
              currentTag = Integer.parseInt(currentFocus.getTag().toString());
            if(currentTag < mFields-1)
                editTextList.get(currentTag+1).requestFocus();
            else{
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
            if(currentTag > 0){
                editTextList.get(currentTag).setText("");
                editTextList.get(currentTag - 1).requestFocus();
            }else{
                //currentTag has reached zero
            }

            return true;
        }
        return false;
    }
}
