package com.termux.app.nexvora;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.termux.R;
import com.termux.shared.termux.settings.preferences.TermuxAppSharedPreferences;

import java.io.IOException;

/** Color theme, font file and font size. Uses the standard Termux style files. */
public class NexVoraAppearanceActivity extends AppCompatActivity {

    private static final int REQ_FONT = 4001;
    private TextView mSizeLabel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        final Context c = this;

        ScrollView scroll = new ScrollView(c);
        scroll.setBackgroundColor(NexVoraUi.color(c, R.color.nexvora_black));
        LinearLayout root = new LinearLayout(c);
        root.setOrientation(LinearLayout.VERTICAL);
        int pad = NexVoraUi.dp(c, 20);
        root.setPadding(pad, NexVoraUi.dp(c, 40), pad, pad);

        root.addView(NexVoraUi.text(c, "Appearance", 28, R.color.nexvora_text_primary, true));

        // ---- Themes
        root.addView(NexVoraUi.sectionTitle(c, "Color theme"), NexVoraUi.matchWrap(c, 24));
        for (final NexVoraStyle.ColorTheme t : NexVoraStyle.themes()) {
            root.addView(NexVoraUi.menuCard(c, t.name, "Background " + t.bg + " \u00B7 Text " + t.fg, v -> {
                try {
                    NexVoraStyle.applyTheme(t);
                    toast("Applied \"" + t.name + "\". Return to the terminal to see it.");
                } catch (IOException e) {
                    toast("Could not apply theme: " + e.getMessage());
                }
            }), NexVoraUi.matchWrap(c, 8));
        }
        root.addView(NexVoraUi.outlineButton(c, "Reset to Termux default colors", v -> {
            NexVoraStyle.resetTheme();
            toast("Default colors restored.");
        }), NexVoraUi.matchWrap(c, 12));

        // ---- Font
        root.addView(NexVoraUi.sectionTitle(c, "Terminal font"), NexVoraUi.matchWrap(c, 28));
        root.addView(NexVoraUi.menuCard(c, "Choose font file",
            "Pick a monospace .ttf / .otf (for example a Nerd Font)", v -> {
                Intent i = new Intent(Intent.ACTION_OPEN_DOCUMENT);
                i.addCategory(Intent.CATEGORY_OPENABLE);
                i.setType("*/*");
                startActivityForResult(i, REQ_FONT);
            }), NexVoraUi.matchWrap(c, 8));
        root.addView(NexVoraUi.outlineButton(c, "Reset to default font", v -> {
            NexVoraStyle.resetFont();
            toast("Default font restored.");
        }), NexVoraUi.matchWrap(c, 8));

        // ---- Size
        root.addView(NexVoraUi.sectionTitle(c, "Text size"), NexVoraUi.matchWrap(c, 28));
        LinearLayout row = new LinearLayout(c);
        row.setOrientation(LinearLayout.HORIZONTAL);
        LinearLayout.LayoutParams half = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f);
        half.rightMargin = NexVoraUi.dp(c, 8);
        row.addView(NexVoraUi.outlineButton(c, "A\u2212", v -> changeSize(false)), half);
        LinearLayout.LayoutParams half2 = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f);
        row.addView(NexVoraUi.outlineButton(c, "A+", v -> changeSize(true)), half2);
        root.addView(row, NexVoraUi.matchWrap(c, 8));
        mSizeLabel = NexVoraUi.text(c, "", 13, R.color.nexvora_text_secondary, false);
        root.addView(mSizeLabel, NexVoraUi.matchWrap(c, 8));
        updateSizeLabel();

        scroll.addView(root);
        setContentView(scroll);
    }

    private void changeSize(boolean increase) {
        TermuxAppSharedPreferences p = TermuxAppSharedPreferences.build(this);
        if (p == null) return;
        p.changeFontSize(increase);
        NexVoraStyle.markStyleDirty();
        updateSizeLabel();
    }

    private void updateSizeLabel() {
        TermuxAppSharedPreferences p = TermuxAppSharedPreferences.build(this);
        if (p != null && mSizeLabel != null) mSizeLabel.setText("Current size: " + p.getFontSize() + " px");
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQ_FONT && resultCode == RESULT_OK && data != null && data.getData() != null) {
            Uri uri = data.getData();
            try {
                NexVoraStyle.applyFont(this, uri);
                toast("Font applied. Return to the terminal to see it.");
            } catch (IOException e) {
                toast("Could not apply font: " + e.getMessage());
            }
        }
    }

    private void toast(String s) {
        Toast.makeText(this, s, Toast.LENGTH_LONG).show();
    }
}
