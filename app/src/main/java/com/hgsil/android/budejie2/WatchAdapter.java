package com.hgsil.android.budejie2;

import android.content.Context;
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
    private Context mContext;
    private MyBitmapUtils myBitmapUtils = new MyBitmapUtils();
    class WatchViewHolder extends RecyclerView.ViewHolder{
        News oneNew;
        TextView title,userName,watchTime;
        ImageView mediaImage,delete;
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
            delete =  (ImageView)itemView.findViewById(R.id.watch_item_delete);
            delete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    delete(oneNew.getId());
                    for (int i = 0; i < mNewses.size() ; i++) {
                        if (mNewses.get(i).getId().equals(oneNew.getId())){
                            mNewses.remove(i);
                        }
                    }
                    notifyDataSetChanged();
                }
            });
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
        private void delete(String id){
            MyDatabaseHelper.DataBaseBuilder.getDataBaseHelper(mContext).delete("Watch","id=?",new String[]{
                    id
            });
        }
    }

    public WatchAdapter(ArrayList<News> newses, Context context) {
        mNewses = new ArrayList<>();
        mNewses.addAll(newses);
        mContext = context;
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
        holder.delete.setImageResource(R.mipmap.delete);
        holder.watchTime.setText(holder.oneNew.getWatchTime()+" 观看");
    }

    @Override
    public int getItemCount() {
       return mNewses.size();
    }


}
