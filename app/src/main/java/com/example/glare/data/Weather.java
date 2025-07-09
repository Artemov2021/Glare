package com.example.glare.data;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.glare.BuildConfig;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Locale;

import org.threeten.bp.LocalDate;
import org.threeten.bp.LocalTime;
import org.threeten.bp.format.DateTimeFormatter;


public class Weather {
    public interface WeatherCallback {
        void onTemperatureReady(int temperature);
        void onWeatherStatusReady(String weatherStatus);
        void onAirQualityReady(String quality);
        void onHumidityReady(int humidity);

        void onTodayWeatherInfosReady(ArrayList<ArrayList<Object>> temperaturesToday);
        void onWeekWeatherInfosReady(ArrayList<ArrayList<Object>> temperaturesWeek);

        void onBackgroundReady(String weatherStatus);
        void onUIReady();

        void onError(String message);
    }

    public static void setCurrentWeatherValues(Context context, Activity activity, WeatherCallback callback) {
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
                fetchVisualCrossingWeatherAsync(lat, lon, callback);
                fetchAirQualityAsync(lat,lon,callback);

            } else {
                callback.onError("Location is null");
            }
        }).addOnFailureListener(e -> callback.onError(e.getMessage()));
    }
    private static void fetchVisualCrossingWeatherAsync(double lat, double lon, WeatherCallback callback) {
        new Thread(() -> {
            try {
                String apiKey = BuildConfig.WEATHER_API_KEY;

                LocalDate today = LocalDate.now();
                LocalDate sevenDaysLater = today.plusDays(6); // total 7 days incl. today

                String urlStr = "https://weather.visualcrossing.com/VisualCrossingWebServices/rest/services/timeline/"
                        + lat + "," + lon + "/" + today + "/" + sevenDaysLater +
                        "?unitGroup=metric&include=hours,current&key=" + apiKey + "&contentType=json";

                JSONObject jsonObj = readJsonFromUrl(urlStr);

                // Current weather
                JSONObject current = jsonObj.getJSONObject("currentConditions");
                int currentTemp = (int) current.getDouble("temp");
                int humidity = current.getInt("humidity");
                String currentStatus = mapCondition(current.getString("conditions"));

                JSONArray days = jsonObj.getJSONArray("days");


                ArrayList<ArrayList<Object>> nextHours = new ArrayList<>();

                // Add "now"
                ArrayList<Object> nowInfo = new ArrayList<>();
                nowInfo.add("now");
                nowInfo.add(currentStatus);
                nowInfo.add(currentTemp);
                nextHours.add(nowInfo);

                int added = 1;
                LocalTime now = LocalTime.now();

                DateTimeFormatter inputFormat = DateTimeFormatter.ofPattern("HH:mm:ss");
                DateTimeFormatter outputFormat = DateTimeFormatter.ofPattern("HH:mm");

                for (int d = 0; d < days.length(); d++) {
                    JSONObject day = days.getJSONObject(d);
                    JSONArray hours = day.getJSONArray("hours");

                    for (int i = 0; i < hours.length(); i++) {
                        JSONObject hourObj = hours.getJSONObject(i);
                        String timeStr = hourObj.getString("datetime"); // e.g., "14:00:00"
                        LocalTime hourTime = LocalTime.parse(timeStr, inputFormat);

                        // Skip past hours today
                        if (d == 0 && hourTime.isBefore(now)) continue;

                        // Add upcoming hour
                        ArrayList<Object> info = new ArrayList<>();
                        info.add(hourTime.format(outputFormat)); // Format to "12:00"
                        info.add(mapCondition(hourObj.getString("conditions")));
                        info.add((int) hourObj.getDouble("temp"));
                        nextHours.add(info);
                        added++;

                        if (added >= 11) break;
                    }
                    if (added >= 11) break;
                }

                ArrayList<ArrayList<Object>> nextDays = new ArrayList<>();

                DateTimeFormatter dayFormatter = DateTimeFormatter.ofPattern("E", Locale.ENGLISH);

                for (int d = 0; d < Math.min(7, days.length()); d++) {
                    JSONObject day = days.getJSONObject(d);

                    String dateStr = day.getString("datetime");
                    LocalDate date = LocalDate.parse(dateStr);
                    String dayName = d == 0 ? "Today" : date.format(dayFormatter); // "Today", "Mon", "Tue"...

                    JSONArray hours = day.getJSONArray("hours");

                    double dayMaxTemp = -1000;
                    double nightMinTemp = 1000;
                    String dayCondition = "";

                    for (int i = 0; i < hours.length(); i++) {
                        JSONObject hour = hours.getJSONObject(i);
                        String time = hour.getString("datetime");
                        int hourInt = Integer.parseInt(time.split(":")[0]);

                        String condition = mapCondition(hour.getString("conditions"));
                        double temp = hour.getDouble("temp");

                        if (hourInt >= 6 && hourInt <= 18) {
                            if (temp > dayMaxTemp) {
                                dayMaxTemp = temp;
                                dayCondition = condition;
                            }
                        } else {
                            if (temp < nightMinTemp) {
                                nightMinTemp = temp;
                            }
                        }
                    }

                    ArrayList<Object> info = new ArrayList<>();
                    info.add(dayName);
                    info.add(dayCondition);
                    info.add((int) dayMaxTemp);
                    info.add((int) nightMinTemp);

                    nextDays.add(info);
                }

                new Handler(Looper.getMainLooper()).post(() -> {
                    callback.onBackgroundReady(currentStatus);

                    callback.onTemperatureReady(currentTemp);
                    callback.onHumidityReady(humidity);
                    callback.onWeatherStatusReady(currentStatus);
                    callback.onTodayWeatherInfosReady(nextHours);
                    callback.onWeekWeatherInfosReady(nextDays);
                    callback.onUIReady();
                });

            } catch (Exception e) {
                Log.d("WeatherApp Debugging", e.getMessage());
                new Handler(Looper.getMainLooper()).post(() -> callback.onError(e.getMessage()));
            }
        }).start();
    }
    private static void fetchAirQualityAsync(double lat, double lon, WeatherCallback callback) {
        new Thread(() -> {
            try {
                String urlString = "https://api.openweathermap.org/data/2.5/air_pollution?lat=" + lat + "&lon=" + lon +
                        "&appid=" + BuildConfig.OPENWEATHER_API_KEY;

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

                JSONObject obj = new JSONObject(content.toString());
                int aqi = obj.getJSONArray("list").getJSONObject(0).getJSONObject("main").getInt("aqi");

                String airQuality;
                if (aqi == 1) airQuality = "Good";
                else if (aqi <= 3) airQuality = "Moderate";
                else airQuality = "Bad";

                String finalAirQuality = airQuality;
                new Handler(Looper.getMainLooper()).post(() -> callback.onAirQualityReady(finalAirQuality));

            } catch (Exception e) {
                e.printStackTrace();
                new Handler(Looper.getMainLooper()).post(() -> callback.onError(e.getMessage()));
            }
        }).start();
    }



    private static JSONObject readJsonFromUrl(String urlString) throws IOException, JSONException, MalformedURLException {
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
        return new JSONObject(content.toString());
    }
    private static String mapCondition(String rawCondition) {
        String condition = rawCondition.toLowerCase();

        if (condition.contains("snow")) {
            return "Snowy";
        } else if (condition.contains("thunder") || condition.contains("storm")) {
            return "Stormy";
        } else if (condition.contains("rain") || condition.contains("drizzle") || condition.contains("shower")) {
            return "Rainy";
        } else if (condition.contains("cloud") || condition.contains("overcast")) {
            return "Cloudy";
        } else if (condition.contains("sun") || condition.contains("clear")) {
            if (itsNight()) {
                return "Clear night";
            }
            return "Sunny";
        } else {
            return "Cloudy"; // Fallback category
        }
    }
    private static boolean itsNight() {
        LocalTime now = LocalTime.now();
        return now.isBefore(LocalTime.of(5, 0)) || now.isAfter(LocalTime.of(22, 0));
    }

}
