package com.ifthenelse.ejmoore2.agenda.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.widget.TextView;

import com.ifthenelse.ejmoore2.agenda.R;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by edward on 1/16/17.
 */

public class TextViewPlus extends TextView {

    private static Map<String, Typeface> fonts = new HashMap<>();

    public TextViewPlus(Context context) {
        this(context, null);
    }

    public TextViewPlus(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public TextViewPlus(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);

        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.TextViewPlus);
        String fontName = a.getString(R.styleable.TextViewPlus_font);
        setFont(fontName);

        a.recycle();
    }

    public boolean setFont(String fontName) {
        if (!fonts.containsKey(fontName)) {
            try {
                Typeface tf = Typeface.createFromAsset(getContext().getAssets(), fontName);
                fonts.put(fontName, tf);
            } catch (Exception e) {
                return false;
            }
        }
        setTypeface(fonts.get(fontName));
        return true;
    }
}
