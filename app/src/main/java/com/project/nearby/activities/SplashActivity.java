package com.project.nearby.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;

import com.project.nearby.R;
import com.project.nearby.utils.Sharedprefs;

public class SplashActivity extends AppCompatActivity {

    private Sharedprefs sharedprefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        sharedprefs = new Sharedprefs(this);
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            Intent i;
            if (sharedprefs.getBoolean("isLogged")){
                i = new Intent(SplashActivity.this, MainActivity.class);
            }else {
                i = new Intent(SplashActivity.this, Authentication.class);
            }
            startActivity(i);
            finish();
        }, 1000);
    }
}