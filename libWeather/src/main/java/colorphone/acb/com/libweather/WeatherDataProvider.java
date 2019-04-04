package colorphone.acb.com.libweather;

import android.content.ContentProvider;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.SparseArray;

import com.ihs.app.framework.HSApplication;

import colorphone.acb.com.libweather.model.LauncherFiles;

/**
 * {@link ContentProvider} interface for local weather data.
 */
public class WeatherDataProvider extends ContentProvider {

    private static final String TABLE_CITY = "city";
    public static final String COLUMN_ID = "_id";
    public static final String COLUMN_QUERY_ID = "queryId";
    public static final String COLUMN_DISPLAY_NAME = "displayName";
    public static final String COLUMN_WEATHER = "weather";
    public static final String COLUMN_LAST_QUERY_TIME = "lastQueryTime";
    public static final String COLUMN_NEEDS_UPDATE = "needsUpdate";
    public static final String COLUMN_IS_LOCAL = "isLocal";
    public static final String COLUMN_RANK = "rank"; // Added in VERSION 2

    public static final String AUTHORITY = HSApplication.getContext().getPackageName() + ".weather";
    public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/" + TABLE_CITY);

    public static final int CITY_QUERY = 1;

    private WeatherDBHelper mHelper;

    // Defines a helper object that matches content URIs to table-specific parameters
    private static final UriMatcher sUriMatcher = new UriMatcher(0);

    // Stores the MIME types served by this provider
    private static final SparseArray<String> sMimeTypes = new SparseArray<>();

    static {
        // Adds a URI "match" entry that maps picture URL content URIs to a numeric code
        sUriMatcher.addURI(AUTHORITY, TABLE_CITY, CITY_QUERY);
        sMimeTypes.put(CITY_QUERY, "vnd.android.cursor.item/vnd." + AUTHORITY + "." + TABLE_CITY);
    }

    @Override
    public boolean onCreate() {
        mHelper = new WeatherDBHelper(getContext());
        return true;
    }

    @Nullable
    @Override
    public Cursor query(@NonNull Uri uri,
                        String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        SQLiteDatabase db = mHelper.getReadableDatabase();
        // Decodes the content URI and maps it to a code
        switch (sUriMatcher.match(uri)) {
            case CITY_QUERY:
                Cursor returnCursor = db.query(TABLE_CITY, projection, selection, selectionArgs, null, null, sortOrder);

                // Sets the ContentResolver to watch this content URI for data changes
                Context context = getContext();
                if (context != null) returnCursor.setNotificationUri(context.getContentResolver(), uri);
                return returnCursor;
        }
        return null;
    }

    @Nullable
    @Override
    public String getType(@NonNull Uri uri) {
        return sMimeTypes.get(sUriMatcher.match(uri));
    }

    @Nullable
    @Override
    public Uri insert(@NonNull Uri uri, ContentValues values) {
        switch (sUriMatcher.match(uri)) {
            case CITY_QUERY:
                SQLiteDatabase localSQLiteDatabase = mHelper.getWritableDatabase();

                // Inserts the row into the table and returns the new row's _id value
                long id = localSQLiteDatabase.insert(TABLE_CITY, null, values);

                // If the insert succeeded, notify a change and return the new row's content URI.
                if (-1 != id) {
                    Context context = getContext();
                    if (context != null) context.getContentResolver().notifyChange(uri, null);
                    return Uri.withAppendedPath(uri, Long.toString(id));
                } else {
                    throw new SQLiteException("Insert error:" + uri);
                }
        }
        return null;
    }

    @Override
    public int delete(@NonNull Uri uri, String selection, String[] selectionArgs) {
        switch (sUriMatcher.match(uri)) {
            case CITY_QUERY:
                SQLiteDatabase localSQLiteDatabase = mHelper.getWritableDatabase();

                // Updates the table
                int rows = localSQLiteDatabase.delete(TABLE_CITY, selection, selectionArgs);

                // If the update succeeded, notify a change and return the number of updated rows.
                if (0 != rows) {
                    Context context = getContext();
                    if (context != null) context.getContentResolver().notifyChange(uri, null);
                    return rows;
                }
        }
        return 0;
    }

    @Override
    public int update(@NonNull Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        switch (sUriMatcher.match(uri)) {
            case CITY_QUERY:
                SQLiteDatabase localSQLiteDatabase = mHelper.getWritableDatabase();

                // Updates the table
                int rows = localSQLiteDatabase.update(TABLE_CITY, values, selection, selectionArgs);

                // If the update succeeded, notify a change and return the number of updated rows.
                if (0 != rows) {
                    Context context = getContext();
                    if (context != null) context.getContentResolver().notifyChange(uri, null);
                    return rows;
                }
        }
        return -1;
    }

    public static void insertToDatabaseSync(ContentValues values) {
        ContentResolver cr = HSApplication.getContext().getContentResolver();
        Cursor c = cr.query(CONTENT_URI, new String[]{COLUMN_RANK}, null, null, COLUMN_RANK + " DESC LIMIT 1");
        int newRank = 0;
        if (c != null) {
            try {
                if (c.moveToNext()) {
                    int maxRank = c.getInt(0);
                    newRank = maxRank + 1;
                }
            } finally {
                c.close();
            }
        }
        values.put(COLUMN_RANK, newRank);
        cr.insert(CONTENT_URI, values);
    }

    /**
     * Database helper for weather storage.
     */
    public static class WeatherDBHelper extends SQLiteOpenHelper {
        private static final int DATABASE_VERSION = 2;

        private WeatherDBHelper(Context context) {
            super(context, LauncherFiles.WEATHER_DB, null, DATABASE_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            db.execSQL("CREATE TABLE " + TABLE_CITY + " (" +
                    COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    COLUMN_QUERY_ID + " TEXT, " +
                    COLUMN_DISPLAY_NAME + " TEXT, " +
                    COLUMN_WEATHER + " TEXT, " +
                    COLUMN_LAST_QUERY_TIME + " INTEGER NOT NULL DEFAULT -1, " +
                    COLUMN_NEEDS_UPDATE + " INTEGER NOT NULL DEFAULT 0, " +
                    COLUMN_IS_LOCAL + " INTEGER NOT NULL DEFAULT 0, " +
                    COLUMN_RANK + " INTEGER NOT NULL DEFAULT -1" +
                    ");");
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            if (oldVersion <= 1 && newVersion >= 2) {
                String addLocalRankQuery = "UPDATE " + TABLE_CITY + " SET " + COLUMN_RANK + " = ?"
                        + " WHERE " + COLUMN_IS_LOCAL + " = ?;";
                db.execSQL("ALTER TABLE " + TABLE_CITY + " ADD COLUMN " + COLUMN_RANK + " INTEGER DEFAULT -1");
                db.execSQL(addLocalRankQuery, new String[]{"0", "1"});
                Cursor c = db.query(TABLE_CITY, new String[]{COLUMN_ID}, COLUMN_IS_LOCAL + " = ?", new String[]{"0"},
                        null, null, null);
                if (c == null) {
                    return;
                }
                int rank = 1;
                try {
                    while (c.moveToNext()) {
                        ContentValues values = new ContentValues();
                        values.put(COLUMN_RANK, rank++);
                        db.update(TABLE_CITY, values, COLUMN_ID + " = ?", new String[]{String.valueOf(c.getInt(0))});
                    }
                } finally {
                    c.close();
                }
            }
        }
    }
}
