package com.hgsil.android.budejie2.BitmapUtils;

import android.content.Context;
import android.graphics.Bitmap;
import android.widget.ImageView;

import com.hgsil.android.budejie2.BitmapUtils.NetCacheUtils;


/**
 * Created by Administrator on 2017/3/8 0008.
 */

public class MyBitmapUtils {
    private NetCacheUtils mNetCacheUtils;
    private LocalCacheUtils mLocalCacheUtils;
    private MemoryCacheUtils mMemoryCacheUtils;



    public MyBitmapUtils(){
        mMemoryCacheUtils=new MemoryCacheUtils();
        mLocalCacheUtils=new LocalCacheUtils();
        mNetCacheUtils=new NetCacheUtils(mLocalCacheUtils,mMemoryCacheUtils);


    }


    public void disPlay(ImageView ivPic, String url,String id) {
        Bitmap bitmap;
        //内存缓存
        bitmap=mMemoryCacheUtils.getBitmapFromMemory(url);
        if (bitmap!=null){
            ivPic.setImageBitmap(bitmap);
            System.out.println("从内存获取图片啦.....");
            return;
        }

        //本地缓存
        bitmap = mLocalCacheUtils.getBitmapFromLocal(id);
        if(bitmap !=null){
            ivPic.setImageBitmap(bitmap);
            System.out.println("从本地获取图片啦.....");
            //从本地获取图片后,保存至内存中
            mMemoryCacheUtils.setBitmapToMemory(id,bitmap);
            return;
        }
        //网络缓存
        ivPic.setTag(url);
        mNetCacheUtils.getBitmapFromNet(ivPic,url,id);
    }
    public void downLoad(ImageView ivPic, String url,String id){
        mNetCacheUtils.getBitmapFromNet(ivPic,url,id);
    }
}
