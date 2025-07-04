package com.example.weatherapp.data;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class Weather {

    public interface TemperatureCallback {
        void onTemperatureReady(int temperature);
        void onError(String message);
    }

    public static void getCurrentTemperature(Context context, Activity activity, TemperatureCallback callback) {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION)
                        != PackageManager.PERMISSION_GRANTED) {
            // Request permissions
            ActivityCompat.requestPermissions(activity, new String[]{
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
            }, 1001);
            callback.onError("Location permission requested");
            return;
        }

        FusedLocationProviderClient fusedLocationClient = LocationServices.getFusedLocationProviderClient(context);

        fusedLocationClient.getLastLocation().addOnSuccessListener(location -> {
            if (location != null) {
                double lat = location.getLatitude();
                double lon = location.getLongitude();

                // Call your weather API async here
                fetchWeatherAsync(lat, lon, callback);

            } else {
                callback.onError("Location is null");
            }
        }).addOnFailureListener(e -> callback.onError(e.getMessage()));
    }
    private static void fetchWeatherAsync(double lat, double lon, TemperatureCallback callback) {
        new Thread(() -> {
            try {
                String urlString = "https://api.openweathermap.org/data/2.5/weather?lat=" + lat + "&lon=" + lon +
                        "&units=metric&appid=b7e9ba5dcd023a261c61d7a0805a39c9";

                URL url = new URL(urlString);
                HttpURLConnection con = (HttpURLConnection) url.openConnection();
                con.setRequestMethod("GET");

                BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
                StringBuilder content = new StringBuilder();
                String inputLine;

                while ((inputLine = in.readLine()) != null) {
                    content.append(inputLine);
                }
                in.close();
                con.disconnect();

                Log.d("WeatherDebug", "Raw JSON response: " + content.toString());
                JSONObject obj = new JSONObject(content.toString());
                int temp = (int) obj.getJSONObject("main").getDouble("temp");

                // Callback on main thread
                new Handler(Looper.getMainLooper()).post(() -> callback.onTemperatureReady(temp));

            } catch (Exception e) {
                e.printStackTrace();
                new Handler(Looper.getMainLooper()).post(() -> callback.onError(e.getMessage()));
            }
        }).start();
    }
}
