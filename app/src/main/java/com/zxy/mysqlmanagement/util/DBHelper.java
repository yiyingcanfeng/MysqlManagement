package com.zxy.mysqlmanagement.util;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DBHelper extends SQLiteOpenHelper {
    public DBHelper(Context context,  int version) {
        super(context, "main.db", null, version);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String sql = "create table connection(" +
                "_id integer primary key autoincrement," +
                "connectionName varchar," +
                "host varchar," +
                "port varchar," +
                "DBName varchar," +
                "user varchar," +
                "password varchar)";
        db.execSQL(sql);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}
