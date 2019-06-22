package com.example.httpdownload;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.Binder;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.ResultReceiver;
import android.support.v4.app.NotificationCompat;
import android.widget.ProgressBar;
import android.widget.Toast;

import java.io.File;

import static android.content.Intent.getIntent;
import static android.content.Intent.getIntentOld;

public class DownloadService extends Service {
    private DownloadTask downloadTask;
    private mBinder binder = new mBinder();
    private String downloadUrl;
    private ResultReceiver resultReceiver;
    public static final int RESULT_OK = 0;
    public static final int UPDATE_PROGRESS = 1;


//    public static final int UPDATE_PROGRESS = 1;
//    Handler handler = new Handler();


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        resultReceiver = intent.getParcelableExtra("receiver");
        return RESULT_OK;
    }

    DownloadListener listener = new DownloadListener() {
        @Override
        public void onProgress(int progress) {
//            Message message = new Message();
//            message.what = UPDATE_PROGRESS;
//            message.arg1 = progress;
//            handler.sendMessage(message);
            Bundle bundle = new Bundle();
            bundle.putInt("progress",progress);
            resultReceiver.send(UPDATE_PROGRESS ,bundle);
            getNotificationManager().notify(1,getNotification("Downloading...",progress));

        }

        @Override
        public void onPaused() {
            downloadTask = null;
            Toast.makeText(DownloadService.this,"Paused Download",Toast.LENGTH_SHORT).show();

        }

        @Override
        public void onFailed() {
            downloadTask = null;
            stopForeground(true);
            getNotificationManager().notify(1,getNotification("Download Failed!",-1));
            Toast.makeText(DownloadService.this,"Download Failed!",Toast.LENGTH_LONG).show();

        }

        @Override
        public void onCancel() {
            downloadTask = null;
            stopForeground(true);
            Toast.makeText(DownloadService.this, "Cancel Download!",Toast.LENGTH_LONG).show();

        }

        @Override
        public void onSucceed() {
            downloadTask = null;
            stopForeground(true);
            getNotificationManager().notify(1,getNotification("Download Succeed!",-1));
            Toast.makeText(DownloadService.this, "Download Succeed!", Toast.LENGTH_SHORT).show();

        }
    };


    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        return binder;
    }


    class mBinder extends Binder{
        public void startDownload(String url){
            if (downloadTask == null){
                downloadUrl = url;
                downloadTask = new DownloadTask(listener);
                downloadTask.execute(url);
                startForeground(1,getNotification("Downloading...",0));
                Toast.makeText(DownloadService.this,"Start Download!",Toast.LENGTH_LONG).show();
            }


        }

        public void pausedDownload(){
            if (downloadTask != null){
                downloadTask.pausedDownload();
            }

        }

        public void cancelDownload(){
            if (downloadTask != null){
                downloadTask.cancelDownload();
            }else if (downloadUrl != null){
                String name = downloadUrl.substring(downloadUrl.lastIndexOf("/"));
                String directory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getPath();
                File file = new File(directory + name);
                if (file.exists()){
                    file.delete();
                }
                getNotificationManager().cancel(1);
                stopForeground(true);
                Toast.makeText(DownloadService.this,"Canceled",Toast.LENGTH_SHORT).show();
            }

        }
    }

    public NotificationManager getNotificationManager(){
        return (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
    }

    public Notification getNotification(String title, int progress){
        Intent intent = new Intent(this,MainActivity.class);
        PendingIntent pi = PendingIntent.getActivity(this,0,intent,PendingIntent.FLAG_UPDATE_CURRENT);
        Notification.Builder builder = new Notification.Builder(this);
        builder.setContentIntent(pi);
        builder.setContentTitle(title);
        builder.setSmallIcon(R.mipmap.ic_launcher);
        builder.setLargeIcon(BitmapFactory.decodeResource(getResources(),R.mipmap.ic_launcher));
        if (progress>=0){
            builder.setContentText( progress + "%");
            builder.setProgress(100,progress,false);
        }
        return builder.build();
    }






}
