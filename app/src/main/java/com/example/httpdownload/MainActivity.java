package com.example.httpdownload;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.ResultReceiver;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    private DownloadService.mBinder binder;
    private ServiceConnection connection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            binder = (DownloadService.mBinder) service;
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {

        }
    };
    private ProgressBar progressBar;



//    public static final int UPDATE_PROGRESS = 1;
//    public  Handler handler = new Handler(){
//        @Override
//        public void handleMessage(Message msg) {
//            switch (msg.what){
//                case UPDATE_PROGRESS:
//                    progressBar.setProgress(msg.arg1);
//            }
//        }
//    };



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Button start = findViewById(R.id.start);
        Button paused = findViewById(R.id.paused);
        Button cancel = findViewById(R.id.cancel);
        progressBar = findViewById(R.id.progress);


        progressBar.setVisibility(View.GONE);
        start.setOnClickListener(this);
        paused.setOnClickListener(this);
        cancel.setOnClickListener(this);
        Intent intent = new Intent(this,DownloadService.class);
        intent.putExtra("receiver", new MyResultReceiver(new Handler()));
        startService(intent);
        bindService(intent,connection,BIND_AUTO_CREATE);

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)!= PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},0);
        }
    }


    @Override
    public void onClick(View v) {
        if (binder != null){
            switch (v.getId()){
                case R.id.start:
                    String url = "http://sj2.img4399.com/downloader/tuer/1.9.2.137/tuer_1.9.2.137.wap.apk";
                    progressBar.setVisibility(View.VISIBLE);
                    binder.startDownload(url);
                    break;
                case R.id.paused:
                    binder.pausedDownload();
                    break;
                case R.id.cancel:
                    binder.cancelDownload();
                    break;
                    default:
                        break;
            }
        }
    }



    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode){
            case 0:
                if (grantResults.length>0 && grantResults[0]!= PackageManager.PERMISSION_GRANTED){
                    Toast.makeText(this,"You deny the permission!",Toast.LENGTH_SHORT).show();
                    finish();
                }
                break;
                default:
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        unbindService(connection);
    }


    class MyResultReceiver extends ResultReceiver {

        public static final int UPDATE_PROGRESS = 1;
        public MyResultReceiver(Handler handler){
            super(handler);
        }
        @Override
        protected void onReceiveResult(int resultCode, Bundle resultData) {
            super.onReceiveResult(resultCode, resultData);
            if (resultCode == UPDATE_PROGRESS){
                int progress = resultData.getInt("progress");
                progressBar.setProgress(progress);
            }
        }
    }




}
