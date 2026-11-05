package com.termux.app.nexvora;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.termux.app.TermuxActivity;

/**
 * Launcher entry point. Shows the NexVora splash (via the window background of its theme, no extra
 * dependency), then opens the Welcome screen on first run or TermuxActivity afterwards.
 */
public class NexVoraLauncherActivity extends AppCompatActivity {

    static final String PREFS = "nexvora_ui";
    static final String KEY_WELCOME_SHOWN = "welcome_shown";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        SharedPreferences p = getSharedPreferences(PREFS, Context.MODE_PRIVATE);
        Intent next;
        if (p.getBoolean(KEY_WELCOME_SHOWN, false)) {
            next = new Intent(this, TermuxActivity.class);
        } else {
            next = new Intent(this, NexVoraWelcomeActivity.class);
        }
        startActivity(next);
        finish();
    }
}
