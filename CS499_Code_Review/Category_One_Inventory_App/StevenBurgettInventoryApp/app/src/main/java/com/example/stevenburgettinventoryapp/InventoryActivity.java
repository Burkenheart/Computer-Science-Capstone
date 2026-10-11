package com.example.stevenburgettinventoryapp;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.text.InputType;
import android.view.Gravity;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

public class InventoryActivity extends AppCompatActivity {

    private DatabaseHelper databaseHelper;

    private EditText editItemName;
    private EditText editItemQuantity;

    private TableLayout tableInventory;
    private TextView textLowInventory;

    // Quantity at or below this value is considered low inventory.
    private static final int LOW_INVENTORY_THRESHOLD = 5;

    // SharedPreferences values used for SMS notification settings.
    private static final String PREFS_NAME = "InventoryPreferences";
    private static final String SMS_ENABLED_KEY = "sms_enabled";
    private static final String PHONE_NUMBER_KEY = "phone_number";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_inventory);

        // Initialize the SQLite database helper.
        databaseHelper = new DatabaseHelper(this);

        // Connect UI controls.
        editItemName = findViewById(R.id.editItemName);
        editItemQuantity = findViewById(R.id.editItemQuantity);

        tableInventory = findViewById(R.id.tableInventory);
        textLowInventory = findViewById(R.id.textLowInventory);

        Button buttonAddItem = findViewById(R.id.buttonAddItem);
        Button buttonSmsSettings = findViewById(R.id.buttonSmsSettings);
        Button buttonLogout = findViewById(R.id.buttonLogout);

        // Add a new inventory item.
        buttonAddItem.setOnClickListener(
                view -> addInventoryItem()
        );

        // Open SMS notification settings.
        buttonSmsSettings.setOnClickListener(view -> {

            Intent intent = new Intent(
                    InventoryActivity.this,
                    SmsPermissionActivity.class
            );

            startActivity(intent);
        });

        // Log out and return to the login screen.
        buttonLogout.setOnClickListener(view -> {

            Intent intent = new Intent(
                    InventoryActivity.this,
                    MainActivity.class
            );

            intent.addFlags(
                    Intent.FLAG_ACTIVITY_CLEAR_TOP |
                            Intent.FLAG_ACTIVITY_NEW_TASK
            );

            startActivity(intent);
            finish();
        });

        // Display existing inventory when the screen opens.
        loadInventory();
    }

    /**
     * Adds a new inventory item using the values entered
     * by the user.
     */
    private void addInventoryItem() {

        String itemName = editItemName
                .getText()
                .toString()
                .trim();

        String quantityText = editItemQuantity
                .getText()
                .toString()
                .trim();

        // Both fields are required.
        if (itemName.isEmpty() || quantityText.isEmpty()) {

            Toast.makeText(
                    this,
                    "Please enter an equipment name and quantity.",
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }

        int quantity;

        try {

            quantity = Integer.parseInt(quantityText);

        } catch (NumberFormatException exception) {

            Toast.makeText(
                    this,
                    "Please enter a valid quantity.",
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }

        boolean itemAdded = databaseHelper.addItem(
                itemName,
                quantity
        );

        if (itemAdded) {

            Toast.makeText(
                    this,
                    "Inventory item added.",
                    Toast.LENGTH_SHORT
            ).show();

            /*
             * Send an SMS warning if the newly added
             * item is already at a low inventory level.
             */
            if (quantity <= LOW_INVENTORY_THRESHOLD) {

                sendLowInventorySms(
                        itemName,
                        quantity
                );
            }

            // Clear fields after a successful insert.
            editItemName.setText("");
            editItemQuantity.setText("");

            // Refresh the inventory grid.
            loadInventory();

        } else {

            Toast.makeText(
                    this,
                    "Unable to add inventory item.",
                    Toast.LENGTH_SHORT
            ).show();
        }
    }

    /**
     * Reads all inventory records from SQLite and displays
     * them in the inventory grid.
     */
    private void loadInventory() {

        /*
         * Keep row 0 because it contains the column headers.
         * Remove all dynamically created inventory rows.
         */
        while (tableInventory.getChildCount() > 1) {
            tableInventory.removeViewAt(1);
        }

        Cursor cursor = databaseHelper.getAllItems();

        boolean lowInventoryFound = false;
        StringBuilder lowInventoryMessage = new StringBuilder();

        if (cursor.moveToFirst()) {

            do {

                int id = cursor.getInt(
                        cursor.getColumnIndexOrThrow("id")
                );

                String name = cursor.getString(
                        cursor.getColumnIndexOrThrow("name")
                );

                int quantity = cursor.getInt(
                        cursor.getColumnIndexOrThrow("quantity")
                );

                // Add this database record to the visible table.
                addInventoryRow(
                        id,
                        name,
                        quantity
                );

                /*
                 * Build the visible warning message for
                 * inventory items with five or fewer remaining.
                 */
                if (quantity <= LOW_INVENTORY_THRESHOLD) {

                    if (!lowInventoryFound) {

                        lowInventoryMessage.append(
                                "Low inventory: "
                        );

                    } else {

                        lowInventoryMessage.append(", ");
                    }

                    lowInventoryMessage
                            .append(name)
                            .append(" (")
                            .append(quantity)
                            .append(")");

                    lowInventoryFound = true;
                }

            } while (cursor.moveToNext());
        }

        cursor.close();

        if (lowInventoryFound) {

            textLowInventory.setText(
                    lowInventoryMessage.toString()
            );

        } else {

            textLowInventory.setText(
                    "No low inventory warnings."
            );
        }
    }

    /**
     * Creates one visible row for an inventory record.
     */
    private void addInventoryRow(
            int id,
            String name,
            int quantity) {

        TableRow row = new TableRow(this);
        row.setPadding(8, 8, 8, 8);

        /*
         * Equipment name column.
         */
        TextView nameView = new TextView(this);
        nameView.setText(name);

        TableRow.LayoutParams nameParams =
                new TableRow.LayoutParams(
                        0,
                        TableRow.LayoutParams.WRAP_CONTENT,
                        2f
                );

        nameView.setLayoutParams(nameParams);

        /*
         * Quantity column.
         */
        TextView quantityView = new TextView(this);

        quantityView.setText(
                String.valueOf(quantity)
        );

        quantityView.setGravity(
                Gravity.CENTER
        );

        TableRow.LayoutParams quantityParams =
                new TableRow.LayoutParams(
                        0,
                        TableRow.LayoutParams.WRAP_CONTENT,
                        1f
                );

        quantityView.setLayoutParams(
                quantityParams
        );

        /*
         * Action column containing Edit and Delete.
         */
        LinearLayout actionLayout =
                new LinearLayout(this);

        actionLayout.setOrientation(
                LinearLayout.HORIZONTAL
        );

        actionLayout.setGravity(
                Gravity.CENTER
        );

        TableRow.LayoutParams actionParams =
                new TableRow.LayoutParams(
                        0,
                        TableRow.LayoutParams.WRAP_CONTENT,
                        2f
                );

        actionLayout.setLayoutParams(
                actionParams
        );

        Button editButton = new Button(this);
        editButton.setText("Edit");
        editButton.setTextSize(10);

        Button deleteButton = new Button(this);
        deleteButton.setText("Delete");
        deleteButton.setTextSize(10);

        editButton.setOnClickListener(
                view -> showEditDialog(
                        id,
                        name,
                        quantity
                )
        );

        deleteButton.setOnClickListener(
                view -> confirmDelete(
                        id,
                        name
                )
        );

        actionLayout.addView(editButton);
        actionLayout.addView(deleteButton);

        row.addView(nameView);
        row.addView(quantityView);
        row.addView(actionLayout);

        tableInventory.addView(row);
    }

    /**
     * Displays a dialog that allows an inventory
     * record to be updated.
     */
    private void showEditDialog(
            int id,
            String currentName,
            int currentQuantity) {

        LinearLayout dialogLayout =
                new LinearLayout(this);

        dialogLayout.setOrientation(
                LinearLayout.VERTICAL
        );

        int padding = 40;

        dialogLayout.setPadding(
                padding,
                padding,
                padding,
                padding
        );

        EditText nameInput = new EditText(this);

        nameInput.setHint(
                "Equipment name"
        );

        nameInput.setText(
                currentName
        );

        EditText quantityInput = new EditText(this);

        quantityInput.setHint(
                "Quantity"
        );

        quantityInput.setInputType(
                InputType.TYPE_CLASS_NUMBER
        );

        quantityInput.setText(
                String.valueOf(currentQuantity)
        );

        dialogLayout.addView(nameInput);
        dialogLayout.addView(quantityInput);

        new AlertDialog.Builder(this)

                .setTitle(
                        "Edit Inventory Item"
                )

                .setView(dialogLayout)

                .setPositiveButton(
                        "Save",
                        (dialog, which) -> {

                            String newName = nameInput
                                    .getText()
                                    .toString()
                                    .trim();

                            String newQuantityText =
                                    quantityInput
                                            .getText()
                                            .toString()
                                            .trim();

                            if (newName.isEmpty() ||
                                    newQuantityText.isEmpty()) {

                                Toast.makeText(
                                        this,
                                        "Item was not updated. All fields are required.",
                                        Toast.LENGTH_SHORT
                                ).show();

                                return;
                            }

                            try {

                                int newQuantity =
                                        Integer.parseInt(
                                                newQuantityText
                                        );

                                boolean updated =
                                        databaseHelper.updateItem(
                                                id,
                                                newName,
                                                newQuantity
                                        );

                                if (updated) {

                                    Toast.makeText(
                                            this,
                                            "Inventory item updated.",
                                            Toast.LENGTH_SHORT
                                    ).show();

                                    /*
                                     * Send an SMS warning if the
                                     * updated quantity is low.
                                     */
                                    if (newQuantity <=
                                            LOW_INVENTORY_THRESHOLD) {

                                        sendLowInventorySms(
                                                newName,
                                                newQuantity
                                        );
                                    }

                                    loadInventory();

                                } else {

                                    Toast.makeText(
                                            this,
                                            "Unable to update item.",
                                            Toast.LENGTH_SHORT
                                    ).show();
                                }

                            } catch (
                                    NumberFormatException exception) {

                                Toast.makeText(
                                        this,
                                        "Please enter a valid quantity.",
                                        Toast.LENGTH_SHORT
                                ).show();
                            }
                        }
                )

                .setNegativeButton(
                        "Cancel",
                        null
                )

                .show();
    }

    /**
     * Requests confirmation before deleting an
     * inventory record.
     */
    private void confirmDelete(
            int id,
            String itemName) {

        new AlertDialog.Builder(this)

                .setTitle(
                        "Delete Inventory Item"
                )

                .setMessage(
                        "Delete " + itemName + "?"
                )

                .setPositiveButton(
                        "Delete",
                        (dialog, which) -> {

                            boolean deleted =
                                    databaseHelper.deleteItem(id);

                            if (deleted) {

                                Toast.makeText(
                                        this,
                                        "Inventory item deleted.",
                                        Toast.LENGTH_SHORT
                                ).show();

                                loadInventory();

                            } else {

                                Toast.makeText(
                                        this,
                                        "Unable to delete item.",
                                        Toast.LENGTH_SHORT
                                ).show();
                            }
                        }
                )

                .setNegativeButton(
                        "Cancel",
                        null
                )

                .show();
    }

    /**
     * Sends an SMS alert when an item's quantity reaches
     * the low-inventory threshold.
     *
     * The method only sends a message when:
     * 1. The user enabled SMS notifications.
     * 2. A phone number has been saved.
     * 3. Android granted SEND_SMS permission.
     */
    private void sendLowInventorySms(
            String itemName,
            int quantity) {

        SharedPreferences sharedPreferences =
                getSharedPreferences(
                        PREFS_NAME,
                        Context.MODE_PRIVATE
                );

        boolean smsEnabled =
                sharedPreferences.getBoolean(
                        SMS_ENABLED_KEY,
                        false
                );

        String phoneNumber =
                sharedPreferences.getString(
                        PHONE_NUMBER_KEY,
                        ""
                );

        /*
         * If SMS is disabled or no phone number exists,
         * continue using the application without SMS.
         */
        if (!smsEnabled ||
                phoneNumber == null ||
                phoneNumber.isEmpty()) {

            return;
        }

        /*
         * Never attempt to send SMS without Android's
         * SEND_SMS permission.
         */
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.SEND_SMS
        ) != PackageManager.PERMISSION_GRANTED) {

            return;
        }

        String message =
                "Inventory Alert: " +
                        itemName +
                        " is low. Current quantity: " +
                        quantity +
                        ".";

        try {

            SmsManager smsManager =
                    SmsManager.getDefault();

            smsManager.sendTextMessage(
                    phoneNumber,
                    null,
                    message,
                    null,
                    null
            );

            Toast.makeText(
                    this,
                    "Low inventory SMS alert sent.",
                    Toast.LENGTH_SHORT
            ).show();

        } catch (Exception exception) {

            /*
             * SMS failure must not interfere with normal
             * inventory functionality.
             */
            Toast.makeText(
                    this,
                    "Unable to send SMS alert.",
                    Toast.LENGTH_SHORT
            ).show();
        }
    }

    /**
     * Refreshes the inventory whenever the user returns
     * to this activity.
     */
    @Override
    protected void onResume() {
        super.onResume();

        if (databaseHelper != null &&
                tableInventory != null) {

            loadInventory();
        }
    }
}