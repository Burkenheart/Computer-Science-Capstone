package com.example.stevenburgettinventoryapp;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class MainActivity extends AppCompatActivity {

    private EditText editUsername;
    private EditText editPassword;

    private Button buttonLogin;
    private Button buttonCreateAccount;

    private DatabaseHelper databaseHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        ViewCompat.setOnApplyWindowInsetsListener(
                findViewById(R.id.main),
                (view, insets) -> {

                    Insets systemBars = insets.getInsets(
                            WindowInsetsCompat.Type.systemBars()
                    );

                    view.setPadding(
                            systemBars.left,
                            systemBars.top,
                            systemBars.right,
                            systemBars.bottom
                    );

                    return insets;
                }
        );

        // Connect the Java variables to the login screen controls.
        editUsername = findViewById(R.id.editUsername);
        editPassword = findViewById(R.id.editPassword);

        buttonLogin = findViewById(R.id.buttonLogin);
        buttonCreateAccount = findViewById(
                R.id.buttonCreateAccount
        );

        // Initialize the SQLite database helper.
        databaseHelper = new DatabaseHelper(this);

        /*
         * Attempt to log an existing user into the application.
         */
        buttonLogin.setOnClickListener(view -> loginUser());

        /*
         * Create a new user account using the username and password
         * entered on the login screen.
         */
        buttonCreateAccount.setOnClickListener(
                view -> createAccount()
        );
    }

    /**
     * Checks the username and password against the users table.
     * A valid account is taken to the inventory screen.
     */
    private void loginUser() {

        String username = editUsername
                .getText()
                .toString()
                .trim();

        String password = editPassword
                .getText()
                .toString()
                .trim();

        // Do not allow empty credentials.
        if (username.isEmpty() || password.isEmpty()) {

            Toast.makeText(
                    this,
                    "Please enter a username and password.",
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }

        boolean validUser = databaseHelper.checkUser(
                username,
                password
        );

        if (validUser) {

            Toast.makeText(
                    this,
                    "Login successful.",
                    Toast.LENGTH_SHORT
            ).show();

            Intent intent = new Intent(
                    MainActivity.this,
                    InventoryActivity.class
            );

            startActivity(intent);

        } else {

            Toast.makeText(
                    this,
                    "Invalid username or password.",
                    Toast.LENGTH_SHORT
            ).show();
        }
    }

    /**
     * Saves a new username and password to the users table.
     */
    private void createAccount() {

        String username = editUsername
                .getText()
                .toString()
                .trim();

        String password = editPassword
                .getText()
                .toString()
                .trim();

        // Require both fields before creating the account.
        if (username.isEmpty() || password.isEmpty()) {

            Toast.makeText(
                    this,
                    "Enter a username and password to create an account.",
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }

        boolean accountCreated = databaseHelper.addUser(
                username,
                password
        );

        if (accountCreated) {

            Toast.makeText(
                    this,
                    "Account created successfully. You can now log in.",
                    Toast.LENGTH_LONG
            ).show();

            // Clear the password after account creation.
            editPassword.setText("");

        } else {

            Toast.makeText(
                    this,
                    "That username already exists.",
                    Toast.LENGTH_SHORT
            ).show();
        }
    }
}