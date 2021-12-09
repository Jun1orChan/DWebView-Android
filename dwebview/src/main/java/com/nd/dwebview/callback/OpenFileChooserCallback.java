package com.nd.dwebview.callback;

import android.net.Uri;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;

/**
 * @author Administrator
 */
public interface OpenFileChooserCallback {

    /**
     * for >=5.0
     *
     * @param filePathCallback
     * @param fileChooserParams
     */
    void openFileChooserCallBack(ValueCallback<Uri[]> filePathCallback, WebChromeClient.FileChooserParams fileChooserParams);
}
