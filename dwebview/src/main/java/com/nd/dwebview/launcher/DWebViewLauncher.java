package com.nd.dwebview.launcher;

import android.app.Application;

import com.nd.dwebview.fragment.JsApiInitHelper;

/**
 * 入口类
 *
 * @author cwj
 * @date 2021/10/14 14:25
 */
public class DWebViewLauncher {


    private volatile static DWebViewLauncher INSTANCE = null;

    private volatile static boolean sHasInit = false;

    private volatile static boolean sDebuggable = false;


    private DWebViewLauncher() {

    }

    /**
     * Init, it must be call before used router.
     */
    public static void init(Application application) {
        if (!sHasInit) {
            JsApiInitHelper.init(application);
        }
    }


    public static DWebViewLauncher getInstance() {
        if (!sHasInit) {
            throw new RuntimeException("DWebView::Init::Invoke init(context) first!");
        } else {
            if (INSTANCE == null) {
                synchronized (DWebViewLauncher.class) {
                    if (INSTANCE == null) {
                        INSTANCE = new DWebViewLauncher();
                    }
                }
            }
            return INSTANCE;
        }
    }


    public static boolean debuggable() {
        return sDebuggable;
    }

    public static synchronized void openDebug() {
        sDebuggable = true;
    }


}
