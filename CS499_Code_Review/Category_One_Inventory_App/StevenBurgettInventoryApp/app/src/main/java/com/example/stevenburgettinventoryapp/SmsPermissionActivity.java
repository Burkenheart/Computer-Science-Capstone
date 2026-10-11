package com.example.stevenburgettinventoryapp;

import android.Manifest;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

public class SmsPermissionActivity extends AppCompatActivity {

    // SharedPreferences keys used by this screen and InventoryActivity.
    public static final String PREFS_NAME = "InventoryPreferences";
    public static final String SMS_ENABLED_KEY = "sms_enabled";
    public static final String PHONE_NUMBER_KEY = "phone_number";

    private TextView textPermissionStatus;
    private EditText editPhone;

    private SharedPreferences sharedPreferences;

    /*
     * Receives the result of Android's SEND_SMS permission request.
     */
    private final ActivityResultLauncher<String> smsPermissionLauncher =
            registerForActivityResult(
                    new ActivityResultContracts.RequestPermission(),
                    isGranted -> {

                        if (isGranted) {

                            saveSmsSettings();

                            textPermissionStatus.setText(
                                    "SMS permission granted. Low inventory alerts are enabled."
                            );

                        } else {

                            sharedPreferences.edit()
                                    .putBoolean(SMS_ENABLED_KEY, false)
                                    .apply();

                            textPermissionStatus.setText(
                                    "SMS permission denied. The inventory app will continue to function normally."
                            );
                        }
                    }
            );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sms_permission);

        Button buttonAllow = findViewById(R.id.buttonAllow);
        Button buttonDeny = findViewById(R.id.buttonDeny);
        Button buttonReturn = findViewById(R.id.buttonReturn);

        editPhone = findViewById(R.id.editPhone);
        textPermissionStatus =
                findViewById(R.id.textPermissionStatus);

        sharedPreferences = getSharedPreferences(
                PREFS_NAME,
                Context.MODE_PRIVATE
        );

        /*
         * Restore the previously saved phone number.
         */
        String savedPhoneNumber =
                sharedPreferences.getString(
                        PHONE_NUMBER_KEY,
                        ""
                );

        editPhone.setText(savedPhoneNumber);

        updatePermissionStatus();

        /*
         * Validate the phone number and request Android SMS permission.
         */
        buttonAllow.setOnClickListener(
                view -> requestSmsPermission()
        );

        /*
         * Disable SMS notifications without disabling any
         * other part of the inventory application.
         */
        buttonDeny.setOnClickListener(view -> {

            sharedPreferences.edit()
                    .putBoolean(SMS_ENABLED_KEY, false)
                    .apply();

            textPermissionStatus.setText(
                    "SMS notifications disabled. The inventory app will continue to function normally."
            );

            Toast.makeText(
                    this,
                    "SMS notifications disabled.",
                    Toast.LENGTH_SHORT
            ).show();
        });

        buttonReturn.setOnClickListener(
                view -> finish()
        );
    }

    /**
     * Validates the phone number and requests SEND_SMS
     * permission when necessary.
     */
    private void requestSmsPermission() {

        String phoneNumber = editPhone
                .getText()
                .toString()
                .trim();

        if (phoneNumber.isEmpty()) {

            Toast.makeText(
                    this,
                    "Please enter a phone number.",
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }

        /*
         * Save the number before launching the Android permission
         * dialog so it remains available when the result returns.
         */
        sharedPreferences.edit()
                .putString(
                        PHONE_NUMBER_KEY,
                        phoneNumber
                )
                .apply();

        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.SEND_SMS
        ) == PackageManager.PERMISSION_GRANTED) {

            saveSmsSettings();

            textPermissionStatus.setText(
                    "SMS permission granted. Low inventory alerts are enabled."
            );

            Toast.makeText(
                    this,
                    "SMS notifications enabled.",
                    Toast.LENGTH_SHORT
            ).show();

        } else {

            smsPermissionLauncher.launch(
                    Manifest.permission.SEND_SMS
            );
        }
    }

    /**
     * Saves the phone number and enables SMS notifications.
     */
    private void saveSmsSettings() {

        String phoneNumber = editPhone
                .getText()
                .toString()
                .trim();

        sharedPreferences.edit()
                .putString(
                        PHONE_NUMBER_KEY,
                        phoneNumber
                )
                .putBoolean(
                        SMS_ENABLED_KEY,
                        true
                )
                .apply();
    }

    /**
     * Displays the current notification status.
     */
    private void updatePermissionStatus() {

        boolean permissionGranted =
                ContextCompat.checkSelfPermission(
                        this,
                        Manifest.permission.SEND_SMS
                ) == PackageManager.PERMISSION_GRANTED;

        boolean smsEnabled =
                sharedPreferences.getBoolean(
                        SMS_ENABLED_KEY,
                        false
                );

        if (permissionGranted && smsEnabled) {

            textPermissionStatus.setText(
                    "SMS permission granted. Low inventory alerts are enabled."
            );

        } else {

            textPermissionStatus.setText(
                    "SMS notifications are currently disabled. The inventory app will continue to function normally."
            );
        }
    }
}