package com.hgsil.android.budejie2;

import android.app.Application;
import android.content.Context;
import android.graphics.Bitmap;

import java.util.ArrayList;

/**
 * Created by Administrator on 2017/5/20 0020.
 */

public class MyApplication extends Application {
    private static Context sContext;

    @Override
    public void onCreate() {
        sContext = getApplicationContext();
    }
    public static Context getContext(){
        return sContext;
    }


}
