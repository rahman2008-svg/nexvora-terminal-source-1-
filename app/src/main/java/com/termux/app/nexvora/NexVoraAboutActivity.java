package com.termux.app.nexvora;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.Gravity;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.termux.BuildConfig;
import com.termux.R;
import com.termux.shared.termux.TermuxConstants;

/** About NexVora: version, developer information, contact links and license notice. */
public class NexVoraAboutActivity extends AppCompatActivity {

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
        root.setGravity(Gravity.CENTER_HORIZONTAL);

        ImageView logo = new ImageView(c);
        logo.setImageResource(R.mipmap.ic_launcher);
        root.addView(logo, new LinearLayout.LayoutParams(NexVoraUi.dp(c, 80), NexVoraUi.dp(c, 80)));
        TextView name = NexVoraUi.text(c, NexVoraBrand.APP_NAME, 24, R.color.nexvora_text_primary, true);
        name.setGravity(Gravity.CENTER);
        root.addView(name, NexVoraUi.matchWrap(c, 12));
        TextView ver = NexVoraUi.text(c, "Version " + BuildConfig.VERSION_NAME + " (" + BuildConfig.VERSION_CODE + ")",
            13, R.color.nexvora_cyan, false);
        ver.setGravity(Gravity.CENTER);
        root.addView(ver, NexVoraUi.matchWrap(c, 4));

        if (BuildConfig.NEXVORA_DEVTEST) {
            TextView warn = NexVoraUi.text(c, "DEVELOPMENT TEST BUILD (com.termux). Not a NexVora Terminal release - do not distribute.",
                12, R.color.nexvora_purple, true);
            warn.setGravity(Gravity.CENTER);
            root.addView(warn, NexVoraUi.matchWrap(c, 8));
        }

        // Version / build info
        LinearLayout info = NexVoraUi.card(c, null);
        info.addView(NexVoraUi.sectionTitle(c, "Version information"));
        info.addView(line(c, "Package", TermuxConstants.TERMUX_PACKAGE_NAME));
        info.addView(line(c, "Version name", BuildConfig.VERSION_NAME));
        info.addView(line(c, "Version code", String.valueOf(BuildConfig.VERSION_CODE)));
        info.addView(line(c, "Bootstrap variant", BuildConfig.TERMUX_PACKAGE_VARIANT));
        root.addView(info, NexVoraUi.matchWrap(c, 20));

        // Developer
        LinearLayout dev = NexVoraUi.card(c, null);
        dev.addView(NexVoraUi.sectionTitle(c, "Developer"));
        dev.addView(line(c, "Developer", NexVoraBrand.DEVELOPER_NAME));
        dev.addView(line(c, "Company", NexVoraBrand.COMPANY));
        root.addView(dev, NexVoraUi.matchWrap(c, 12));

        // Contact
        LinearLayout contact = NexVoraUi.card(c, null);
        contact.addView(NexVoraUi.sectionTitle(c, "Contact"));
        for (int i = 0; i < NexVoraBrand.WHATSAPP_NUMBERS.length; i++) {
            final String intl = NexVoraBrand.WHATSAPP_INTERNATIONAL[i];
            contact.addView(link(c, "WhatsApp  " + NexVoraBrand.WHATSAPP_NUMBERS[i],
                () -> open(Uri.parse("https://wa.me/" + intl), Intent.ACTION_VIEW)));
        }
        contact.addView(link(c, "Facebook", () -> open(Uri.parse(NexVoraBrand.FACEBOOK_URL), Intent.ACTION_VIEW)));
        contact.addView(link(c, "Instagram", () -> open(Uri.parse(NexVoraBrand.INSTAGRAM_URL), Intent.ACTION_VIEW)));
        contact.addView(link(c, NexVoraBrand.EMAIL, () -> open(Uri.parse("mailto:" + NexVoraBrand.EMAIL), Intent.ACTION_SENDTO)));
        root.addView(contact, NexVoraUi.matchWrap(c, 12));

        // License
        LinearLayout lic = NexVoraUi.card(c, null);
        lic.addView(NexVoraUi.sectionTitle(c, "Open-source notice"));
        TextView notice = NexVoraUi.text(c, NexVoraBrand.LICENSE_NOTICE, 12, R.color.nexvora_text_secondary, false);
        notice.setPadding(0, NexVoraUi.dp(c, 8), 0, 0);
        lic.addView(notice);
        lic.addView(link(c, "Upstream project: Termux", () -> open(Uri.parse(NexVoraBrand.UPSTREAM_URL), Intent.ACTION_VIEW)));
        root.addView(lic, NexVoraUi.matchWrap(c, 12));

        TextView copy = NexVoraUi.text(c, NexVoraBrand.COPYRIGHT, 11, R.color.nexvora_text_secondary, false);
        copy.setGravity(Gravity.CENTER);
        root.addView(copy, NexVoraUi.matchWrap(c, 20));

        scroll.addView(root);
        setContentView(scroll);
    }

    private TextView line(Context c, String k, String v) {
        TextView t = NexVoraUi.text(c, k + ":  " + v, 14, R.color.nexvora_text_primary, false);
        t.setPadding(0, NexVoraUi.dp(c, 8), 0, 0);
        return t;
    }

    private TextView link(Context c, String label, Runnable r) {
        TextView t = NexVoraUi.text(c, label, 15, R.color.nexvora_cyan, false);
        t.setPadding(0, NexVoraUi.dp(c, 12), 0, NexVoraUi.dp(c, 4));
        t.setClickable(true);
        t.setOnClickListener(v -> r.run());
        return t;
    }

    private void open(Uri uri, String action) {
        try {
            startActivity(new Intent(action, uri));
        } catch (ActivityNotFoundException e) {
            Toast.makeText(this, "No app available to open this link", Toast.LENGTH_SHORT).show();
        }
    }
}
