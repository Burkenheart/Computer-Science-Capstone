package com.example.stevenburgettinventoryapp;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "inventory.db";
    private static final int DATABASE_VERSION = 1;

    // Users table
    private static final String TABLE_USERS = "users";
    private static final String USER_ID = "id";
    private static final String USER_USERNAME = "username";
    private static final String USER_PASSWORD = "password";

    // Inventory table
    private static final String TABLE_INVENTORY = "inventory";
    private static final String ITEM_ID = "id";
    private static final String ITEM_NAME = "name";
    private static final String ITEM_QUANTITY = "quantity";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {

        String createUsersTable =
                "CREATE TABLE " + TABLE_USERS + " (" +
                        USER_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        USER_USERNAME + " TEXT UNIQUE NOT NULL, " +
                        USER_PASSWORD + " TEXT NOT NULL)";

        String createInventoryTable =
                "CREATE TABLE " + TABLE_INVENTORY + " (" +
                        ITEM_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        ITEM_NAME + " TEXT NOT NULL, " +
                        ITEM_QUANTITY + " INTEGER NOT NULL)";

        db.execSQL(createUsersTable);
        db.execSQL(createInventoryTable);
    }

    @Override
    public void onUpgrade(
            SQLiteDatabase db,
            int oldVersion,
            int newVersion) {

        db.execSQL("DROP TABLE IF EXISTS " + TABLE_USERS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_INVENTORY);

        onCreate(db);
    }

    // Creates a new user account.
    public boolean addUser(String username, String password) {

        SQLiteDatabase db = getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(USER_USERNAME, username);
        values.put(USER_PASSWORD, password);

        long result = db.insert(
                TABLE_USERS,
                null,
                values
        );

        return result != -1;
    }

    // Checks whether the supplied username and password exist.
    public boolean checkUser(String username, String password) {

        SQLiteDatabase db = getReadableDatabase();

        String selection =
                USER_USERNAME + " = ? AND " +
                        USER_PASSWORD + " = ?";

        String[] selectionArgs = {
                username,
                password
        };

        Cursor cursor = db.query(
                TABLE_USERS,
                new String[]{USER_ID},
                selection,
                selectionArgs,
                null,
                null,
                null
        );

        boolean userExists = cursor.getCount() > 0;

        cursor.close();

        return userExists;
    }

    // Adds a new inventory item.
    public boolean addItem(String name, int quantity) {

        SQLiteDatabase db = getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(ITEM_NAME, name);
        values.put(ITEM_QUANTITY, quantity);

        long result = db.insert(
                TABLE_INVENTORY,
                null,
                values
        );

        return result != -1;
    }

    // Returns all inventory records.
    public Cursor getAllItems() {

        SQLiteDatabase db = getReadableDatabase();

        return db.query(
                TABLE_INVENTORY,
                null,
                null,
                null,
                null,
                null,
                ITEM_NAME + " ASC"
        );
    }

    // Updates an existing inventory record.
    public boolean updateItem(
            int id,
            String name,
            int quantity) {

        SQLiteDatabase db = getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(ITEM_NAME, name);
        values.put(ITEM_QUANTITY, quantity);

        int rowsUpdated = db.update(
                TABLE_INVENTORY,
                values,
                ITEM_ID + " = ?",
                new String[]{
                        String.valueOf(id)
                }
        );

        return rowsUpdated > 0;
    }

    // Deletes an inventory record.
    public boolean deleteItem(int id) {

        SQLiteDatabase db = getWritableDatabase();

        int rowsDeleted = db.delete(
                TABLE_INVENTORY,
                ITEM_ID + " = ?",
                new String[]{
                        String.valueOf(id)
                }
        );

        return rowsDeleted > 0;
    }
}