package com.hgsil.android.budejie2;

import android.os.Environment;
import android.provider.MediaStore;

import com.hgsil.android.budejie2.HttpUtils.HttpUtil;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Created by Administrator on 2017/5/21 0021.
 */

public class DownLoadToLocal {
    public interface DownCallback{
        void onSuccess();

    }

    public static void saveFromNet(String url,String path,DownCallback callback){
        BufferedInputStream inputStream  = new BufferedInputStream(HttpUtil.get(url));
        byte[] bytes = new byte[512];
        File appDir = new File(Environment.getExternalStorageDirectory(), "BuDeJie2");
        if (!appDir.exists()) {
            appDir.mkdirs();
        }

        File dir = new File(appDir , "Video");
        if (!dir.exists()){
          dir.mkdirs();
        }
        File file = new File(dir, path);
        if (!file.exists()){
            file.mkdirs();
        }
        try {
            BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(file));

            while (inputStream.read(bytes,0,512)>0){
                bos.write(inputStream.read(bytes));
            }

            callback.onSuccess();
            inputStream.close();
            bos.flush();
            bos.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
