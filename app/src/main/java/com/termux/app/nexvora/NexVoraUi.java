package com.termux.app.nexvora;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.view.Gravity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import com.termux.R;

/** Small programmatic UI helpers so all NexVora screens share the same dark-luxury look. */
final class NexVoraUi {

    private NexVoraUi() {}

    static int dp(@NonNull Context c, int v) {
        return Math.round(v * c.getResources().getDisplayMetrics().density);
    }

    static int color(@NonNull Context c, int resId) {
        return ContextCompat.getColor(c, resId);
    }

    static TextView text(@NonNull Context c, @NonNull CharSequence s, float sp, int colorRes, boolean bold) {
        TextView t = new TextView(c);
        t.setText(s);
        t.setTextSize(sp);
        t.setTextColor(color(c, colorRes));
        if (bold) t.setTypeface(Typeface.DEFAULT_BOLD);
        return t;
    }

    static LinearLayout.LayoutParams matchWrap(@NonNull Context c, int topMarginDp) {
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        lp.topMargin = dp(c, topMarginDp);
        return lp;
    }

    /** Rounded card with a subtle stroke. Optional click handler. */
    static LinearLayout card(@NonNull Context c, @Nullable View.OnClickListener onClick) {
        LinearLayout l = new LinearLayout(c);
        l.setOrientation(LinearLayout.VERTICAL);
        int p = dp(c, 16);
        l.setPadding(p, p, p, p);
        GradientDrawable bg = new GradientDrawable();
        bg.setColor(color(c, R.color.nexvora_surface));
        bg.setCornerRadius(dp(c, 16));
        bg.setStroke(dp(c, 1), color(c, R.color.nexvora_stroke));
        l.setBackground(bg);
        if (onClick != null) {
            l.setClickable(true);
            l.setFocusable(true);
            l.setOnClickListener(onClick);
        }
        return l;
    }

    /** Card with a title and a secondary-coloured subtitle. */
    static LinearLayout menuCard(@NonNull Context c, @NonNull String title, @NonNull String subtitle,
                                 @Nullable View.OnClickListener onClick) {
        LinearLayout l = card(c, onClick);
        l.addView(text(c, title, 17, R.color.nexvora_text_primary, true));
        TextView sub = text(c, subtitle, 13, R.color.nexvora_text_secondary, false);
        sub.setPadding(0, dp(c, 4), 0, 0);
        l.addView(sub);
        return l;
    }

    /** Cyan -> purple gradient button. */
    static TextView primaryButton(@NonNull Context c, @NonNull String label, @NonNull View.OnClickListener onClick) {
        TextView b = text(c, label, 16, R.color.nexvora_black, true);
        b.setGravity(Gravity.CENTER);
        int p = dp(c, 14);
        b.setPadding(p, p, p, p);
        GradientDrawable bg = new GradientDrawable(GradientDrawable.Orientation.LEFT_RIGHT,
            new int[]{color(c, R.color.nexvora_cyan), color(c, R.color.nexvora_purple)});
        bg.setCornerRadius(dp(c, 14));
        b.setBackground(bg);
        b.setClickable(true);
        b.setFocusable(true);
        b.setOnClickListener(onClick);
        return b;
    }

    /** Outlined secondary button. */
    static TextView outlineButton(@NonNull Context c, @NonNull String label, @NonNull View.OnClickListener onClick) {
        TextView b = text(c, label, 15, R.color.nexvora_cyan, true);
        b.setGravity(Gravity.CENTER);
        int p = dp(c, 12);
        b.setPadding(p, p, p, p);
        GradientDrawable bg = new GradientDrawable();
        bg.setColor(Color.TRANSPARENT);
        bg.setCornerRadius(dp(c, 14));
        bg.setStroke(dp(c, 1), color(c, R.color.nexvora_cyan));
        b.setBackground(bg);
        b.setClickable(true);
        b.setFocusable(true);
        b.setOnClickListener(onClick);
        return b;
    }

    static TextView sectionTitle(@NonNull Context c, @NonNull String s) {
        TextView t = text(c, s.toUpperCase(), 12, R.color.nexvora_purple, true);
        t.setLetterSpacing(0.12f);
        return t;
    }
}
