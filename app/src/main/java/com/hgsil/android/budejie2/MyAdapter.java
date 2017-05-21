package com.hgsil.android.budejie2;

import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.Loader;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.media.AudioManager;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.provider.Settings;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;

import android.widget.SeekBar;
import android.widget.TextView;


import com.hgsil.android.budejie2.BitmapUtils.LocalCacheUtils;

import com.hgsil.android.budejie2.BitmapUtils.MyBitmapUtils;
import com.hgsil.android.budejie2.View.CircleImageView;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by Administrator on 2017/5/19 0019.
 */

public class MyAdapter extends RecyclerView.Adapter<MyAdapter.ViewHolder> {
    private List<News> mNewses ;
    private String TAG = "MainActivity";
    private ArrayList<ViewHolder> mViewHolders = new ArrayList<>();
    private MyBitmapUtils myBitmapUtils = new MyBitmapUtils();
    private SharedPreferences mSharedPreferences;
    private Context mContext;
    int pictuersCount = 0;


    class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{

        boolean actionIsShow = false;
        Bitmap videoPicture;
        boolean isPlaying = false ;
        boolean isShow = false;
        boolean isRebuild = false;
        boolean hasDownLoad = true;

        private DownloadManager mDownloadManager ;

        private News oneNew = new News();
        private LinearLayout contentAction;
        private TextView hate,love,date,userName,title;
        private ImageView isHate,isLove,pause,download;
        private SurfaceView mSurfaceView;
        private CircleImageView avatar;
        private MediaPlayer mediaPlayer;
        private int currentPosition;

        private SeekBar seekBar ;
        private ImageView mediaImage;
        private Handler mHandler = new Handler(){
            @Override
            public void handleMessage(Message msg) {

                super.handleMessage(msg);
                switch (msg.what){
                    case 0:
                        if (mediaImage.getTag().equals(oneNew.getVideo_uri()))
                            mediaImage.setImageBitmap(LocalCacheUtils.getBitmapFromLocal(oneNew.getId()));
                        break;
                    case 1:
                        currentPosition =mSharedPreferences.getInt(oneNew.getVideo_uri(),0);
                        mediaImage.setVisibility(View.GONE);
                        mSurfaceView.setVisibility(View.VISIBLE);
                        seekBar.setVisibility(View.VISIBLE);
                        play(currentPosition);
                        break;
                    case 2:
                        pause.setImageResource(R.mipmap.pause);
                        break;
                    case 3:
                        pause.setImageResource(R.mipmap.start);
                        break;
                    case 4:
                        contentAction.setVisibility(View.VISIBLE);
                        actionIsShow = true;
                        break;
                    case 5:
                        contentAction.setVisibility(View.GONE);
                        actionIsShow = false;
                        break;
                }
            }
        };
        private long mTaskId;

        private BroadcastReceiver receiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                checkDownloadStatus();
            }
        };
        //检查下载状态
        private void checkDownloadStatus() {
            DownloadManager.Query query = new DownloadManager.Query();
            query.setFilterById(mTaskId);//筛选下载任务，传入任务ID，可变参数
            Cursor c = mDownloadManager.query(query);
            if (c.moveToFirst()) {
                int status = c.getInt(c.getColumnIndex(DownloadManager.COLUMN_STATUS));

                    if (status == DownloadManager.STATUS_SUCCESSFUL)
                        hasDownLoad = true;
                }
            }


        public ViewHolder(View itemView) {
            super(itemView);
            contentAction = (LinearLayout)itemView.findViewById(R.id.news_content_action);
            contentAction.getBackground().setAlpha(60);
            hate = (TextView)itemView.findViewById(R.id.news_naive);

            date = (TextView)itemView.findViewById(R.id.news_date);
            title = (TextView)itemView.findViewById(R.id.news_title) ;
            userName = (TextView)itemView.findViewById(R.id.news_username);
            love = (TextView)itemView.findViewById(R.id.news_exciting);
            isHate = (ImageView)itemView.findViewById(R.id.news_isNaive_image);
            isLove = (ImageView)itemView.findViewById(R.id.news_isExciting_image);

            pause = (ImageView)itemView.findViewById(R.id.news_content_action_start);
            download = (ImageView)itemView.findViewById(R.id.news_content_action_download);

            seekBar = (SeekBar)itemView.findViewById(R.id.news_content_seek);
            avatar = (CircleImageView)itemView.findViewById(R.id.news_avatar);
            mSurfaceView = (SurfaceView)itemView.findViewById(R.id.news_content_surface);
            mediaImage = (ImageView)itemView.findViewById(R.id.news_content_image);


            download.setOnClickListener(this);
            mediaImage.setOnClickListener(this);
            mSurfaceView.setOnClickListener(this);
            pause.setOnClickListener(this);

            mSurfaceView.getHolder().addCallback(callback);

            seekBar.setOnSeekBarChangeListener(change);

        }
        //seekbar进度改变时间
        private SeekBar.OnSeekBarChangeListener change = new SeekBar.OnSeekBarChangeListener() {
            @Override
            // 当进度条停止修改的时候触发，滑动
            public void onStopTrackingTouch(SeekBar seekBar) {
                // 取得当前进度条的刻度
                int progress = seekBar.getProgress();
                if (mediaPlayer != null && mediaPlayer.isPlaying()) {
                    // 设置当前播放的位置
                    mediaPlayer.seekTo(progress);

                }
            }
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

            }
        };

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

        private SurfaceHolder.Callback callback = new SurfaceHolder.Callback() {
            // SurfaceHolder被修改的时候回调
            @Override
            public void surfaceDestroyed(SurfaceHolder holder) {
                Log.i(TAG, "SurfaceHolder 被销毁");
                currentPosition = seekBar.getProgress();
                SharedPreferences.Editor editor = mSharedPreferences.edit();
                editor.putInt(oneNew.getVideo_uri(),currentPosition);
                editor.apply();
                // 销毁SurfaceHolder的时候记录当前的播放位置并停止播放
                if (mediaPlayer != null && mediaPlayer.isPlaying()) {

                    mediaPlayer.stop();
                    mediaPlayer = null;
                    isRebuild = true;
                    pause.setImageResource(R.mipmap.start);
                }
            }
            @Override
            public void surfaceCreated(SurfaceHolder holder) {
                Log.i(TAG, "SurfaceHolder 被创建");
                    if (isRebuild) {
                        isRebuild = false ;
                        play(mSharedPreferences.getInt(oneNew.getVideo_uri(), 0));
                    }
                    Message message =new Message();
                    message.what = 0 ;
                    mHandler.sendMessage(message);

                    // 创建SurfaceHolder的时候，如果存在上次播放的位置，则按照上次播放位置进行播放

            }
            @Override
            public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

                Log.i("MainActivity", "SurfaceHolder 大小被改变");
            }
        };

        protected void download(){
            //创建下载任务,downloadUrl就是下载链接
            DownloadManager.Request request = new DownloadManager.Request(Uri.parse(oneNew.getVideo_uri()));
            //指定下载路径和下载文件名
            request.setDestinationInExternalPublicDir("/download/",oneNew.getVideo_uri());
            //在通知栏中显示，默认就是显示的
            request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE);
            request.setVisibleInDownloadsUi(true);
            //获取下载管理器
            mDownloadManager= (DownloadManager) mContext.getSystemService(Context.DOWNLOAD_SERVICE);
            //将下载任务加入下载队列，否则不会进行下载
            mTaskId = mDownloadManager.enqueue(request);
            SharedPreferences.Editor editor = mSharedPreferences.edit();
            editor.putString(oneNew.getId()+oneNew.getVideo_uri(),String.valueOf(mDownloadManager.getUriForDownloadedFile(mTaskId)));

        }

        protected void play(final int msec) {
            setWatchToDataBase();
            try {
                mTaskId = mSharedPreferences.getLong(oneNew.getId(),0);
                mediaPlayer = new MediaPlayer();
                mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
                if (!hasDownLoad)
                    mediaPlayer.setDataSource(mSharedPreferences.getString(oneNew.getId()+oneNew.getVideo_uri(),null));
                else
                mediaPlayer.setDataSource(oneNew.getVideo_uri());

                // 设置显示视频的SurfaceHolder
                mediaPlayer.setDisplay(mSurfaceView.getHolder());
                Log.i(TAG, "开始装载");
                mediaPlayer.prepareAsync();

                mediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                    @Override
                    public void onPrepared(MediaPlayer mp) {
                        // 设置进度条的最大进度为视频流的最大播放时长
                        seekBar.setMax(mediaPlayer.getDuration());
                        mediaPlayer.seekTo(msec);
                        Log.i(TAG, "装载完成");
                        mediaPlayer.start();


                        // 开始线程，更新进度条的刻度
                        new Thread() {
                            @Override
                            public void run() {
                                try {
                                    isShow = true;
                                    isPlaying = true;
                                    while (isPlaying) {
                                         int current = mediaPlayer.getCurrentPosition();
                                         seekBar.setProgress(current);
                                         sleep(500);
                                    }
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                        }.start();

                    }
                });
                mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                    @Override
                    public void onCompletion(MediaPlayer mp) {
                        // 在播放完毕被回调
                        isShow = false;
                    }
                });
                mediaPlayer.setOnErrorListener(new MediaPlayer.OnErrorListener() {
                    @Override
                    public boolean onError(MediaPlayer mp, int what, int extra) {
                        // 发生错误重新播放
                        play(0);
                        isPlaying = false;
                        isShow = false;
                        return false;
                    }
                });

            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        protected void pause() {
            if (mediaPlayer == null){
                isRebuild = false ;
                isShow = true;
                Message message = new Message();
                message.what = 1;
                mHandler.sendMessage(message);
                Message message1 = new Message();
                message1.what = 2;
                mHandler.sendMessage(message1);
                return;
            }
            else if (!isShow) {
                isShow = true;
                mediaPlayer.start();
                Message message = new Message();
                message.what = 2;
                mHandler.sendMessage(message);
                return;
            }
            else if (mediaPlayer != null && mediaPlayer.isPlaying()) {
                mediaPlayer.pause();
                isShow = false;
                Message message = new Message();
                message.what = 3;
                mHandler.sendMessage(message);
            }
        }

        @Override
        public void onClick(View view) {
            Message message = new Message();
            switch (view.getId()){
                case R.id.news_content_image:
                    //setWatchToDataBase();
                    message.what = 1;
                    mHandler.sendMessage(message);
                    break;
                case R.id.news_content_surface:
                    if (!actionIsShow) {
                        message.what = 4;
                    }
                    else
                        message.what = 5;
                    mHandler.sendMessage(message);
                    break;
                case R.id.news_content_action_start:
                    pause();
                    break;
                case R.id.news_content_action_download:
                    download();
                    break;
            }
        }

        public void setWatchToDataBase(){
            ContentValues contentValues = new ContentValues();
            contentValues.put("id",oneNew.getId());
            contentValues.put("videoUrl",oneNew.getVideo_uri());
            contentValues.put("text",oneNew.getText());
            contentValues.put("username",oneNew.getName());
            contentValues.put("avatar",oneNew.getProfile_image());
            contentValues.put("watchTime",System.currentTimeMillis()+"");
            MyDatabaseHelper.DataBaseBuilder.getDataBaseHelper(mContext).replace("Watch",null ,contentValues);
        }

    }

    public MyAdapter(List<News> newses, Context context) {
        mNewses = new ArrayList<>();
        mNewses.addAll(newses);
        mContext = context;

    }

    @Override
    public MyAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view =  LayoutInflater.from(parent.getContext()).inflate(R.layout.video_item,parent,false);
        ViewHolder viewHolder = new ViewHolder(view);
        mViewHolders.add(viewHolder);
        mSharedPreferences = mContext.getSharedPreferences("pictures",mContext.MODE_PRIVATE);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        holder.oneNew = mNewses.get(position);

        holder.hate.setText(holder.oneNew.getLove());
        holder.love.setText(holder.oneNew.getHate());
        holder.date.setText(holder.oneNew.getCreate_time());
        holder.userName.setText(holder.oneNew.getName());
        holder.isHate.setImageResource(R.mipmap.love);
        holder.isLove.setImageResource(R.mipmap.hate);
        holder.title.setText(holder.oneNew.getText());
        if (holder.isRebuild)
            holder.pause.setImageResource(R.mipmap.start);
        else
        holder.pause.setImageResource(R.mipmap.pause);


        myBitmapUtils.disPlay(holder.avatar,holder.oneNew.getProfile_image(),holder.oneNew.getProfile_image());
        if (!LocalCacheUtils.getBitmapFromLocal(holder.oneNew.getId()).equals(null))
            holder.mediaImage.setImageBitmap(LocalCacheUtils.getBitmapFromLocal(holder.oneNew.getId()));
        else
        new Thread(){
            @Override
            public void run() {
                holder.videoPicture = holder.createVideoThumbnail(holder.oneNew.getVideo_uri(),300,200);
                LocalCacheUtils.saveImage(holder.videoPicture,holder.oneNew.getId());
                Message message = new Message();
                message.what = 0 ;
                holder.mHandler.sendMessage(message);
            }}.start();

    }


    @Override
    public int getItemCount() {
        return mNewses.size();
    }

    public void addItem(List<News> newses){
        mNewses.addAll(newses);
        notifyDataSetChanged();
    }
    public void refresh(List<News> newses){
        mNewses.clear();
        mNewses.addAll(newses);
        notifyDataSetChanged();
    }

}
