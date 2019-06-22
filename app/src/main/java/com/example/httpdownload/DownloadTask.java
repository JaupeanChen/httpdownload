package com.example.httpdownload;

import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.os.Environment;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class DownloadTask extends AsyncTask<String,Integer,Integer> {
    private DownloadListener listener;

    public DownloadTask(DownloadListener listener){
        this.listener = listener;
    }


    public static final int DOWNLOAD_SUCCEED = 1;
    public static final int DOWNLOAD_PAUSED = 2;
    public static final int DOWNLOAD_CANCEL = 3;
    public static final int DOWNLOAD_FAILED = 4;


    private boolean isPaused;
    private boolean isCanceled;
    private int lastProgress;

    private ProgressDialog dialog = new ProgressDialog(ContextGetter.getContext());


    @Override
    protected Integer doInBackground(String... strings) {
        InputStream inputStream = null;
        RandomAccessFile accessFile = null;
        File file = null;
        try {
            String url = strings[0];
            String name = url.substring(url.lastIndexOf("/"));
            if (isSDcardMounted()){
                String directory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getPath();
                file = new File(directory + name);
            }else {
                String path = ContextGetter.getContext().getCacheDir().getPath();
                file = new File(path + name);
                Toast.makeText(ContextGetter.getContext(),"No SDCard, will choose internalStorage",Toast.LENGTH_LONG).show();
            }

            long downloadedLength = 0;
            if (file.exists()){
                downloadedLength = file.length();
            }
            long contentLength = getContentLength(url);
            if (contentLength == 0){
                return DOWNLOAD_FAILED;
            }else if (downloadedLength == contentLength){
                return DOWNLOAD_SUCCEED;
            }
            OkHttpClient client = httpClient();
            Request request = new Request.Builder()
                    .addHeader("RANGE","bytes=" + downloadedLength + "-")
                    .url(url)
                    .build();
            Response response = client.newCall(request).execute();
            if (response!= null){
                inputStream = response.body().byteStream();
                accessFile = new RandomAccessFile(file,"rw");
                accessFile.seek(downloadedLength);

                byte[] bytes = new byte[1024];
                int total = 0;
                int len;
                while ((len = inputStream.read(bytes))!=0) {
                    if (isPaused) {
                        return DOWNLOAD_PAUSED;
                    }
                    if (isCanceled) {
                        return DOWNLOAD_CANCEL;
                    }
                    accessFile.write(bytes, 0, len);
                    total += len;
                    int progress = (int) ((total + downloadedLength) * 100 / contentLength);
                    publishProgress(progress);
                }
            }

            response.body().close();
            return DOWNLOAD_SUCCEED;



        }catch (IOException e){
            e.printStackTrace();
        }finally {
            try {
                if (inputStream != null){
                    inputStream.close();
                }
                if (accessFile != null){
                    accessFile.close();
                }
                if (isCanceled ){
                    if (file.exists()){
                        file.delete();
                    }

                }
            }catch (IOException e){
                e.printStackTrace();
            }

        }

        return DOWNLOAD_FAILED;
    }

    @Override
    protected void onProgressUpdate(Integer... values) {
        int progress = values[0];
        if (progress > lastProgress){
            listener.onProgress(progress);
            dialog.setTitle("Downloading");
            dialog.setCancelable(true);
            dialog.setProgress(progress);
            dialog.show();
            lastProgress = progress;
        }
    }

    @Override
    protected void onPostExecute(Integer integer) {
        switch (integer){
            case DOWNLOAD_SUCCEED:
                listener.onSucceed();
                break;
            case DOWNLOAD_FAILED:
                listener.onFailed();
                break;
            case DOWNLOAD_PAUSED:
                listener.onPaused();
                break;
            case DOWNLOAD_CANCEL:
                listener.onCancel();
                break;
                default:
                    break;
        }
    }

    public Boolean isSDcardMounted(){
        return Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED);
    }

    public long getContentLength(String url){
        long contentLength = 0;
        try {
            OkHttpClient client = httpClient();
            Request request = new Request.Builder()
                    .url(url)
                    .build();
            Response response = client.newCall(request).execute();
            if (response!=null && response.isSuccessful()){
                contentLength = response.body().contentLength();
                response.body().close();
            }

        }catch (IOException e){
            e.printStackTrace();
        }

        return contentLength;

    }


    public void pausedDownload(){
        isPaused = true;
    }

    public void cancelDownload(){
        isCanceled = true;
    }


    public static OkHttpClient httpClient() {
        return new OkHttpClient();
        /*
        //定义一个信任所有证书的TrustManager
        final X509TrustManager trustAllCert = new X509TrustManager() {
            @Override
            public void checkClientTrusted(java.security.cert.X509Certificate[] chain, String authType) throws CertificateException {
            }

            @Override
            public void checkServerTrusted(java.security.cert.X509Certificate[] chain, String authType) throws CertificateException {
            }

            @Override
            public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                return new java.security.cert.X509Certificate[]{};
            }
        };
//设置OkHttpClient
        return new OkHttpClient.Builder().sslSocketFactory(new SSL(trustAllCert), trustAllCert).build();
        */
    }
}
