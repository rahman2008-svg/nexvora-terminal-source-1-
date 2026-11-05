package com.termux.app.nexvora;

import android.content.Context;
import android.os.Bundle;
import android.text.InputType;
import android.view.Gravity;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.termux.R;

import java.io.IOException;

/** Edits $PREFIX/etc/motd, the text printed by Termux's login script when a session starts. */
public class NexVoraGreetingActivity extends AppCompatActivity {

    private EditText mEdit;

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

        root.addView(NexVoraUi.text(c, "Startup greeting", 28, R.color.nexvora_text_primary, true));
        root.addView(NexVoraUi.text(c,
            "Shown at the start of each new login shell session. Leave empty to show nothing.",
            13, R.color.nexvora_text_secondary, false), NexVoraUi.matchWrap(c, 6));

        mEdit = new EditText(c);
        mEdit.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_MULTI_LINE
            | InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS);
        mEdit.setMinLines(6);
        mEdit.setGravity(Gravity.TOP | Gravity.START);
        mEdit.setTextColor(NexVoraUi.color(c, R.color.nexvora_cyan));
        mEdit.setHintTextColor(NexVoraUi.color(c, R.color.nexvora_text_secondary));
        mEdit.setHint("Welcome back, developer.");
        mEdit.setTypeface(android.graphics.Typeface.MONOSPACE);
        String existing = NexVoraStyle.readGreeting();
        mEdit.setText(existing.isEmpty() ? NexVoraBrand.DEFAULT_GREETING : existing);
        root.addView(mEdit, NexVoraUi.matchWrap(c, 16));

        root.addView(NexVoraUi.primaryButton(c, "Save greeting", v -> save(mEdit.getText().toString())),
            NexVoraUi.matchWrap(c, 16));
        root.addView(NexVoraUi.outlineButton(c, "Restore NexVora default", v -> {
            mEdit.setText(NexVoraBrand.DEFAULT_GREETING);
            save(NexVoraBrand.DEFAULT_GREETING);
        }), NexVoraUi.matchWrap(c, 10));

        scroll.addView(root);
        setContentView(scroll);
    }

    private void save(String text) {
        try {
            NexVoraStyle.writeGreeting(text);
            Toast.makeText(this, "Greeting saved. It appears in new sessions.", Toast.LENGTH_LONG).show();
        } catch (IOException e) {
            Toast.makeText(this, "Could not save: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }
}
