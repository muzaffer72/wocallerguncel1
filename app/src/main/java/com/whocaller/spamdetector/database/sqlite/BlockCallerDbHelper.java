/*
 * Company : AndroPlaza
 * Detailed : Software Development Company in Sri Lanka
 * Developer : Buddhika
 * Contact : support@androplaza.shop
 * Whatsapp : +94711920144
 */

package com.whocaller.spamdetector.database.sqlite;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.BaseColumns;

import com.whocaller.spamdetector.modal.Contact;

import java.util.ArrayList;
import java.util.List;

public class BlockCallerDbHelper extends SQLiteOpenHelper {

    // Database version and name
    private static final int DATABASE_VERSION = 1;
    private static final String DATABASE_NAME = "BlockCaller.db";

    // Table and column names
    private static final String TABLE_NAME = "block_caller";
    private static final String COLUMN_NAME_ID = BaseColumns._ID;
    private static final String COLUMN_NAME_NAME = "name";
    private static final String COLUMN_NAME_PHONE_NUMBER = "phone_number";

    // SQL statement to create the table
    private static final String SQL_CREATE_ENTRIES =
            "CREATE TABLE " + TABLE_NAME + " (" +
                    COLUMN_NAME_ID + " INTEGER PRIMARY KEY," +
                    COLUMN_NAME_NAME + " TEXT," +
                    COLUMN_NAME_PHONE_NUMBER + " TEXT)";

    // SQL statement to delete the table
    private static final String SQL_DELETE_ENTRIES =
            "DROP TABLE IF EXISTS " + TABLE_NAME;

    public BlockCallerDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(SQL_CREATE_ENTRIES);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL(SQL_DELETE_ENTRIES);
        onCreate(db);
    }

    public boolean addBlockCaller(String name, String phoneNumber) {
        if (isPhoneNumberBlocked(phoneNumber)) {
            return false;
        }

        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(COLUMN_NAME_NAME, name);
        values.put(COLUMN_NAME_PHONE_NUMBER, phoneNumber);

        long newRowId = db.insert(TABLE_NAME, null, values);
        return newRowId != -1;
    }

    public boolean isPhoneNumberBlocked(String phoneNumber) {
        SQLiteDatabase db = this.getReadableDatabase();

        String[] projection = {COLUMN_NAME_PHONE_NUMBER};
        String selection = COLUMN_NAME_PHONE_NUMBER + " = ?";
        String[] selectionArgs = {phoneNumber};

        Cursor cursor = db.query(
                TABLE_NAME,
                projection,
                selection,
                selectionArgs,
                null,
                null,
                null
        );

        boolean exists = cursor.getCount() > 0;
        cursor.close();
        return exists;
    }

    @SuppressLint("Range")
    public List<Contact> getAllBlockCallers() {
        List<Contact> blockCallerList = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        String[] projection = {
                COLUMN_NAME_ID,
                COLUMN_NAME_NAME,
                COLUMN_NAME_PHONE_NUMBER
        };

        try (Cursor cursor = db.query(
                TABLE_NAME,
                projection,
                null,
                null,
                null,
                null,
                null
        )) {
            if (cursor.moveToFirst()) {
                do {
                    Contact blockCaller = new Contact();
                    blockCaller.setName(cursor.getString(cursor.getColumnIndex(COLUMN_NAME_NAME)));
                    blockCaller.setPhoneNumber(cursor.getString(cursor.getColumnIndex(COLUMN_NAME_PHONE_NUMBER)));
                    blockCallerList.add(blockCaller);
                } while (cursor.moveToNext());
            }
        }

        return blockCallerList;
    }

    public boolean deleteBlockCallerByPhoneNumber(String phoneNumber) {
        SQLiteDatabase db = this.getWritableDatabase();

        String selection = COLUMN_NAME_PHONE_NUMBER + " = ?";
        String[] selectionArgs = {phoneNumber};

        int deletedRows = db.delete(TABLE_NAME, selection, selectionArgs);
        return deletedRows > 0;
    }
}
