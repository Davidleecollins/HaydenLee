package com.abh80.smartedge;

import android.Manifest;
import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.provider.Settings;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.google.android.material.materialswitch.MaterialSwitch;

public class MainActivity extends AppCompatActivity implements SharedPreferences.OnSharedPreferenceChangeListener {
    private SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        init();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if (!Settings.canDrawOverlays(this) || !Settings.Secure.getString(this.getContentResolver(), "enabled_notification_listeners").contains(getApplicationContext().getPackageName())
                || ActivityCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            startActivity(new Intent(this, PermissionActivity.class));
        }
        MaterialSwitch enable_btn = findViewById(R.id.enable_switch);
        enable_btn.setOnClickListener(l -> {
            sharedPreferences.edit().putBoolean("enabled", enable_btn.isChecked()).apply();
        });
        enable_btn.setChecked(sharedPreferences.getBoolean("enabled", false));

        MaterialSwitch enable_btn2 = findViewById(R.id.enable_switch2);
        enable_btn2.setOnClickListener(l -> {
            sharedPreferences.edit().putBoolean("hwd_enabled", enable_btn2.isChecked()).apply();
        });
        enable_btn2.setChecked(sharedPreferences.getBoolean("hwd_enabled", false));

    }

    public boolean isMyServiceRunning(Class<?> serviceClass) {
        // Source : https://stackoverflow.com/a/5921190
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

    private void init() {
        if (sharedPreferences == null) {
            sharedPreferences = getSharedPreferences(getPackageName(), MODE_PRIVATE);
            sharedPreferences.registerOnSharedPreferenceChangeListener(this);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        init();
        handleOverlay(false);
    }

    private void handleOverlay(boolean force) {
        if (!isMyServiceRunning(OverlayService.class)) {
            if (Settings.canDrawOverlays(this) && Settings.Secure.getString(this.getContentResolver(), "enabled_notification_listeners").contains(getApplicationContext().getPackageName())
                    && ActivityCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED) {
                if (sharedPreferences.getBoolean("enabled", false)) {
                    startForegroundService(new Intent(this, OverlayService.class));
                }

            }
        } else {
            if (force) stopService(new Intent(this, OverlayService.class));

            if (sharedPreferences.getBoolean("enabled", false)) {
                if (Settings.canDrawOverlays(this) && Settings.Secure.getString(this.getContentResolver(), "enabled_notification_listeners").contains(getApplicationContext().getPackageName())
                        && ActivityCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED) {
                    if (sharedPreferences.getBoolean("enabled", false)) {
                        startForegroundService(new Intent(this, OverlayService.class));
                    }
                }
            }
        }
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String s) {
        handleOverlay(true);
    }
}
