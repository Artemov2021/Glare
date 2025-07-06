package com.example.weatherapp;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.Outline;
import android.graphics.RenderEffect;
import android.graphics.Shader;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewOutlineProvider;
import android.view.Window;
import android.view.animation.DecelerateInterpolator;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.weatherapp.ui.BlurInitializer;
import com.example.weatherapp.ui.WeatherUI;
import com.jakewharton.threetenabp.AndroidThreeTen;


public class MainActivity extends AppCompatActivity {
    private WeatherUI weatherUI;

    // Modern launcher to receive result from settings
    private ActivityResultLauncher<Intent> permissionSettingsLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AndroidThreeTen.init(this);
        setContentView(R.layout.activity_main);

        weatherUI = new WeatherUI(this);
        weatherUI.setPermissionSettingsLauncher(permissionSettingsLauncher);
        weatherUI.setStatusBarsTransparent();
        weatherUI.checkPermissionsAndSetAppropriateUI();

        setPermissionWindowResultListener();
    }



    private void setPermissionWindowResultListener() {
        // Set up launcher
        permissionSettingsLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    // Called when user returns from Settings
                    if (weatherUI != null) {
                        weatherUI.checkPermissionsAndSetAppropriateUI();
                    }
                }
        );
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == 1001) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                weatherUI.checkForInternetAndSetAppropriateUI();
            } else {
                weatherUI.setNoPermissionsUI();
            }
        }
    }
}
