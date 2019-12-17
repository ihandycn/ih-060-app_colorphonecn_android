package com.honeycomb.colorphone.util;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;

/* compiled from: DBUtils */
public class o {

    /* renamed from: a  reason: collision with root package name */
    private static final a f12139a = new a() {
        public boolean a(String str, int i, Object obj, ContentValues contentValues) {
            return false;
        }
    };

    /* compiled from: DBUtils */
    public interface a {
        boolean a(String str, int i, Object obj, ContentValues contentValues);
    }

    public static String a(Uri uri) {
        long j;
        String str;
        if (uri == null) {
            return "";
        }
        try {
            j = ContentUris.parseId(uri);
        } catch (Exception unused) {
            j = -1;
        }
        if (j == -1) {
            str = "";
        } else {
            str = String.valueOf(j);
        }
        return str;
    }

    public static void a(ContentResolver contentResolver, Uri uri, ContentValues contentValues) {
        a(contentResolver, uri, contentValues, f12139a);
    }

    public static void a(ContentResolver contentResolver, Uri uri, ContentValues contentValues, a aVar) {
        if (contentResolver != null && uri != null && contentValues != null) {
            Cursor query = contentResolver.query(uri, null, null, null, null);
            if (query != null) {
                if (query.moveToFirst()) {
                    String[] columnNames = query.getColumnNames();
                    for (int i = 0; i < columnNames.length; i++) {
                        String str = columnNames[i];
                        if (!query.isNull(i)) {
                            switch (query.getType(i)) {
                                case 1:
                                    int i2 = query.getInt(i);
                                    if (aVar != null && aVar.a(str, 1, Integer.valueOf(i2), contentValues)) {
                                        break;
                                    } else {
                                        contentValues.put(str, Integer.valueOf(i2));
                                        break;
                                    }
                                case 2:
                                    float f = query.getFloat(i);
                                    if (aVar != null && aVar.a(str, 2, Float.valueOf(f), contentValues)) {
                                        break;
                                    } else {
                                        contentValues.put(str, Float.valueOf(f));
                                        break;
                                    }
                                case 3:
                                    String string = query.getString(i);
                                    if (aVar != null && aVar.a(str, 3, string, contentValues)) {
                                        break;
                                    } else {
                                        contentValues.put(str, string);
                                        break;
                                    }
                                case 4:
                                    byte[] blob = query.getBlob(i);
                                    if (aVar != null && aVar.a(str, 4, blob, contentValues)) {
                                        break;
                                    } else {
                                        contentValues.put(str, blob);
                                        break;
                                    }
                            }
                        }
                    }
                }
                a(query);
            }
        }
    }

    public static void a(Cursor cursor) {
        if (cursor != null) {
            try {
                cursor.close();
            } catch (Exception unused) {
            }
        }
    }
}