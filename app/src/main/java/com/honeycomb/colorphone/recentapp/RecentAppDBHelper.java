package com.honeycomb.colorphone.recentapp;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.honeycomb.colorphone.contact.ThemeEntry;

public class RecentAppDBHelper extends SQLiteOpenHelper {
    public final static int DATABASE_VERSION = 1;
    public final static String DATABASE_NAME = "recent_app.db";

    public RecentAppDBHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    private static final String SQL_DELETE_ENTRIES =
            "DROP TABLE IF EXISTS " + AppUsage.AppUsageEntry.TABLE_NAME;

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.beginTransaction();
        try {
            db.execSQL("CREATE TABLE IF NOT EXISTS "
                    + AppUsage.AppUsageEntry.TABLE_NAME
                    + String.format(
                    "("
                            + "%s INTEGER PRIMARY KEY, " // id
                            + "%s INTEGER, "
                            + "%s VARCHAR "
                            + ")"
                    , AppUsage.AppUsageEntry._ID
                    , AppUsage.AppUsageEntry.COLUMN_NAME_LAUNCHTIME_LAST
                    , AppUsage.AppUsageEntry.COLUMN_NAME_PACKAGE_NAME

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

}