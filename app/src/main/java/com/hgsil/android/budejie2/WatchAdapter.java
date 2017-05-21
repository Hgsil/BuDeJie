package com.hgsil.android.budejie2;

import android.graphics.Bitmap;
import android.media.MediaMetadataRetriever;
import android.media.ThumbnailUtils;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import android.widget.ImageView;
import android.widget.TextView;

import com.hgsil.android.budejie2.BitmapUtils.LocalCacheUtils;
import com.hgsil.android.budejie2.BitmapUtils.MyBitmapUtils;
import com.hgsil.android.budejie2.View.CircleImageView;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by Administrator on 2017/5/20 0020.
 */

public class WatchAdapter extends RecyclerView.Adapter<WatchAdapter.WatchViewHolder> {
    private ArrayList<News> mNewses ;
    private MyBitmapUtils myBitmapUtils = new MyBitmapUtils();
    class WatchViewHolder extends RecyclerView.ViewHolder{
        News oneNew;
        TextView title,userName,watchTime;
        ImageView mediaImage;
        CircleImageView avatar;

        Handler mHandler = new Handler(){
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                if (msg.what == 0)
                if (mediaImage.getTag().equals(oneNew.getVideo_uri()))
                    mediaImage.setImageBitmap(LocalCacheUtils.getBitmapFromLocal(oneNew.getId()));
            }
        };
        public WatchViewHolder(View itemView) {
            super(itemView);
            title = (TextView)itemView.findViewById(R.id.watch_item_title) ;
            userName = (TextView)itemView.findViewById(R.id.watch_item_username);
            avatar = (CircleImageView)itemView.findViewById(R.id.watch_item_avatar);
            watchTime = (TextView)itemView.findViewById(R.id.watch_item_watchTime);

        }
        //获取视频首桢图片
        private Bitmap createVideoThumbnail(String url, int width, int height) {
            Bitmap bitmap = null;
            MediaMetadataRetriever retriever = new MediaMetadataRetriever();
            int kind = MediaStore.Video.Thumbnails.MINI_KIND;
            try {
                if (Build.VERSION.SDK_INT >= 14) {
                    mediaImage.setTag(oneNew.getVideo_uri());
                    retriever.setDataSource(url, new HashMap<String, String>());
                } else {
                    retriever.setDataSource(url);
                }
                bitmap = retriever.getFrameAtTime();
            } catch (IllegalArgumentException ex) {
                // Assume this is a corrupt video file
            } catch (RuntimeException ex) {
                // Assume this is a corrupt video file.
            } finally {
                try {
                    retriever.release();
                } catch (RuntimeException ex) {
                    // Ignore failures while cleaning up.
                }
            }
            if (kind == MediaStore.Images.Thumbnails.MICRO_KIND && bitmap != null) {
                bitmap = ThumbnailUtils.extractThumbnail(bitmap, width, height,
                        ThumbnailUtils.OPTIONS_RECYCLE_INPUT);
            }
            return bitmap;
        }

    }

    public WatchAdapter(ArrayList<News> newses) {
        mNewses = new ArrayList<>();
        mNewses.addAll(newses);
    }

    @Override
    public WatchViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view =  LayoutInflater.from(parent.getContext()).inflate(R.layout.watch_item,parent,false);
        WatchViewHolder viewHolder = new WatchViewHolder(view);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(final WatchViewHolder holder, int position) {
        holder.oneNew = mNewses.get(position);
        holder.userName.setText(holder.oneNew.getName());
        holder.title.setText(holder.oneNew.getText());
        myBitmapUtils.disPlay(holder.avatar,holder.oneNew.getProfile_image(),holder.oneNew.getId());

        holder.watchTime.setText(holder.oneNew.getWatchTime()+" 观看");
    }

    @Override
    public int getItemCount() {
       return mNewses.size();
    }


}
