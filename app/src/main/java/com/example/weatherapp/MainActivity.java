package com.example.weatherapp;

import android.annotation.SuppressLint;
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

import androidx.appcompat.app.AppCompatActivity;

import com.example.weatherapp.ui.BlurInitializer;
import com.example.weatherapp.ui.WeatherUI;


public class MainActivity extends AppCompatActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        WeatherUI weatherUI = new WeatherUI(this);

        weatherUI.setAppropriateUIValues();
        setPullToRefreshListener();

    }

    @SuppressLint("ClickableViewAccessibility")
    private void setPullToRefreshListener() {
        ScrollView scrollView = findViewById(R.id.scrollView);

        final float maxPullDistance = TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP, 80, getResources().getDisplayMetrics()); // 60dp in pixels
        scrollView.setOnTouchListener(new View.OnTouchListener() {
            float startY = 0f;
            boolean dragging = false;

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getActionMasked()) {
                    case MotionEvent.ACTION_DOWN:
                        startY = event.getY();
                        dragging = scrollView.getScrollY() == 0;
                        break;

                    case MotionEvent.ACTION_MOVE:
                        if (!dragging) break;

                        float currentY = event.getY();
                        float deltaY = currentY - startY;

                        if (deltaY > 0) {  // dragging down only
                            float translationY = Math.min(deltaY / 2, maxPullDistance); // dampen drag by half
                            scrollView.setTranslationY(translationY);
                            return true;  // consume event, prevent scrollView from scrolling
                        }
                        break;

                    case MotionEvent.ACTION_UP:
                    case MotionEvent.ACTION_CANCEL:
                        if (dragging) {
                            // Animate back to 0 translationY smoothly
                            scrollView.animate()
                                    .translationY(0)
                                    .setDuration(300)
                                    .setInterpolator(new DecelerateInterpolator())
                                    .start();

                            dragging = false;
                            return true;
                        }
                        break;
                }
                return false;  // let ScrollView handle other cases normally
            }
        });
    }

}
