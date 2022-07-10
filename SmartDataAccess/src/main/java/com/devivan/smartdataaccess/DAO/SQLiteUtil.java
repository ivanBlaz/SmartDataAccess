package com.devivan.smartdataaccess.DAO;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.devivan.smartdataaccess.Beans.SmartBean;

import java.util.ArrayList;
import java.util.List;

public class SQLiteUtil {

    ////////////////
    // CONNECTION //
    ////////////////
    private static final String DB_NAME = "smartDB";
    private static final int DB_VERSION = 1;
    public static SQLiteConnection connection;

    public static void connect(Context context) {
        if (connection == null) connection = new SQLiteConnection(context, DB_NAME, null, DB_VERSION);
    }

    public static void disconnect() {
        if (connection != null) {
            connection.close();
            connection = null;
        }
    }

    @SuppressLint("Recycle")
    public static String getValue(String key) {
        Cursor c = read().query(T_SMART.trim(), new String[]{F_VALUE.trim()}, F_KEY.trim() + " = ?", new String[]{key}, F_KEY.trim(), F_ID.trim() + " = MIN(" + F_ID.trim() + ")", null);
        if (c.moveToFirst()) return c.getString(0);
        else return null;
    }

    public static List<SmartBean> geValues(String key) {
        Cursor c = read().query(T_SMART.trim(), new String[]{F_ID.trim(), F_KEY.trim(), F_VALUE.trim()}, F_KEY.trim() + " = ? AND " + NOT_FIRST_KEY, new String[]{key, key}, null, null, F_ID.trim() + " DESC");
        List<SmartBean> smartBeans = new ArrayList<>();
        while (c.moveToNext()) { smartBeans.add(new SmartBean(c.getInt(0), c.getString(1), c.getString(2))); } c.close();
        return smartBeans;
    }

    public static List<SmartBean> geValues(String key, String value) {
        Cursor c = read().query(T_SMART.trim(), new String[]{F_ID.trim(), F_KEY.trim(), F_VALUE.trim()}, F_KEY.trim() + " = ? AND " + F_VALUE + " LIKE '%" + value + "%' AND " + NOT_FIRST_KEY, new String[]{key, key}, null, null, F_ID.trim() + " DESC");
        List<SmartBean> smartBeans = new ArrayList<>();
        while (c.moveToNext()) { smartBeans.add(new SmartBean(c.getInt(0), c.getString(1), c.getString(2))); } c.close();
        return smartBeans;
    }


    public static void addValue(String key, String value) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(F_KEY.trim(), key);
        contentValues.put(F_VALUE.trim(), value);
        write().insert(T_SMART.trim(), null, contentValues);
    }

    public static void setValue(String key, String value) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(F_VALUE.trim(), value);

        if (null == getValue(key)) {
            contentValues.put(F_KEY.trim(), key);
            write().insert(T_SMART.trim(), null, contentValues);
        } else {
            write().update(T_SMART.trim(), contentValues, F_KEY.trim() + " = ? AND " + FIRST_KEY, new String[] {key, key});
        }
    }

    public static void setValue(int id, String value) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(F_VALUE.trim(), value);
        write().update(T_SMART.trim(), contentValues, F_ID.trim() + " = ?", new String[] {String.valueOf(id)});
    }

    public static void delValue(int id) {
        write().delete(T_SMART.trim(), F_ID.trim() + " = ?", new String[] {String.valueOf(id)});
    }

    private static SQLiteDatabase write() {
        return connection.getWritableDatabase();
    }

    private static SQLiteDatabase read() {
        return connection.getReadableDatabase();
    }

    ////////////
    // TABLES //
    ////////////
    public static final String T_SMART = " smart ";

    ////////////
    // FIELDS //
    ////////////
    public static final String F_ID = " id ";
    public static final String F_ID_TYPE = " INTEGER PRIMARY KEY AUTOINCREMENT ";

    public static final String F_KEY = " key ";
    public static final String F_KEY_TYPE = " TEXT ";

    public static final String F_VALUE = " value ";
    public static final String F_VALUE_TYPE = " TEXT ";

    public static final String T_SMART_FIELDS = F_ID + F_ID_TYPE + "," +
                                                F_KEY + F_KEY_TYPE + "," +
                                                F_VALUE + F_VALUE_TYPE;

    ////////////
    // CREATE //
    ////////////
    public static final String C_T_SMART = "CREATE TABLE" + T_SMART + "(" + T_SMART_FIELDS + ")";

    //////////
    // DROP //
    //////////
    public static final String D_T_SMART = "DROP TABLE" + T_SMART;


    ///////////
    // WHERE //
    ///////////
    private static final String FIRST_KEY = F_ID.trim() + " = (SELECT MIN(" + F_ID.trim() + ") FROM " + T_SMART.trim() + " WHERE " + F_KEY.trim() + " = ?)";
    private static final String NOT_FIRST_KEY = F_ID.trim() + " != (SELECT MIN(" + F_ID.trim() + ") FROM " + T_SMART.trim() + " WHERE " + F_KEY.trim() + " = ?)";
}
