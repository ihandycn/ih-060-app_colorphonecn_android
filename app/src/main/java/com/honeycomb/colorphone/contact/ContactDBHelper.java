package com.honeycomb.colorphone.contact;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class ContactDBHelper extends SQLiteOpenHelper {
    public final static int DATABASE_VERSION = 1;
    public final static String DATABASE_NAME = "contact_call_themes.db";

    public ContactDBHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    private static final String SQL_DELETE_ENTRIES =
            "DROP TABLE IF EXISTS " + ThemeEntry.TABLE_NAME;

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.beginTransaction();
        try {
            db.execSQL("CREATE TABLE IF NOT EXISTS "
                    + ThemeEntry.TABLE_NAME
                    + String.format(
                    "("
                            + "%s INTEGER PRIMARY KEY, " // id, com.honeycomb.colorphone.download id
                            + "%s VARCHAR, " // name
                            + "%s VARCHAR, " // number
                            + "%s VARCHAR, " // photo uri
                            + "%s INTEGER " // Theme id
                            + ")"
                    , ThemeEntry._ID
                    , ThemeEntry.NAME
                    , ThemeEntry.NUMBER
                    , ThemeEntry.PHOTO_URI
                    , ThemeEntry.THEME_ID

            ));
            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }

    }


    private void update(ThemeEntry entry) {

    }

    private void delete(ThemeEntry entry) {

    }

    private void insert(ThemeEntry entry) {

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL(SQL_DELETE_ENTRIES);
        onCreate(db);
    }


    public enum Action {
        INSERT,
        UPDATE,
        DELETE
    }

}