package com.hgsil.android.budejie2;

import android.content.Context;
import android.database.DatabaseErrorHandler;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by Administrator on 2017/5/21 0021.
 */

public class MyDatabaseHelper extends SQLiteOpenHelper {
    public static final String CREATE_WATCH = "create table Watch("
            + "id integer primary key ,"
            + "videoUrl varchar(50),"
            + "text varchar(150),"
            + "username varchar(20),"
            + "avatar varchar(50),"
            + "watchTime varchar(20) not null)";
    private MyDatabaseHelper(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);
    }

    private MyDatabaseHelper(Context context, String name, SQLiteDatabase.CursorFactory factory, int version, DatabaseErrorHandler errorHandler) {
        super(context, name, factory, version, errorHandler);
    }

    static class DataBaseBuilder{

        private static SQLiteDatabase mySQLDateBase ;
        public static SQLiteDatabase getDataBaseHelper(Context context){
            MyDatabaseHelper mMyDatabaseHelper = new MyDatabaseHelper(context,"Watch.tb",null,1);
            mySQLDateBase =  mMyDatabaseHelper.getWritableDatabase();
            return mySQLDateBase;
        }
    }
    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        sqLiteDatabase.execSQL(CREATE_WATCH);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {

    }
}
