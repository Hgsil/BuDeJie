package com.hgsil.android.budejie2.BitmapUtils;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Created by Administrator on 2017/3/8 0008.
 */

public class LocalCacheUtils {
    private static final String CACHE_PATH= Environment.getExternalStorageDirectory().getAbsolutePath()+"/BuDeJie2/Picture";

    /**
     * 从本地读取图片
     * @param id
     */
    public  static Bitmap getBitmapFromLocal(String id){
        try {
            File file=new File(CACHE_PATH,id);

            Bitmap bitmap = BitmapFactory.decodeStream(new FileInputStream(file));

            return bitmap;
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    /**
     * 从网络获取图片后,保存至本地缓存
     * @param id
     * @param bitmap
     */
    public void setBitmapToLocal(String id,Bitmap bitmap){
        saveImage(bitmap,id);

    }
    //缓存图片
    public static void saveImage(Bitmap bmp,String id) {
        File appDir = new File(Environment.getExternalStorageDirectory(), "BuDeJie2/Picture");
        if (!appDir.exists()) {
            appDir.mkdir();
        }

        File file = new File(appDir , id);
        try {
            BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(file));
            /*FileOutputStream fos = new FileOutputStream(file);
r
            */
            bmp.compress(Bitmap.CompressFormat.JPEG, 100, bos);
            bos.flush();
            bos.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }




}
