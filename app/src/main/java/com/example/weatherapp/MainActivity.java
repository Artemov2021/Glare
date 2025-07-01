package com.example.weatherapp;

import android.graphics.Color;
import android.graphics.Outline;
import android.graphics.RenderEffect;
import android.graphics.Shader;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewOutlineProvider;
import android.view.Window;
import android.widget.FrameLayout;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;

import java.lang.annotation.Target;

import eightbitlab.com.blurview.BlurView;
import eightbitlab.com.blurview.RenderScriptBlur;

public class MainActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setStatusBarsTransparent();


        BlurView blurViewToday = findViewById(R.id.weatherTodayBackground);
        BlurView blurViewWeek = findViewById(R.id.weatherWeekBackground);
        BlurView blurWeatherInfo = findViewById(R.id.weatherInfo);

        ViewGroup rootLayout = findViewById(R.id.blurTarget);
        Drawable windowBackground = getWindow().getDecorView().getBackground();

        float cornerRadiusDp = 30f;
        int cornerRadiusPx = (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP, cornerRadiusDp, getResources().getDisplayMetrics());

// Setup first blur view
        blurViewToday.setupWith(rootLayout)
                .setFrameClearDrawable(windowBackground)
                .setBlurRadius(20f);

        blurViewToday.setOutlineProvider(new ViewOutlineProvider() {
            @Override
            public void getOutline(View view, Outline outline) {
                outline.setRoundRect(0, 0, view.getWidth(), view.getHeight(), cornerRadiusPx);
            }
        });
        blurViewToday.setClipToOutline(true);

// Setup second blur view
        blurViewWeek.setupWith(rootLayout)
                .setFrameClearDrawable(windowBackground)
                .setBlurRadius(20f);

        blurViewWeek.setOutlineProvider(new ViewOutlineProvider() {
            @Override
            public void getOutline(View view, Outline outline) {
                outline.setRoundRect(0, 0, view.getWidth(), view.getHeight(), cornerRadiusPx);
            }
        });
        blurViewWeek.setClipToOutline(true);

        blurWeatherInfo.setupWith(rootLayout)
                .setFrameClearDrawable(windowBackground)
                .setBlurRadius(20f);

        blurWeatherInfo.setOutlineProvider(new ViewOutlineProvider() {
            @Override
            public void getOutline(View view, Outline outline) {
                outline.setRoundRect(0, 0, view.getWidth(), view.getHeight(), cornerRadiusPx);
            }
        });
        blurWeatherInfo.setClipToOutline(true);

    }

    private void setStatusBarsTransparent() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.getDecorView().setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                            | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION  // for navigation bar
            );
            window.setStatusBarColor(Color.TRANSPARENT);
            window.setNavigationBarColor(Color.TRANSPARENT);
        }
    }
}
