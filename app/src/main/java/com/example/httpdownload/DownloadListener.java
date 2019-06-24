package com.example.httpdownload;

public interface DownloadListener {
    void onProgress(int progress);
    void onPaused();
    void onFailed();
    void onCancel();
    void onSucceed();
}
