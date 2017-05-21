package com.hgsil.android.budejie2;

import android.content.ContentValues;
import android.database.Cursor;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import java.util.ArrayList;

/**
 * Created by Administrator on 2017/5/20 0020.
 */

public class WatchActivity extends AppCompatActivity {
    RecyclerView mRecyclerView ;
    WatchAdapter mWatchAdapter;
    ArrayList<News> mNewses  = new ArrayList<>();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_watch);
        mRecyclerView = (RecyclerView) findViewById(R.id.watch_recycler);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(layoutManager);
        getDataFromDataBase();
        mWatchAdapter = new WatchAdapter(mNewses,this);
        mRecyclerView.setAdapter(mWatchAdapter);
    }
    public void getDataFromDataBase(){
        Cursor cursor = MyDatabaseHelper.DataBaseBuilder.getDataBaseHelper(this).query("Watch",null,null,null,null,null,null);
        if (cursor.moveToFirst()){
            do {
                News oneNew = new News();
                oneNew.setId(cursor.getString(cursor.getColumnIndex("id")));
                oneNew.setVideo_uri(cursor.getString(cursor.getColumnIndex("videoUrl")));
                oneNew.setName(cursor.getString(cursor.getColumnIndex("username")));
                oneNew.setProfile_image(cursor.getString(cursor.getColumnIndex("avatar")));
                oneNew.setWatchTime(cursor.getString(cursor.getColumnIndex("watchTime")));
                oneNew.setText(cursor.getString(cursor.getColumnIndex("text")));
                mNewses.add(oneNew);
            }while (cursor.moveToNext());
        }
        cursor.close();
    }
}
