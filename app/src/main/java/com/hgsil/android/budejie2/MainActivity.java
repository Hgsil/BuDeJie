package com.hgsil.android.budejie2;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.hgsil.android.budejie2.HttpUtils.AsynNetUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private RecyclerView mRecyclerView ;
    private MyAdapter mAdapter;
    private Context mContext = this;
    private boolean notFresh = true;
    private boolean notAdd = true;
    private boolean havePermission =false;
    private List<News> mNewses = new ArrayList<>();
    private int page = 0;
    private ImageView watch;
    private  LinearLayoutManager layoutManager;
    private SwipeRefreshLayout mSwipeRefreshLayout;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initView();
        setListener();
    }
    private void setListener(){
        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                notFresh = false;
                setNewses();
                notFresh = true;
                mSwipeRefreshLayout.setRefreshing(false);
            }
        });

        mRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            int lastVisibleItem;
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                if (newState ==RecyclerView.SCROLL_STATE_IDLE &&
                        lastVisibleItem+ 1 ==mAdapter.getItemCount()){
                    notAdd = false;
                    page = 0;
                    setNewses();
                    notAdd = true;
                }
            }

            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                lastVisibleItem = layoutManager.findLastVisibleItemPosition();
            }
        });

        watch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(MainActivity.this,WatchActivity.class));
            }
        });
    }
    private void initView(){
        watch = (ImageView)findViewById(R.id.watch_image);
        mRecyclerView = (RecyclerView)findViewById(R.id.recycler_mainactivity);
        setNewses();
        layoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(layoutManager);
        mSwipeRefreshLayout = (SwipeRefreshLayout)findViewById(R.id.swipe_main_activity);


    }

    private void setNewses() {
        page++;
        String url = " http://route.showapi.com/255-1?";
        AsynNetUtils.post(url,
                "showapi_appid=38573&" +
                "showapi_sign=44DB125645B95E83289AABBA791C13EF&"+
                "page="+page + "&type=41", new AsynNetUtils.Callback() {
            @Override
            public void onResponse(String response) {
                try {
                    JSONObject responseJsonObject = new JSONObject(response);
                    JSONObject bodyJsonObject = new JSONObject(responseJsonObject.getString("showapi_res_body"));
                    Log.d("MainActivity", responseJsonObject.toString());
                    JSONObject pageObject = new JSONObject(bodyJsonObject.getString("pagebean"));
                    JSONArray contentObject = new JSONArray(pageObject.getString("contentlist"));
                    for (int i = 0; i < contentObject.length(); i++) {
                        setOneNew(contentObject.getJSONObject(i));

                    }
                    if(notFresh && notAdd) {
                        mAdapter = new MyAdapter(mNewses,mContext);
                        mRecyclerView.setAdapter(mAdapter);
                        mNewses.clear();
                    }
                    else if (!notAdd){
                        mAdapter.addItem(mNewses);
                        mNewses.clear();
                    }
                    else if (!notFresh){
                        mAdapter.refresh(mNewses);
                        mNewses.clear();
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });

    }  @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode){
            case 1:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    havePermission = true;
                }else{
                    Toast.makeText(this,"无SD卡写入权限",Toast.LENGTH_SHORT).show();
                }
                break;
            default:
        }
    }


    private void setOneNew(JSONObject jsonObject){
        News news = new News();
        try {
            news.setCreate_time(jsonObject.getString("create_time"));
            news.setHate(jsonObject.getString("hate"));
            news.setId(jsonObject.getString("id"));
            news.setLove(jsonObject.getString("love"));
            news.setText(jsonObject.getString("text"));
            news.setName(jsonObject.getString("name"));
            news.setVideo_uri(jsonObject.getString("video_uri"));
            news.setVideotime(jsonObject.getString("videotime"));
            news.setVoiceuri(jsonObject.getString("voiceuri"));
            news.setProfile_image(jsonObject.getString("profile_image"));
        } catch (JSONException e) {
            e.printStackTrace();
        }
        mNewses.add(news);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) !=
                PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(MainActivity.this, new String[]
                    {Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
        } else {
            havePermission = true;
        }
    }
    long mPressedTime = 0;
    @Override
    public void onBackPressed() {
        long mNowTime = System.currentTimeMillis();//获取第一次按键时间
        if((mNowTime - mPressedTime) > 2000) {//比较两次按键时间差
            Toast.makeText(this, "再按一次退出程序", Toast.LENGTH_SHORT).show();
            mPressedTime = mNowTime;
        }
        else{
            //退出程序
            this.finish();
            System.exit(0);
        }
    }
}
