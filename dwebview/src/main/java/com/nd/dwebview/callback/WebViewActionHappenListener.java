package com.nd.dwebview.callback;

import android.graphics.Bitmap;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;

/**
 * @author Administrator
 */
public interface WebViewActionHappenListener {

    /**
     * 接收到title
     *
     * @param title
     */
    void onReceivedTitleHappened(String title);

    /**
     * 发生http异常
     *
     * @param request
     * @param errorResponse
     */
    void onReceivedHttpErrorHappened(WebResourceRequest request, WebResourceResponse errorResponse);

    /**
     * 发生异常
     *
     * @param request
     * @param error
     */
    void onReceivedErrorHappened(WebResourceRequest request, WebResourceError error);

    /**
     * 低版本异常回调
     *
     * @param errorCode
     * @param description
     * @param failingUrl
     */
    void onReceivedErrorHappened(int errorCode, String description, String failingUrl);

    /**
     * 页面开始
     *
     * @param url
     * @param favicon
     */
    void onPageStartedHappened(String url, Bitmap favicon);

    /**
     * 页面结束
     *
     * @param url
     */
    void onPageFinishedHappened(String url);
}