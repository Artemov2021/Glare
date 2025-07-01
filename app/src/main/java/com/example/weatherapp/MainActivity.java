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

import com.example.weatherapp.ui.BlurInitializer;

import java.lang.annotation.Target;

import eightbitlab.com.blurview.BlurView;
import eightbitlab.com.blurview.RenderScriptBlur;

public class MainActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initialize();

    }


    private void initialize() {
        setStatusBarsTransparent();
        setBluredInfo();
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
    private void setBluredInfo() {
        BlurView blurView1 = findViewById(R.id.weatherInfo);
        BlurView blurView2 = findViewById(R.id.weatherTodayBackground);
        BlurView blurView3 = findViewById(R.id.weatherWeekBackground);
        ViewGroup rootLayout = findViewById(R.id.blurTarget);

        BlurInitializer.initBlurView(this, blurView1, rootLayout, 20f, 30f);
        BlurInitializer.initBlurView(this, blurView2, rootLayout, 20f, 30f);
        BlurInitializer.initBlurView(this, blurView3, rootLayout, 20f, 30f);

        BlurInitializer.setStroke(blurView1, 0.4f, Color.WHITE, 0.5f);
        BlurInitializer.setStroke(blurView2, 0.4f, Color.WHITE, 0.5f);
        BlurInitializer.setStroke(blurView3, 0.4f, Color.WHITE, 0.5f);
    }
}
