package com.example.glare.ui;

import android.Manifest;
import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.Point;
import android.net.ConnectivityManager;
import android.net.NetworkCapabilities;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.LinearInterpolator;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.TextView;
import android.net.Network;

import androidx.activity.result.ActivityResultLauncher;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.glare.R;
import com.example.glare.data.Weather;

import org.threeten.bp.LocalTime;

import java.util.ArrayList;

import eightbitlab.com.blurview.BlurView;

public class WeatherUI {
    ImageView connectionSymbol;
    private ObjectAnimator rotationAnimator;
    private ObjectAnimator locationRotationAnimator;
    private ActivityResultLauncher<Intent> permissionSettingsLauncher;
    private static final String[] REQUIRED_PERMISSIONS = new String[] {
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.INTERNET
    };
    private Activity activity;
    private Window window;
    private float translationY = 0;
    private boolean isWeatherUIReloading = false;

    public WeatherUI(Activity activity) {
        this.activity = activity;
        this.window = activity.getWindow();
    }


    public void setPermissionSettingsLauncher(ActivityResultLauncher<Intent> launcher) {
        this.permissionSettingsLauncher = launcher;
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
        hidePermissionUI();

        if (!isInternetAvailable()) {
            setNoInternetUI();
        } else {
            setAppropriateUI();
        }
    }


    private void hidePermissionUI() {
        FrameLayout permissionFrameLayout = activity.findViewById(R.id.permissionFrameLayout);
        permissionFrameLayout.setVisibility(View.INVISIBLE);
    }
    private void hideNoInternetUI() {
        FrameLayout noInternetFrameLayout = activity.findViewById(R.id.noInternetFrameLayout);
        noInternetFrameLayout.setVisibility(View.INVISIBLE);
    }
    private void hideErrorUI() {
        FrameLayout errorFrameLayout = activity.findViewById(R.id.errorFrameLayout);
        errorFrameLayout.setVisibility(View.INVISIBLE);
    }
    private void setNoInternetUI() {
        ImageView background = activity.findViewById(R.id.bgImage);
        background.setImageResource(R.drawable.sunny_morning_background);

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

        setPullToRefreshListener();
        setBluredInfo();

        setWeatherValues();
    }
    public void setNoPermissionsUI() {
        // set frame layout blur effect, visibility -> true
        ImageView background = activity.findViewById(R.id.bgImage);
        background.setImageResource(R.drawable.sunny_morning_background);

        FrameLayout permissionFrameLayout = activity.findViewById(R.id.permissionFrameLayout);
        BlurView permissionBlurView = activity.findViewById(R.id.permissionBlurView);
        ImageView permissionButton = activity.findViewById(R.id.permissionButton);
        ViewGroup rootLayout = activity.findViewById(R.id.blurTarget);

        BlurInitializer.initBlurView(activity, permissionBlurView, rootLayout, 20f, 23f);
        BlurInitializer.setStroke(permissionBlurView, 0.4f, Color.WHITE, 0.5f);

        permissionFrameLayout.setVisibility(View.VISIBLE);
        setPermissionButtonPressedEffect(permissionButton);
    }
    private void setErrorUI() {
        ScrollView scrollView = activity.findViewById(R.id.scrollView);
        scrollView.setVisibility(View.INVISIBLE);

        ImageView background = activity.findViewById(R.id.bgImage);
        background.setImageResource(R.drawable.sunny_morning_background);

        FrameLayout errorFrameLayout = activity.findViewById(R.id.errorFrameLayout);
        BlurView errorBlurView = activity.findViewById(R.id.errorBlurView);
        ImageView errorTryAgainButton = activity.findViewById(R.id.errorTryAgainButton);
        ViewGroup rootLayout = activity.findViewById(R.id.blurTarget);

        BlurInitializer.initBlurView(activity,errorBlurView, rootLayout, 20f, 23f);
        BlurInitializer.setStroke(errorBlurView, 0.4f, Color.WHITE, 0.5f);

        errorFrameLayout.setVisibility(View.VISIBLE);
        setTryAgainButtonPressedEffect(errorTryAgainButton,errorFrameLayout);
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
        connectionSymbol = new ImageView(activity);
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

        connectionSymbol.setLayoutParams(params);
        noInternetFramelayout.addView(connectionSymbol);

        // Rotate the connectionSymbol infinitely
        rotationAnimator = ObjectAnimator.ofFloat(connectionSymbol, "rotation", 0f, 360f);
        rotationAnimator.setDuration(1000); // 1 second for one full rotation
        rotationAnimator.setInterpolator(new LinearInterpolator());
        rotationAnimator.setRepeatCount(ObjectAnimator.INFINITE);
        rotationAnimator.start();
    }
    private void setNormalTryAgainButton() {
        FrameLayout errorFrameLayout = activity.findViewById(R.id.errorFrameLayout);
        ImageView errorTryAgainButton = activity.findViewById(R.id.errorTryAgainButton);

        errorFrameLayout.removeView(connectionSymbol);

        errorTryAgainButton.setImageResource(R.drawable.try_again_button);
        errorTryAgainButton.setEnabled(true);
    }
    private void tryReconnect() {
        if (isInternetAvailable()) {
            stopReconnectingAnimation();
            hideNoInternetUI();
            hideErrorUI();
            setAppropriateUI();
            setNormalTryAgainButton();
        } else {
            // Optionally, use a handler to retry after a delay
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    tryReconnect(); // Try again after delay
                }
            }, 2000); // retry every 2 second
        }
    }
    private void stopReconnectingAnimation() {
        if (rotationAnimator != null && rotationAnimator.isRunning()) {
            rotationAnimator.cancel();
            rotationAnimator = null;
        }
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
                            translationY = Math.min(deltaY / 2, maxPullDistance); // dampen drag by half
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

                            if (!isWeatherUIReloading && translationY > 150) {
                                isWeatherUIReloading = true;
                                setLocationSymbolLoading();
                                reloadWeatherUI();
                            }

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
    private void setWeatherValues() {
        Weather.setCurrentWeatherValues(activity, activity, new Weather.WeatherCallback() {
            @Override
            public void onTemperatureReady(int temperature) {
                setCurrentTemperature(temperature);
            }

            @Override
            public void onWeatherStatusReady(String weatherStatus) {
                setAppropriateWeatherStatus(weatherStatus);
            }

            @Override
            public void onAirQualityReady(String quality) {
                setAirQuality(quality);
            }

            @Override
            public void onHumidityReady(int humidity) {
                setHumidity(humidity);
            }

            @Override
            public void onTodayWeatherInfosReady(ArrayList<ArrayList<Object>> temperaturesToday) {
                try {
                    for (int i = 0;i < temperaturesToday.size();i++) {
                        String weatherTodayTimeId = "weatherTodayTime" + (i+1);
                        String weatherTodaySymbolId = "weatherTodaySymbol" + (i+1);
                        String weatherTodayTemperatureId = "weatherTodayTemperature" + (i+1);

                        int weatherTodayResID = activity.getResources().getIdentifier(weatherTodayTimeId, "id", activity.getPackageName());
                        int weatherTodaySymbolResID = activity.getResources().getIdentifier(weatherTodaySymbolId, "id", activity.getPackageName());
                        int weatherTodayTemperatureResID = activity.getResources().getIdentifier(weatherTodayTemperatureId, "id", activity.getPackageName());

                        TextView weatherTodayTime = activity.findViewById(weatherTodayResID);
                        ImageView weatherTodaySymbol = activity.findViewById(weatherTodaySymbolResID);
                        TextView weatherTodayTemperature = activity.findViewById(weatherTodayTemperatureResID);

                        String time = (String) temperaturesToday.get(i).get(0);
                        String status = (String) temperaturesToday.get(i).get(1);
                        int temperature = (int) temperaturesToday.get(i).get(2);

                        setCurrentWeatherTimeSymbolTemperature(weatherTodayTime,weatherTodaySymbol,weatherTodayTemperature,time,status,temperature,"today");
                    }
                } catch (Exception e) {
                    stopReconnectingAnimation();
                    setErrorUI();
                }
            }

            @Override
            public void onWeekWeatherInfosReady(ArrayList<ArrayList<Object>> temperaturesWeek) {
                try {
                    for (int i = 0;i < temperaturesWeek.size();i++) {
                        String weatherWeekTimeId = "weatherWeekTime" + (i+1);
                        String weatherWeekSymbolId = "weatherWeekSymbol" + (i+1);
                        String weatherWeekDayTemperatureId = "weatherWeekDayTemperature" + (i+1);
                        String weatherWeekNightTemperatureId = "weatherWeekNightTemperature" + (i+1);

                        int weatherWeekResID = activity.getResources().getIdentifier(weatherWeekTimeId, "id", activity.getPackageName());
                        int weatherWeekSymbolResID = activity.getResources().getIdentifier(weatherWeekSymbolId, "id", activity.getPackageName());
                        int weatherWeekDayTemperatureResID = activity.getResources().getIdentifier(weatherWeekDayTemperatureId, "id", activity.getPackageName());
                        int weatherWeekNightTemperatureResID = activity.getResources().getIdentifier(weatherWeekNightTemperatureId, "id", activity.getPackageName());

                        TextView weatherWeekTime = activity.findViewById(weatherWeekResID);
                        ImageView weatherWeekSymbol = activity.findViewById(weatherWeekSymbolResID);
                        TextView weatherWeekDayTemperature = activity.findViewById(weatherWeekDayTemperatureResID);
                        TextView weatherWeekNightTemperature = activity.findViewById(weatherWeekNightTemperatureResID);

                        String time = (String) temperaturesWeek.get(i).get(0);
                        String status = (String) temperaturesWeek.get(i).get(1);
                        int dayTemperature = (int) temperaturesWeek.get(i).get(2);
                        int nightTemperature = (int) temperaturesWeek.get(i).get(3);

                        setWeekWeatherTimeSymbolTemperature(weatherWeekTime,weatherWeekSymbol,weatherWeekDayTemperature,weatherWeekNightTemperature,
                                time,status,dayTemperature,nightTemperature);
                    }
                } catch (Exception e) {
                    stopReconnectingAnimation();
                    setErrorUI();
                }
            }

            @Override
            public void onBackgroundReady(String weatherStatus) {
                setAppropriateBackground(weatherStatus);
            }

            @Override
            public void onUIReady() {
                ScrollView scrollView = activity.findViewById(R.id.scrollView);
                scrollView.setVisibility(View.VISIBLE);

                if (isWeatherUIReloading) {
                    // Keep the loading symbol visible for at least 1.5 seconds
                    new Handler(Looper.getMainLooper()).postDelayed(() -> {
                        isWeatherUIReloading = false;
                        stopLocationSymbolRotation();
                        setNormalLocationSymbol();
                    }, 1200); // 1500 ms = 1.5 seconds
                }
            }
            @Override
            public void onError(String message) {
                stopReconnectingAnimation();
                setErrorUI();
            }
        });
    }
    private void setCurrentTemperature(int temperature) {
        TextView currentTemperature = activity.findViewById(R.id.currentTemperature);
        ViewGroup.LayoutParams baseParams = currentTemperature.getLayoutParams();
        ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) baseParams;

        byte temperatureSize = (byte) String.valueOf(temperature).length();
        if (temperatureSize == 1) {
            params.leftMargin = convertDpIntoPx(160);
        } else if (temperatureSize == 2) {
            params.leftMargin = convertDpIntoPx(137);
        } else if (temperatureSize == 3) {
            params.leftMargin = convertDpIntoPx(115);
        }
        currentTemperature.setLayoutParams(params);
        currentTemperature.setText(temperature+"°");
    }
    private void setAppropriateWeatherStatus(String weatherStatus) {
        ImageView weatherStatusTitleSymbol = activity.findViewById(R.id.weatherStatusTitleSymbol);
        TextView weatherStatusTitle = activity.findViewById(R.id.weatherStatusTitle);
        switch (weatherStatus) {
            case "Sunny":
                weatherStatusTitleSymbol.setImageResource(R.drawable.sunny_weather_status_title_symbol);

                int sunnySymbolSizeInDp = 21;
                int sunnySymbolTopMarginInDp = 248;
                int sunnySymbolStartMarginInDp = 223;
                int sunnyTextStartMarginInDp = 153;

                // Convert to pixels
                int sunnySymbolSizePx = convertDpIntoPx(sunnySymbolSizeInDp);
                int sunnySymbolTopMarginPx = convertDpIntoPx(sunnySymbolTopMarginInDp);
                int sunnySymbolStartMarginPx = convertDpIntoPx(sunnySymbolStartMarginInDp);
                int sunnyTextStartMargin = convertDpIntoPx(sunnyTextStartMarginInDp);

                // Get layout params and cast to MarginLayoutParams
                ViewGroup.MarginLayoutParams sunnySymbolParams = (ViewGroup.MarginLayoutParams) weatherStatusTitleSymbol.getLayoutParams();
                ViewGroup.MarginLayoutParams sunnyTextParams = (ViewGroup.MarginLayoutParams) weatherStatusTitle.getLayoutParams();

                // Set size
                sunnySymbolParams.width = sunnySymbolSizePx;
                sunnySymbolParams.height = sunnySymbolSizePx;

                // Set margins
                sunnySymbolParams.topMargin = sunnySymbolTopMarginPx;
                sunnySymbolParams.leftMargin = sunnySymbolStartMarginPx;
                sunnyTextParams.leftMargin = sunnyTextStartMargin;

                // Apply updated layout params
                weatherStatusTitleSymbol.setLayoutParams(sunnySymbolParams);
                weatherStatusTitle.setLayoutParams(sunnyTextParams);

                weatherStatusTitle.setText(weatherStatus);

                break;
            case "Cloudy":
                weatherStatusTitleSymbol.setImageResource(R.drawable.cloudy_weather_status_title_symbol);

                int cloudySymbolSizeInDp = 30;
                int cloudySymbolTopMarginInDp = 242;
                int cloudySymbolStartMarginInDp = 226;
                int cloudyTextStartMarginInDp = 151;

                // Convert to pixels
                int cloudySymbolSizePx = convertDpIntoPx(cloudySymbolSizeInDp);
                int cloudySymbolTopMarginPx = convertDpIntoPx(cloudySymbolTopMarginInDp);
                int cloudySymbolStartMarginPx = convertDpIntoPx(cloudySymbolStartMarginInDp);
                int cloudyTextStartMargin = convertDpIntoPx(cloudyTextStartMarginInDp);

                // Get layout params and cast to MarginLayoutParams
                ViewGroup.MarginLayoutParams cloudySymbolParams = (ViewGroup.MarginLayoutParams) weatherStatusTitleSymbol.getLayoutParams();
                ViewGroup.MarginLayoutParams cloudyTextParams = (ViewGroup.MarginLayoutParams) weatherStatusTitle.getLayoutParams();

                // Set size
                cloudySymbolParams.width = cloudySymbolSizePx;
                cloudySymbolParams.height = cloudySymbolSizePx;

                // Set margins
                cloudySymbolParams.topMargin = cloudySymbolTopMarginPx;
                cloudySymbolParams.leftMargin = cloudySymbolStartMarginPx;
                cloudyTextParams.leftMargin = cloudyTextStartMargin;

                // Apply updated layout params
                weatherStatusTitleSymbol.setLayoutParams(cloudySymbolParams);
                weatherStatusTitle.setLayoutParams(cloudyTextParams);

                weatherStatusTitle.setText(weatherStatus);

                break;
            case "Rainy":
                weatherStatusTitleSymbol.setImageResource(R.drawable.rainy_weather_status_title_symbol);

                int rainySymbolWidthInDp = 28;
                int rainySymbolHeightInDp = 31;
                int rainySymbolTopMarginInDp = 242;
                int rainySymbolStartMarginInDp = 218;
                int rainyTextStartMarginInDp = 151;

                // Convert to pixels
                int rainySymbolWidthPx = convertDpIntoPx(rainySymbolWidthInDp,rainySymbolHeightInDp).x;
                int rainySymbolHeightPx = convertDpIntoPx(rainySymbolWidthInDp,rainySymbolHeightInDp).y;

                int rainySymbolTopMarginPx = convertDpIntoPx(rainySymbolTopMarginInDp);
                int rainySymbolStartMarginPx = convertDpIntoPx(rainySymbolStartMarginInDp);
                int rainyTextStartMargin = convertDpIntoPx(rainyTextStartMarginInDp);

                // Get layout params and cast to MarginLayoutParams
                ViewGroup.MarginLayoutParams rainySymbolParams = (ViewGroup.MarginLayoutParams) weatherStatusTitleSymbol.getLayoutParams();
                ViewGroup.MarginLayoutParams rainyTextParams = (ViewGroup.MarginLayoutParams) weatherStatusTitle.getLayoutParams();

                // Set size
                rainySymbolParams.width = rainySymbolWidthPx;
                rainySymbolParams.height = rainySymbolHeightPx;

                // Set margins
                rainySymbolParams.topMargin = rainySymbolTopMarginPx;
                rainySymbolParams.leftMargin = rainySymbolStartMarginPx;
                rainyTextParams.leftMargin = rainyTextStartMargin;

                // Apply updated layout params
                weatherStatusTitleSymbol.setLayoutParams(rainySymbolParams);
                weatherStatusTitle.setLayoutParams(rainyTextParams);

                weatherStatusTitle.setText(weatherStatus);

                break;
            case "Stormy":
                weatherStatusTitleSymbol.setImageResource(R.drawable.stormy_weather_status_title_symbol);

                int stormySymbolWidthInDp = 28;
                int stormySymbolHeightInDp = 36;
                int stormySymbolTopMarginInDp = 242;
                int stormySymbolStartMarginInDp = 225;
                int stormyTextStartMarginInDp = 147;

                // Convert to pixels
                int stormySymbolWidthPx = convertDpIntoPx(stormySymbolWidthInDp,stormySymbolHeightInDp).x;
                int stormySymbolHeightPx = convertDpIntoPx(stormySymbolWidthInDp,stormySymbolHeightInDp).y;

                int stormySymbolTopMarginPx = convertDpIntoPx(stormySymbolTopMarginInDp);
                int stormySymbolStartMarginPx = convertDpIntoPx(stormySymbolStartMarginInDp);
                int stormyTextStartMargin = convertDpIntoPx(stormyTextStartMarginInDp);

                // Get layout params and cast to MarginLayoutParams
                ViewGroup.MarginLayoutParams stormySymbolParams = (ViewGroup.MarginLayoutParams) weatherStatusTitleSymbol.getLayoutParams();
                ViewGroup.MarginLayoutParams stormyTextParams = (ViewGroup.MarginLayoutParams) weatherStatusTitle.getLayoutParams();

                // Set size
                stormySymbolParams.width = stormySymbolWidthPx;
                stormySymbolParams.height = stormySymbolHeightPx;

                // Set margins
                stormySymbolParams.topMargin = stormySymbolTopMarginPx;
                stormySymbolParams.leftMargin = stormySymbolStartMarginPx;
                stormyTextParams.leftMargin = stormyTextStartMargin;

                // Apply updated layout params
                weatherStatusTitleSymbol.setLayoutParams(stormySymbolParams);
                weatherStatusTitle.setLayoutParams(stormyTextParams);

                weatherStatusTitle.setText(weatherStatus);

                break;
            case "Snowy":
                weatherStatusTitleSymbol.setImageResource(R.drawable.snowy_weather_status_title_symbol);

                int snowySymbolWidthInDp = 31;
                int snowySymbolHeightInDp = 30;
                int snowySymbolTopMarginInDp = 242;
                int snowySymbolStartMarginInDp = 220;
                int snowyTextStartMarginInDp = 149;

                // Convert to pixels
                int snowySymbolWidthPx = convertDpIntoPx(snowySymbolWidthInDp,snowySymbolHeightInDp).x;
                int snowySymbolHeightPx = convertDpIntoPx(snowySymbolWidthInDp,snowySymbolHeightInDp).y;

                int snowySymbolTopMarginPx = convertDpIntoPx(snowySymbolTopMarginInDp);
                int snowySymbolStartMarginPx = convertDpIntoPx(snowySymbolStartMarginInDp);
                int snowyTextStartMargin = convertDpIntoPx(snowyTextStartMarginInDp);

                // Get layout params and cast to MarginLayoutParams
                ViewGroup.MarginLayoutParams snowySymbolParams = (ViewGroup.MarginLayoutParams) weatherStatusTitleSymbol.getLayoutParams();
                ViewGroup.MarginLayoutParams snowyTextParams = (ViewGroup.MarginLayoutParams) weatherStatusTitle.getLayoutParams();

                // Set size
                snowySymbolParams.width = snowySymbolWidthPx;
                snowySymbolParams.height = snowySymbolHeightPx;

                // Set margins
                snowySymbolParams.topMargin = snowySymbolTopMarginPx;
                snowySymbolParams.leftMargin = snowySymbolStartMarginPx;
                snowyTextParams.leftMargin = snowyTextStartMargin;

                // Apply updated layout params
                weatherStatusTitleSymbol.setLayoutParams(snowySymbolParams);
                weatherStatusTitle.setLayoutParams(snowyTextParams);

                weatherStatusTitle.setText(weatherStatus);

                break;
            case "Clear night":
                weatherStatusTitleSymbol.setImageResource(R.drawable.clear_night_status_title_symbol);

                int nightSymbolWidthInDp = 29;
                int nightSymbolHeightInDp = 29;
                int nightSymbolTopMarginInDp = 242;
                int nightSymbolStartMarginInDp = 244;
                int nightTextStartMarginInDp = 127;

                // Convert to pixels
                int nightSymbolWidthPx = convertDpIntoPx(nightSymbolWidthInDp,nightSymbolHeightInDp).x;
                int nightSymbolHeightPx = convertDpIntoPx(nightSymbolWidthInDp,nightSymbolHeightInDp).y;

                int nightSymbolTopMarginPx = convertDpIntoPx(nightSymbolTopMarginInDp);
                int nightSymbolStartMarginPx = convertDpIntoPx(nightSymbolStartMarginInDp);
                int nightTextStartMargin = convertDpIntoPx(nightTextStartMarginInDp);

                // Get layout params and cast to MarginLayoutParams
                ViewGroup.MarginLayoutParams nightSymbolParams = (ViewGroup.MarginLayoutParams) weatherStatusTitleSymbol.getLayoutParams();
                ViewGroup.MarginLayoutParams nightTextParams = (ViewGroup.MarginLayoutParams) weatherStatusTitle.getLayoutParams();

                // Set size
                nightSymbolParams.width = nightSymbolWidthPx;
                nightSymbolParams.height = nightSymbolHeightPx;

                // Set margins
                nightSymbolParams.topMargin = nightSymbolTopMarginPx;
                nightSymbolParams.leftMargin = nightSymbolStartMarginPx;
                nightTextParams.leftMargin = nightTextStartMargin;

                // Apply updated layout params
                weatherStatusTitleSymbol.setLayoutParams(nightSymbolParams);
                weatherStatusTitle.setLayoutParams(nightTextParams);

                weatherStatusTitle.setText(weatherStatus);

                break;
        }
    }
    private void setAirQuality(String quality) {
        TextView airQualityStatus = activity.findViewById(R.id.airQualityStatus);
        airQualityStatus.setText(quality);
    }
    private void setHumidity(int humidity) {
        TextView humidityStatus = activity.findViewById(R.id.humidityStatus);
        humidityStatus.setText(humidity+"%");
    }
    private void setCurrentWeatherTimeSymbolTemperature(TextView timeTextView,ImageView weatherSymbol,TextView temperatureTextView
            ,String time,String status,int temperature,String block) {
        setWeatherTodayTime(timeTextView,time);
        setWeatherIcon(weatherSymbol,status,block);
        setTemperature(temperatureTextView,temperature,status);
    }
    private void setWeekWeatherTimeSymbolTemperature(TextView timeTextView,ImageView weatherSymbol,TextView dayTemperatureTextView
            ,TextView nightTemperatureTextView,String time,String status,int dayTemperature,int nightTemperature) {
        setCurrentWeatherTimeSymbolTemperature(timeTextView,weatherSymbol,dayTemperatureTextView,time,status,dayTemperature,"week");
        setWeekWeatherNightTemperature(nightTemperatureTextView,nightTemperature);
    }
    private void setWeekWeatherNightTemperature(TextView nightTemperatureTextView,int nightTemperature) {
        nightTemperatureTextView.setText(nightTemperature+"°");
    }
    private void setAppropriateBackground(String weatherStatus) {
        ImageView background = activity.findViewById(R.id.bgImage);
        switch (weatherStatus) {
            case "Sunny":
                if (itsMorning()) {
                    background.setImageResource(R.drawable.sunny_morning_background);
                } else if (itsDay()) {
                    background.setImageResource(R.drawable.sunny_day_background);
                } else if (itsEvening()) {
                    background.setImageResource(R.drawable.sunny_evening_background);
                } else if (itsNight()) {
                    background.setImageResource(R.drawable.clear_night_background);
                }
                break;
            case "Cloudy":
            case "Rainy":
                if (itsMorning() || itsDay() || itsEvening()) {
                    background.setImageResource(R.drawable.rainy_day_background);
                } else {
                    background.setImageResource(R.drawable.rainy_night_background);
                }
                break;
            case "Snowy":
                if (itsMorning()) {
                    background.setImageResource(R.drawable.snowy_morning_background);
                } else if (itsDay()){
                    background.setImageResource(R.drawable.snowy_day_background);
                } else {
                    background.setImageResource(R.drawable.snowy_night_background);
                }
                break;
        }
    }

    private int convertDpIntoPx(int sizeDp) {
        return (int) (sizeDp * Resources.getSystem().getDisplayMetrics().density + 0.5f);
    }
    private Point convertDpIntoPx(int widthDp, int heightDp) {
        float density = Resources.getSystem().getDisplayMetrics().density;
        int widthPx = (int) (widthDp * density + 0.5f);
        int heightPx = (int) (heightDp * density + 0.5f);
        return new Point(widthPx, heightPx);
    }

    private void setWeatherTodayTime(TextView timeTextView,String time) {
        timeTextView.setText(time);
    }
    private void setWeatherIcon(ImageView symbol,String status,String block) {
        ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) symbol.getLayoutParams();
        switch (status) {
            case "Sunny":
                symbol.setImageResource(R.drawable.sunny_weather_symbol);
                params.width = convertDpIntoPx(32);
                params.height = convertDpIntoPx(32);
                params.topMargin = convertDpIntoPx(22);
                break;
            case "Cloudy":
                symbol.setImageResource(R.drawable.cloudy_weather_symbol);
                params.width = convertDpIntoPx(51);
                params.height = convertDpIntoPx(51);
                params.topMargin = convertDpIntoPx(11);
                break;
            case "Rainy":
                symbol.setImageResource(R.drawable.rainy_weather_symbol);
                params.width = convertDpIntoPx(54,60).x;
                params.height = convertDpIntoPx(54,60).y;
                params.topMargin = convertDpIntoPx(11);
                break;
            case "Stormy":
                symbol.setImageResource(R.drawable.stormy_weather_symbol);
                params.width = convertDpIntoPx(54,68).x;
                params.height = convertDpIntoPx(54,68).y;
                params.topMargin = convertDpIntoPx(9);
                break;
            case "Snowy":
                symbol.setImageResource(R.drawable.snowy_weather_symbol);
                params.width = convertDpIntoPx(48,47).x;
                params.height = convertDpIntoPx(48,47).y;
                params.topMargin = convertDpIntoPx(13);
                break;
            case "Clear night":
                if (block.equals("today")) {
                    symbol.setImageResource(R.drawable.clear_night_symbol);
                    params.width = convertDpIntoPx(35);
                    params.height = convertDpIntoPx(35);
                    params.topMargin = convertDpIntoPx(22);
                    break;
                } else {
                    symbol.setImageResource(R.drawable.sunny_weather_symbol);
                    params.width = convertDpIntoPx(32);
                    params.height = convertDpIntoPx(32);
                    params.topMargin = convertDpIntoPx(22);
                    break;
                }
        }
        symbol.setLayoutParams(params);
    }
    private void setTemperature(TextView temperatureTextView,int temperature,String status) {
        ViewGroup.LayoutParams baseParams = temperatureTextView.getLayoutParams();
        if (baseParams instanceof ViewGroup.MarginLayoutParams) {
            ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) baseParams;

            switch (status) {
                case "Sunny":
                    params.topMargin = convertDpIntoPx(27);
                    break;
                case "Cloudy":
                    params.topMargin = convertDpIntoPx(19);
                    break;
                case "Rainy":
                    params.topMargin = convertDpIntoPx(10);
                    break;
                case "Stormy":
                    params.topMargin = convertDpIntoPx(4);
                    break;
                case "Snowy":
                    params.topMargin = convertDpIntoPx(21);
                    break;
                case "Clear night":
                    params.topMargin = convertDpIntoPx(24);
                    break;
            }
            temperatureTextView.setText(temperature+"°");
            temperatureTextView.setLayoutParams(params);
        }
    }

    private boolean itsMorning() {
        LocalTime now = LocalTime.now();
        return !now.isBefore(LocalTime.of(5, 0)) && now.isBefore(LocalTime.of(11, 0));
    }
    private boolean itsDay() {
        LocalTime now = LocalTime.now();
        return !now.isBefore(LocalTime.of(11, 0)) && now.isBefore(LocalTime.of(16, 0));
    }
    private boolean itsEvening() {
        LocalTime now = LocalTime.now();
        return !now.isBefore(LocalTime.of(16, 0)) && now.isBefore(LocalTime.of(20, 0));
    }
    private boolean itsNight() {
        LocalTime now = LocalTime.now();
        return now.isBefore(LocalTime.of(5, 0)) || !now.isBefore(LocalTime.of(20, 0));
    }

    private void reloadWeatherUI() {
        setWeatherValues();
    }
    private void setLocationSymbolLoading() {
        ImageView locationSymbol = activity.findViewById(R.id.currentLocationTitleSymbol);
        locationSymbol.setImageResource(R.drawable.current_location_loading_title_symbol);

        // Convert 15dp to px for size
        int sizeInPx = convertDpIntoPx(14);
        ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) locationSymbol.getLayoutParams();
        params.width = sizeInPx;
        params.height = sizeInPx;

        // Convert 10dp to px for top margin
        int topMarginInPx = convertDpIntoPx(53);
        params.topMargin = topMarginInPx;

        locationSymbol.setLayoutParams(params);

        // Start rotating animation
        locationRotationAnimator = ObjectAnimator.ofFloat(locationSymbol, "rotation", 0f, 360f);
        locationRotationAnimator.setDuration(1000); // 1 second per rotation
        locationRotationAnimator.setRepeatCount(ObjectAnimator.INFINITE);
        locationRotationAnimator.setInterpolator(new LinearInterpolator());
        locationRotationAnimator.start();
    }
    private void stopLocationSymbolRotation() {
        if (locationRotationAnimator != null && locationRotationAnimator.isRunning()) {
            locationRotationAnimator.cancel();
            locationRotationAnimator = null;

            // Reset rotation in case you want the image to be upright
            ImageView locationSymbol = activity.findViewById(R.id.currentLocationTitleSymbol);
            locationSymbol.setRotation(0f);
        }
    }
    private void setNormalLocationSymbol() {
        ImageView locationSymbol = activity.findViewById(R.id.currentLocationTitleSymbol);
        locationSymbol.setImageResource(R.drawable.current_location_title_symbol);

        int sizeInPx = convertDpIntoPx(18);
        ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) locationSymbol.getLayoutParams();
        params.width = sizeInPx;
        params.height = sizeInPx;

        int topMarginInPx = convertDpIntoPx(51);
        params.topMargin = topMarginInPx;

        locationSymbol.setLayoutParams(params);
    }
}
