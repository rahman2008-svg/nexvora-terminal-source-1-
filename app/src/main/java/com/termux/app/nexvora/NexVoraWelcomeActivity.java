package com.termux.app.nexvora;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.Gravity;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.termux.R;
import com.termux.app.TermuxActivity;

/** First-run welcome screen. */
public class NexVoraWelcomeActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        final Context c = this;

        ScrollView scroll = new ScrollView(c);
        scroll.setFillViewport(true);
        scroll.setBackgroundColor(NexVoraUi.color(c, R.color.nexvora_black));

        LinearLayout root = new LinearLayout(c);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setGravity(Gravity.CENTER_HORIZONTAL);
        int pad = NexVoraUi.dp(c, 24);
        root.setPadding(pad, NexVoraUi.dp(c, 56), pad, pad);

        ImageView logo = new ImageView(c);
        logo.setImageResource(R.mipmap.ic_launcher);
        root.addView(logo, new LinearLayout.LayoutParams(NexVoraUi.dp(c, 96), NexVoraUi.dp(c, 96)));

        TextView title = NexVoraUi.text(c, "Welcome to " + NexVoraBrand.APP_NAME, 26, R.color.nexvora_text_primary, true);
        title.setGravity(Gravity.CENTER);
        root.addView(title, NexVoraUi.matchWrap(c, 24));

        TextView tag = NexVoraUi.text(c, NexVoraBrand.TAGLINE, 15, R.color.nexvora_cyan, false);
        tag.setGravity(Gravity.CENTER);
        root.addView(tag, NexVoraUi.matchWrap(c, 8));

        String[][] features = {
            {"Real terminal", "A full Linux environment with Bash and package management."},
            {"Multiple sessions", "Swipe in from the left edge to manage sessions."},
            {"Made yours", "Color themes, fonts, extra keys and a custom startup greeting."},
        };
        for (String[] f : features)
            root.addView(NexVoraUi.menuCard(c, f[0], f[1], null), NexVoraUi.matchWrap(c, 14));

        TextView start = NexVoraUi.primaryButton(c, "Start Terminal", v -> {
            getSharedPreferences(NexVoraLauncherActivity.PREFS, Context.MODE_PRIVATE)
                .edit().putBoolean(NexVoraLauncherActivity.KEY_WELCOME_SHOWN, true).apply();
            startActivity(new Intent(c, TermuxActivity.class));
            finish();
        });
        root.addView(start, NexVoraUi.matchWrap(c, 28));

        TextView credit = NexVoraUi.text(c, NexVoraBrand.COPYRIGHT, 11, R.color.nexvora_text_secondary, false);
        credit.setGravity(Gravity.CENTER);
        root.addView(credit, NexVoraUi.matchWrap(c, 20));

        scroll.addView(root);
        setContentView(scroll);
    }
}
