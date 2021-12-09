package com.nd.dwebview_android;

import android.app.Application;

import com.nd.dwebview.launcher.DWebViewLauncher;

public class MApp extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        DWebViewLauncher.openDebug();
        DWebViewLauncher.init(this);
    }
}
