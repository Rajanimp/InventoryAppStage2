package com.example.inventoryappstage2.utils;

import android.graphics.Color;
import android.text.Spannable;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.text.style.URLSpan;

public class StringUtil {

    public static void stripUnderlines(Spannable textView) {
        URLSpan[] spans = textView.getSpans(0, textView.length(), URLSpan.class);

        for (URLSpan span : spans) {
            int start = textView.getSpanStart(span);
            int end = textView.getSpanEnd(span);
            textView.removeSpan(span);

            span = new URLSpanNoUnderline(span.getURL());
            textView.setSpan(span, start, end, 0);
        }
    }


}
