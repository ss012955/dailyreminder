package com.example.dailyreminder;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class ReminderDatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "reminders.db";
    private static final int DATABASE_VERSION = 1;

    public static final String TABLE_NAME = "reminders";
    public static final String COLUMN_ID = "_id";
    public static final String COLUMN_TITLE = "title";
    public static final String COLUMN_DESCRIPTION = "description";
    public static final String COLUMN_TIME = "time"; // Store reminder time
    public static final String COLUMN_COMPLETED = "completed";

    public ReminderDatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String createTable = "CREATE TABLE " + TABLE_NAME + " (" +
                COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COLUMN_TITLE + " TEXT, " +
                COLUMN_DESCRIPTION + " TEXT, " +
                COLUMN_TIME + " TEXT, " +
                COLUMN_COMPLETED + " INTEGER DEFAULT 0)";
        db.execSQL(createTable);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        onCreate(db);
    }

    // CRUD Operations
    public long addReminder(String title, String description, String time) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_TITLE, title);
        values.put(COLUMN_DESCRIPTION, description);
        values.put(COLUMN_TIME, time);
        return db.insert(TABLE_NAME, null, values);
    }
    public Reminder updateReminder(int id, String title, String description, String time) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_TITLE, title);
        values.put(COLUMN_DESCRIPTION, description);
        values.put(COLUMN_TIME, time);

        // Update the reminder with the specified ID
        int rowsAffected = db.update(TABLE_NAME, values, COLUMN_ID + " = ?", new String[]{String.valueOf(id)});

        if (rowsAffected > 0) {
            // Return the updated reminder object if the update was successful
            return new Reminder(id, title, description, time);
        } else {
            return null;
        }
    }
    public int deleteReminder(int id) {
        SQLiteDatabase db = this.getWritableDatabase();
        return db.delete(TABLE_NAME, COLUMN_ID + "=?", new String[]{String.valueOf(id)});
    }

    public Cursor getAllReminders() {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.query(TABLE_NAME, null, null, null, null, null, null);
    }


}