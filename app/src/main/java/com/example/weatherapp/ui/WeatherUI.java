package com.example.weatherapp.ui;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Build;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.weatherapp.R;
import com.example.weatherapp.data.Weather;

import java.util.ArrayList;
import java.util.List;

import eightbitlab.com.blurview.BlurView;

public class WeatherUI {

    private static final int PERMISSIONS_REQUEST_CODE = 123;
    private static final String[] REQUIRED_PERMISSIONS = new String[] {
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.INTERNET
    };
    private Activity activity;
    private Window window;

    public WeatherUI(Activity activity) {
        this.activity = activity;
        this.window = activity.getWindow();
    }
    public void setAppropriateUIValues() {
        setStatusBarsTransparent();
        setBluredInfo();
        requestPermissions();
        setCurrentTemperature();
    }


    private void setBluredInfo() {
        BlurView blurView1 = activity.findViewById(R.id.weatherInfo);
        BlurView blurView2 = activity.findViewById(R.id.weatherTodayBackground);
        BlurView blurView3 = activity.findViewById(R.id.weatherWeekBackground);
        ViewGroup rootLayout = activity.findViewById(R.id.blurTarget);

        BlurInitializer.initBlurView(activity, blurView1, rootLayout, 40f, 23f);
        BlurInitializer.initBlurView(activity, blurView2, rootLayout, 40f, 23f);
        BlurInitializer.initBlurView(activity, blurView3, rootLayout, 40f, 23f);

        BlurInitializer.setStroke(blurView1, 0.4f, Color.WHITE, 0.5f);
        BlurInitializer.setStroke(blurView2, 0.4f, Color.WHITE, 0.5f);
        BlurInitializer.setStroke(blurView3, 0.4f, Color.WHITE, 0.5f);
    }
    private void setStatusBarsTransparent() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            window.getDecorView().setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                            | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION  // for navigation bar
            );
            window.setStatusBarColor(Color.TRANSPARENT);
            window.setNavigationBarColor(Color.TRANSPARENT);
        }
    }
    private void requestPermissions() {
        List<String> missingPermissions = new ArrayList<>();

        for (String permission : REQUIRED_PERMISSIONS) {
            if (ContextCompat.checkSelfPermission(activity, permission) != PackageManager.PERMISSION_GRANTED) {
                missingPermissions.add(permission);
            }
        }

        if (!missingPermissions.isEmpty()) {
            // Request only the missing permissions
            ActivityCompat.requestPermissions(activity,
                    missingPermissions.toArray(new String[0]),
                    PERMISSIONS_REQUEST_CODE);
        }
    }
    private void setCurrentTemperature() {
        TextView currentTemperature = activity.findViewById(R.id.currentTemperature);
        Weather.getCurrentTemperature(activity, activity, new Weather.TemperatureCallback() {
            @Override
            public void onTemperatureReady(int temperature) {
                currentTemperature.setText(temperature+"°");
            }

            @Override
            public void onError(String message) {
                Log.e("WeatherApp", "Error: " + message);
                Toast.makeText(activity, "Error occurred, check log", Toast.LENGTH_LONG).show();
            }
        });
    }
}
