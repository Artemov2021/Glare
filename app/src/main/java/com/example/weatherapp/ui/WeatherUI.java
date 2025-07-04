package com.example.weatherapp.ui;

import android.Manifest;
import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.Typeface;
import android.net.ConnectivityManager;
import android.net.NetworkCapabilities;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
import android.util.Log;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.LinearInterpolator;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;
import android.net.Network;

import androidx.activity.result.ActivityResultLauncher;
import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.weatherapp.R;
import com.example.weatherapp.data.Weather;

import java.util.ArrayList;
import java.util.List;

import eightbitlab.com.blurview.BlurView;

public class WeatherUI {
    private static final int PERMISSIONS_REQUEST_CODE = 123;
    private static final int REQUEST_PERMISSION_SETTINGS = 1234;
    private ActivityResultLauncher<Intent> permissionSettingsLauncher;
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
    public void checkPermissionsAndSetAppropriateUI() {
        if (ContextCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            // Permission already granted
            checkForInternetAndSetAppropriateUI();
        } else {
            // Request it — system handles "Don't ask again" automatically
            ActivityCompat.requestPermissions(activity, new String[] {
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
            }, 1001);
        }
    }
    public void checkForInternetAndSetAppropriateUI() {
        FrameLayout permissionFrameLayout = activity.findViewById(R.id.permissionFrameLayout);
        permissionFrameLayout.setVisibility(View.INVISIBLE);

        if (!isInternetAvailable()) {
            setNoInternetUI();
        } else {
            setAppropriateUI();
        }
    }
    private void setNoInternetUI() {
        FrameLayout noInternetFrameLayout = activity.findViewById(R.id.noInternetFrameLayout);
        BlurView noInternetBlurView = activity.findViewById(R.id.noInternetBlurView);
        ImageView tryAgainButton = activity.findViewById(R.id.tryAgainButton);
        ViewGroup rootLayout = activity.findViewById(R.id.blurTarget);

        BlurInitializer.initBlurView(activity, noInternetBlurView, rootLayout, 20f, 23f);
        BlurInitializer.setStroke(noInternetBlurView, 0.4f, Color.WHITE, 0.5f);

        noInternetFrameLayout.setVisibility(View.VISIBLE);
        setTryAgainButtonPressedEffect(tryAgainButton,noInternetFrameLayout);
    }
    private void setAppropriateUI() {

    }
    public void setNoPermissionsUI() {
        // set frame layout blur effect, visibility -> true
        FrameLayout permissionFrameLayout = activity.findViewById(R.id.permissionFrameLayout);
        BlurView permissionBlurView = activity.findViewById(R.id.permissionBlurView);
        ImageView permissionButton = activity.findViewById(R.id.permissionButton);
        ViewGroup rootLayout = activity.findViewById(R.id.blurTarget);

        BlurInitializer.initBlurView(activity, permissionBlurView, rootLayout, 20f, 23f);
        BlurInitializer.setStroke(permissionBlurView, 0.4f, Color.WHITE, 0.5f);

        permissionFrameLayout.setVisibility(View.VISIBLE);
        setPermissionButtonPressedEffect(permissionButton);
    }
    @SuppressLint("ClickableViewAccessibility")
    private void setPermissionButtonPressedEffect(ImageView permissionButton) {
        permissionButton.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        v.animate().scaleX(0.9f).scaleY(0.9f).setDuration(100).start();
                        return true;

                    case MotionEvent.ACTION_UP:
                        v.animate().scaleX(1f).scaleY(1f).setDuration(100).start();
                        v.performClick(); // For accessibility

                        openPermissionSettings();

                        return true;

                    case MotionEvent.ACTION_CANCEL:
                        v.animate().scaleX(1f).scaleY(1f).setDuration(100).start();
                        return true;
                }
                return false;
            }
        });
    }
    @SuppressLint("ClickableViewAccessibility")
    private void setTryAgainButtonPressedEffect(ImageView tryAgainButton,FrameLayout noInternetFramelayout) {
        tryAgainButton.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        v.animate().scaleX(0.9f).scaleY(0.9f).setDuration(100).start();
                        return true;

                    case MotionEvent.ACTION_UP:
                        v.animate().scaleX(1f).scaleY(1f).setDuration(100).start();
                        v.performClick(); // For accessibility

                        new Handler(Looper.getMainLooper()).postDelayed(() -> {
                            tryAgainButton.setImageResource(R.drawable.reconnecting_button);
                            tryAgainButton.setEnabled(false);
                            addReconnectingAnimation(noInternetFramelayout);
                            tryReconnect();
                        }, 200);

                        return true;

                    case MotionEvent.ACTION_CANCEL:
                        v.animate().scaleX(1f).scaleY(1f).setDuration(100).start();
                        return true;
                }
                return false;
            }
        });
    }
    private void addReconnectingAnimation(FrameLayout noInternetFramelayout) {
        // Create ImageView and set image
        ImageView connectionSymbol = new ImageView(activity);
        connectionSymbol.setImageResource(R.drawable.reconnecting_button_symbol);

        // Convert dp to pixels
        int sizeDp = 20;
        int marginTopDp = 127;
        int marginStartDp = 100;
        float scale = activity.getResources().getDisplayMetrics().density;

        int sizePx = (int) (sizeDp * scale + 0.5f);
        int marginTopPx = (int) (marginTopDp * scale + 0.5f);
        int marginStartPx = (int) (marginStartDp * scale + 0.5f);

        // Set layout params with margins
        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(sizePx, sizePx);
        params.topMargin = marginTopPx;
        params.setMarginStart(marginStartPx);  // Requires API 17+

        // Optional: set gravity if needed (e.g., top-left by default)
        // params.gravity = Gravity.TOP | Gravity.START;

        connectionSymbol.setLayoutParams(params);
        noInternetFramelayout.addView(connectionSymbol);

        // Rotate the connectionSymbol infinitely
        ObjectAnimator rotation = ObjectAnimator.ofFloat(connectionSymbol, "rotation", 0f, 360f);
        rotation.setDuration(1000); // 1 second for one full rotation
        rotation.setInterpolator(new LinearInterpolator());
        rotation.setRepeatCount(ObjectAnimator.INFINITE);
        rotation.start();
    }
    private void tryReconnect() {

    }
    private void openPermissionSettings() {
        Intent intent;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            intent = new Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                    .setData(Uri.fromParts("package", activity.getPackageName(), null));
        } else {
            intent = new Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
            intent.setData(Uri.fromParts("package", activity.getPackageName(), null));
        }

        if (permissionSettingsLauncher != null) {
            permissionSettingsLauncher.launch(intent);
        } else {
            activity.startActivity(intent); // fallback
        }
    }
    public void setPermissionSettingsLauncher(ActivityResultLauncher<Intent> launcher) {
        this.permissionSettingsLauncher = launcher;
    }
    public boolean isInternetAvailable() {
        ConnectivityManager cm = (ConnectivityManager) activity.getSystemService(activity.CONNECTIVITY_SERVICE);

        if (cm == null) return false;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Network network = cm.getActiveNetwork();
            if (network == null) return false;

            NetworkCapabilities capabilities = cm.getNetworkCapabilities(network);
            return capabilities != null &&
                    (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)
                            || capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)
                            || capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET));
        } else {
            NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
            return activeNetwork != null && activeNetwork.isConnected();
        }
    }

















    @SuppressLint("ClickableViewAccessibility")
    private void setPullToRefreshListener() {
        ScrollView scrollView = activity.findViewById(R.id.scrollView);

        final float maxPullDistance = TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP, 80, activity.getResources().getDisplayMetrics()); // 60dp in pixels
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
    public void setStatusBarsTransparent() {
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
                Log.e("WeatherApp",temperature+"°");
            }

            @Override
            public void onError(String message) {
                Log.e("WeatherApp", "Error: " + message);
                Toast.makeText(activity, "Error occurred, check log", Toast.LENGTH_LONG).show();
            }
        });
    }
}
