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

import com.whocaller.spamdetector.modal.Contact;

import java.util.ArrayList;
import java.util.List;

public class ContactsDataDb extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "whocaller.db";
    private static final int DATABASE_VERSION = 1;

    // Contacts table
    private static final String TABLE_NAME = "contacts";
    private static final String COLUMN_ID = "id";
    private static final String COLUMN_NAME = "name";
    private static final String COLUMN_IS_WHO = "is_who";
    private static final String COLUMN_IS_SPAM = "is_spam";
    private static final String COLUMN_PHONE_NUMBER = "phone_number";
    private static final String COLUMN_SPAM_TYPE = "spam_type";
    private static final String COLUMN_TAGS = "tags";
    private static final String COLUMN_CARRIER_NAME = "carrier_name";
    private static final String COLUMN_COUNTRY_NAME = "country_name";
    private static final String COLUMN_CONTACT_BY = "contacts_By";


    private static final String SQL_DELETE_ENTRIES =
            "DROP TABLE IF EXISTS " + TABLE_NAME;

    public ContactsDataDb(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String createTable = "CREATE TABLE " + TABLE_NAME + " (" +
                COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COLUMN_NAME + " TEXT, " +
                COLUMN_IS_WHO + " INTEGER, " +
                COLUMN_IS_SPAM + " INTEGER, " +
                COLUMN_PHONE_NUMBER + " TEXT UNIQUE, " +
                COLUMN_SPAM_TYPE + " TEXT, " +
                COLUMN_TAGS + " TEXT, " +
                COLUMN_CARRIER_NAME + " TEXT, " +
                COLUMN_COUNTRY_NAME + " TEXT, " +
                COLUMN_CONTACT_BY + " TEXT)";
        db.execSQL(createTable);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL(SQL_DELETE_ENTRIES);
        onCreate(db);
    }


    @SuppressLint("Range")
    public void addContactOrUpdate(String name, String phoneNumber, boolean isWho, boolean isSpam, String spamType, String tags, String carrierName, String countryName, String contactsBy) {
        long result;

        try (SQLiteDatabase db = this.getWritableDatabase(); Cursor cursor = db.query(TABLE_NAME, new String[]{COLUMN_ID},
                COLUMN_PHONE_NUMBER + " = ?", new String[]{phoneNumber},
                null, null, null)) {

            ContentValues values = new ContentValues();
            values.put(COLUMN_NAME, name);
            values.put(COLUMN_IS_WHO, isWho ? 1 : 0);
            values.put(COLUMN_IS_SPAM, isSpam ? 1 : 0);
            values.put(COLUMN_SPAM_TYPE, spamType);
            values.put(COLUMN_TAGS, tags);
            values.put(COLUMN_CARRIER_NAME, carrierName);
            values.put(COLUMN_COUNTRY_NAME, countryName);
            values.put(COLUMN_CONTACT_BY, contactsBy);

            if (cursor != null && cursor.moveToFirst()) {
                int rowsAffected = db.update(TABLE_NAME, values,
                        COLUMN_PHONE_NUMBER + " = ?", new String[]{phoneNumber});
                result = rowsAffected > 0 ? cursor.getLong(cursor.getColumnIndex(COLUMN_ID)) : -1;
            } else {
                values.put(COLUMN_PHONE_NUMBER, phoneNumber);
                result = db.insert(TABLE_NAME, null, values);
            }
        }

    }


    @SuppressLint("Range")
    public long addContactOrUpdate(Contact contact) {
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = null;
        long result;

        try {
            cursor = db.query(TABLE_NAME, new String[]{COLUMN_ID},
                    COLUMN_PHONE_NUMBER + " = ?", new String[]{contact.getPhoneNumber()},
                    null, null, null);

            ContentValues values = new ContentValues();
            values.put(COLUMN_NAME, contact.getName());
            values.put(COLUMN_IS_WHO, contact.isWho() ? 1 : 0);
            values.put(COLUMN_IS_SPAM, contact.isSpam() ? 1 : 0);
            values.put(COLUMN_SPAM_TYPE, contact.getSpamType());
            values.put(COLUMN_TAGS, contact.getTag());
            values.put(COLUMN_CARRIER_NAME, contact.getCarrierName());
            values.put(COLUMN_COUNTRY_NAME, contact.getCountryName());
            values.put(COLUMN_CONTACT_BY, contact.getContactsBy());

            if (cursor != null && cursor.moveToFirst()) {
                int rowsAffected = db.update(TABLE_NAME, values,
                        COLUMN_PHONE_NUMBER + " = ?", new String[]{contact.getPhoneNumber()});
                result = rowsAffected > 0 ? cursor.getLong(cursor.getColumnIndex(COLUMN_ID)) : -1;
            } else {
                values.put(COLUMN_PHONE_NUMBER, contact.getPhoneNumber());
                result = db.insert(TABLE_NAME, null, values);
            }
        } finally {
            if (cursor != null) {
                cursor.close();
            }
            db.close();
        }

        return result;
    }

    @SuppressLint("Range")
    public List<Contact> getAllContacts() {
        List<Contact> contactList = new ArrayList<>();

        try (SQLiteDatabase db = this.getReadableDatabase();
             Cursor cursor = db.query(TABLE_NAME,
                     new String[]{COLUMN_ID, COLUMN_NAME, COLUMN_PHONE_NUMBER, COLUMN_IS_WHO, COLUMN_IS_SPAM, COLUMN_SPAM_TYPE, COLUMN_TAGS, COLUMN_CARRIER_NAME, COLUMN_COUNTRY_NAME, COLUMN_CONTACT_BY},
                     COLUMN_CONTACT_BY + " = ?", new String[]{"whocaller"}, null, null, null)) {

            if (cursor != null && cursor.moveToFirst()) {
                do {
                    Contact contact = new Contact();
                    contact.setName(cursor.getString(cursor.getColumnIndex(COLUMN_NAME)));
                    contact.setPhoneNumber(cursor.getString(cursor.getColumnIndex(COLUMN_PHONE_NUMBER)));
                    contact.setIsWho(cursor.getInt(cursor.getColumnIndex(COLUMN_IS_WHO)) == 1);
                    contact.setIsSpam(cursor.getInt(cursor.getColumnIndex(COLUMN_IS_SPAM)) == 1);
                    contact.setSpamType(cursor.getString(cursor.getColumnIndex(COLUMN_SPAM_TYPE)));
                    contact.setTag(cursor.getString(cursor.getColumnIndex(COLUMN_TAGS)));
                    contact.setCarrierName(cursor.getString(cursor.getColumnIndex(COLUMN_CARRIER_NAME)));
                    contact.setCountryName(cursor.getString(cursor.getColumnIndex(COLUMN_COUNTRY_NAME)));
                    contact.setContactsBy(cursor.getString(cursor.getColumnIndex(COLUMN_CONTACT_BY))); // New column

                    contactList.add(contact);
                } while (cursor.moveToNext());
            }
        }

        return contactList;
    }

    @SuppressLint("Range")
    public List<Contact> getSpamContacts() {
        List<Contact> spamContacts = new ArrayList<>();

        try (SQLiteDatabase db = this.getReadableDatabase();
             Cursor cursor = db.query(TABLE_NAME,
                     new String[]{COLUMN_ID, COLUMN_NAME, COLUMN_PHONE_NUMBER, COLUMN_IS_WHO, COLUMN_IS_SPAM, COLUMN_SPAM_TYPE, COLUMN_TAGS, COLUMN_CARRIER_NAME, COLUMN_COUNTRY_NAME, COLUMN_CONTACT_BY},
                     COLUMN_IS_SPAM + " = ?", new String[]{"1"},
                     null, null, null)) {

            if (cursor != null && cursor.moveToFirst()) {
                do {
                    Contact contact = new Contact();
                    contact.setName(cursor.getString(cursor.getColumnIndex(COLUMN_NAME)));
                    contact.setPhoneNumber(cursor.getString(cursor.getColumnIndex(COLUMN_PHONE_NUMBER)));
                    contact.setIsWho(cursor.getInt(cursor.getColumnIndex(COLUMN_IS_WHO)) == 1);
                    contact.setIsSpam(cursor.getInt(cursor.getColumnIndex(COLUMN_IS_SPAM)) == 1);
                    contact.setSpamType(cursor.getString(cursor.getColumnIndex(COLUMN_SPAM_TYPE)));
                    contact.setTag(cursor.getString(cursor.getColumnIndex(COLUMN_TAGS)));
                    contact.setCarrierName(cursor.getString(cursor.getColumnIndex(COLUMN_CARRIER_NAME)));
                    contact.setCountryName(cursor.getString(cursor.getColumnIndex(COLUMN_COUNTRY_NAME)));
                    contact.setCountryName(cursor.getString(cursor.getColumnIndex(COLUMN_CONTACT_BY)));
                    spamContacts.add(contact);
                } while (cursor.moveToNext());
            }
        }

        return spamContacts;
    }

    @SuppressLint("Range")
    public Contact getContactByPhoneNumber(String phoneNumber) {
        if (phoneNumber == null) {
            throw new IllegalArgumentException("phoneNumber cannot be null");
        }

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = null;
        Contact contact = null;

        String[] phoneFormats = {
                phoneNumber, formatPhoneNumber(phoneNumber)
        };

        try {
            for (String number : phoneFormats) {
                if (number != null) {
                    cursor = db.query(TABLE_NAME,
                            new String[]{COLUMN_ID, COLUMN_NAME, COLUMN_PHONE_NUMBER, COLUMN_IS_WHO, COLUMN_IS_SPAM, COLUMN_SPAM_TYPE, COLUMN_TAGS, COLUMN_CARRIER_NAME, COLUMN_COUNTRY_NAME, COLUMN_CONTACT_BY},
                            COLUMN_PHONE_NUMBER + " = ?", new String[]{number},
                            null, null, null);

                    if (cursor != null && cursor.moveToFirst()) {
                        contact = new Contact();
                        contact.setName(cursor.getString(cursor.getColumnIndex(COLUMN_NAME)));
                        contact.setPhoneNumber(cursor.getString(cursor.getColumnIndex(COLUMN_PHONE_NUMBER)));
                        contact.setIsWho(cursor.getInt(cursor.getColumnIndex(COLUMN_IS_WHO)) == 1);
                        contact.setIsSpam(cursor.getInt(cursor.getColumnIndex(COLUMN_IS_SPAM)) == 1);
                        contact.setSpamType(cursor.getString(cursor.getColumnIndex(COLUMN_SPAM_TYPE)));
                        contact.setTag(cursor.getString(cursor.getColumnIndex(COLUMN_TAGS)));
                        contact.setCarrierName(cursor.getString(cursor.getColumnIndex(COLUMN_CARRIER_NAME)));
                        contact.setCountryName(cursor.getString(cursor.getColumnIndex(COLUMN_COUNTRY_NAME)));
                        contact.setContactsBy(cursor.getString(cursor.getColumnIndex(COLUMN_CONTACT_BY)));

                        break;
                    }

                    if (cursor != null) {
                        cursor.close();
                    }
                }
            }
        } finally {
            if (cursor != null) {
                cursor.close();
            }
            db.close();
        }

        return contact;
    }

    private String formatPhoneNumber(String phoneNumber) {
//        String nationalFormat = PhoneNumberUtils.toNationalFormat(phoneNumber, "US");
//        if (nationalFormat != null) {
//            return nationalFormat.replace(" ", "");
//        } else {
//            return null;
//        }
        return phoneNumber;
    }

    public boolean deleteContactByPhoneNumber(String phoneNumber) {
        SQLiteDatabase db = this.getWritableDatabase();
        int rowsAffected = db.delete(TABLE_NAME, COLUMN_PHONE_NUMBER + " = ?", new String[]{phoneNumber});
        db.close();
        return rowsAffected > 0;
    }
}
