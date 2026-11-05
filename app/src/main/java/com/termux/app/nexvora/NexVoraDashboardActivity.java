package com.termux.app.nexvora;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.termux.R;
import com.termux.app.TermuxActivity;
import com.termux.app.activities.SettingsActivity;

/** NexVora settings dashboard. Terminal-engine settings stay in the original Termux SettingsActivity. */
public class NexVoraDashboardActivity extends AppCompatActivity {

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

        root.addView(NexVoraUi.text(c, "Settings", 28, R.color.nexvora_text_primary, true));
        TextView sub = NexVoraUi.text(c, NexVoraBrand.APP_NAME + " \u00B7 v" + versionName(),
            13, R.color.nexvora_cyan, false);
        root.addView(sub, NexVoraUi.matchWrap(c, 4));

        root.addView(NexVoraUi.sectionTitle(c, "Personalize"), NexVoraUi.matchWrap(c, 24));
        root.addView(NexVoraUi.menuCard(c, "Appearance",
            "Color themes, terminal font and text size",
            v -> startActivity(new Intent(c, NexVoraAppearanceActivity.class))), NexVoraUi.matchWrap(c, 10));
        root.addView(NexVoraUi.menuCard(c, "Startup greeting",
            "Message shown when a new session starts",
            v -> startActivity(new Intent(c, NexVoraGreetingActivity.class))), NexVoraUi.matchWrap(c, 10));

        root.addView(NexVoraUi.sectionTitle(c, "Terminal"), NexVoraUi.matchWrap(c, 24));
        root.addView(NexVoraUi.menuCard(c, "Terminal settings",
            "Extra keys, input, view, debugging (original engine settings)",
            v -> startActivity(new Intent(c, SettingsActivity.class))), NexVoraUi.matchWrap(c, 10));
        root.addView(NexVoraUi.menuCard(c, "Open terminal",
            "Return to your sessions",
            v -> { startActivity(new Intent(c, TermuxActivity.class)); finish(); }), NexVoraUi.matchWrap(c, 10));

        root.addView(NexVoraUi.sectionTitle(c, "About"), NexVoraUi.matchWrap(c, 24));
        root.addView(NexVoraUi.menuCard(c, "About " + NexVoraBrand.BRAND,
            "Version, developer, contact and licenses",
            v -> startActivity(new Intent(c, NexVoraAboutActivity.class))), NexVoraUi.matchWrap(c, 10));

        scroll.addView(root);
        setContentView(scroll);
    }

    private String versionName() {
        try {
            return getPackageManager().getPackageInfo(getPackageName(), 0).versionName;
        } catch (Exception e) {
            return "?";
        }
    }
}
