package com.example.glare;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.glare.ui.WeatherUI;
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
