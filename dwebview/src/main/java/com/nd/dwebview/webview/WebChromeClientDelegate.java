package com.nd.dwebview.webview;

import android.annotation.TargetApi;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Message;
import android.view.View;
import android.webkit.ConsoleMessage;
import android.webkit.GeolocationPermissions;
import android.webkit.JsPromptResult;
import android.webkit.JsResult;
import android.webkit.PermissionRequest;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebStorage;
import android.webkit.WebView;

import com.nd.dwebview.callback.IVideo;
import com.nd.dwebview.callback.OpenFileChooserCallback;
import com.nd.dwebview.callback.WebViewActionHappenListener;

/**
 * @author cwj
 */
public class WebChromeClientDelegate extends WebChromeClient {

    private WebChromeClient mDelegate;

    private OpenFileChooserCallback mOpenFileChooserCallback;

    private WebViewActionHappenListener mWebViewActionHappenListener;

    private IVideo mVideo;

    public void setVideoImpl(IVideo videoImpl) {
        mVideo = videoImpl;
    }


    public void setDelegate(WebChromeClient delegate) {
        mDelegate = delegate;
    }

    public void setOpenFileChooserCallback(OpenFileChooserCallback openFileChooserCallback) {
        mOpenFileChooserCallback = openFileChooserCallback;
    }

    public void setWebViewActionHappenListener(WebViewActionHappenListener webViewActionHappenListener) {
        this.mWebViewActionHappenListener = webViewActionHappenListener;
    }

    @Override
    public void onProgressChanged(WebView view, int newProgress) {
        if (mDelegate != null) {
            mDelegate.onProgressChanged(view, newProgress);
        } else {
            super.onProgressChanged(view, newProgress);
        }
    }

    @Override
    public void onReceivedTitle(WebView view, String title) {
        if (title.startsWith(DWebView.ERROR_URL_START_ABOUT_BLANK) || title.startsWith(DWebView.ERROR_URL_START_DATA)) {
            super.onReceivedTitle(view, title);
            return;
        }
        if (mWebViewActionHappenListener != null) {
            mWebViewActionHappenListener.onReceivedTitleHappened(title);
        }
        if (mDelegate != null) {
            mDelegate.onReceivedTitle(view, title);
        } else {
            super.onReceivedTitle(view, title);
        }
    }

    @Override
    public void onReceivedIcon(WebView view, Bitmap icon) {
        if (mDelegate != null) {
            mDelegate.onReceivedIcon(view, icon);
        } else {
            super.onReceivedIcon(view, icon);
        }
    }

    @Override
    public void onReceivedTouchIconUrl(WebView view, String url, boolean precomposed) {
        if (mDelegate != null) {
            mDelegate.onReceivedTouchIconUrl(view, url, precomposed);
        } else {
            super.onReceivedTouchIconUrl(view, url, precomposed);
        }
    }

    @Override
    public Bitmap getDefaultVideoPoster() {
        return Bitmap.createBitmap(200, 200, Bitmap.Config.ARGB_8888);
    }

    @Override
    public void onShowCustomView(View view, CustomViewCallback callback) {
        if (mVideo != null) {
            mVideo.onShowCustomView(view, callback);
        }
        if (mDelegate != null) {
            mDelegate.onShowCustomView(view, callback);
        } else {
            super.onShowCustomView(view, callback);
        }
    }

    @Override
    public void onHideCustomView() {
        if (mVideo != null) {
            mVideo.onHideCustomView();
        }
        if (mDelegate != null) {
            mDelegate.onHideCustomView();
        } else {
            super.onHideCustomView();
        }
    }

    @Override
    public boolean onCreateWindow(WebView view, boolean isDialog,
                                  boolean isUserGesture, Message resultMsg) {
        if (mDelegate != null) {
            return mDelegate.onCreateWindow(view, isDialog,
                    isUserGesture, resultMsg);
        }
        return super.onCreateWindow(view, isDialog, isUserGesture, resultMsg);
    }

    @Override
    public void onRequestFocus(WebView view) {
        if (mDelegate != null) {
            mDelegate.onRequestFocus(view);
        } else {
            super.onRequestFocus(view);
        }
    }

    @Override
    public void onCloseWindow(WebView window) {
        if (mDelegate != null) {
            mDelegate.onCloseWindow(window);
        } else {
            super.onCloseWindow(window);
        }
    }

    @Override
    public boolean onJsAlert(WebView view, String url, final String message, final JsResult result) {
        result.confirm();
        if (mDelegate != null) {
            if (mDelegate.onJsAlert(view, url, message, result)) {
                return true;
            }
        }
        return true;
    }

    @Override
    public boolean onJsConfirm(WebView view, String url, String message,
                               final JsResult result) {
        if (mDelegate != null) {
            mDelegate.onJsConfirm(view, url, message, result);
        }
        return true;
    }

    @Override
    public boolean onJsPrompt(WebView view, String url, final String message,
                              String defaultValue, final JsPromptResult result) {

        if (mDelegate != null) {
            return mDelegate.onJsBeforeUnload(view, url, message, result);
        } else {
            return super.onJsPrompt(view, url, message, defaultValue, result);
        }
    }

    @Override
    public boolean onJsBeforeUnload(WebView view, String url, String message, JsResult result) {
        if (mDelegate != null) {
            return mDelegate.onJsBeforeUnload(view, url, message, result);
        }
        return super.onJsBeforeUnload(view, url, message, result);
    }

    @Override
    public void onExceededDatabaseQuota(String url, String databaseIdentifier, long quota,
                                        long estimatedDatabaseSize,
                                        long totalQuota,
                                        WebStorage.QuotaUpdater quotaUpdater) {
        if (mDelegate != null) {
            mDelegate.onExceededDatabaseQuota(url, databaseIdentifier, quota,
                    estimatedDatabaseSize, totalQuota, quotaUpdater);
        } else {
            super.onExceededDatabaseQuota(url, databaseIdentifier, quota,
                    estimatedDatabaseSize, totalQuota, quotaUpdater);
        }
    }

    @Override
    public void onReachedMaxAppCacheSize(long requiredStorage, long quota, WebStorage.QuotaUpdater quotaUpdater) {
        if (mDelegate != null) {
            mDelegate.onReachedMaxAppCacheSize(requiredStorage, quota, quotaUpdater);
        }
        super.onReachedMaxAppCacheSize(requiredStorage, quota, quotaUpdater);
    }

    @Override
    public void onGeolocationPermissionsShowPrompt(String origin, GeolocationPermissions.Callback callback) {
        if (mDelegate != null) {
            mDelegate.onGeolocationPermissionsShowPrompt(origin, callback);
        } else {
            super.onGeolocationPermissionsShowPrompt(origin, callback);
        }
    }

    @Override
    public void onGeolocationPermissionsHidePrompt() {
        if (mDelegate != null) {
            mDelegate.onGeolocationPermissionsHidePrompt();
        } else {
            super.onGeolocationPermissionsHidePrompt();
        }
    }


    @Override
    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public void onPermissionRequest(PermissionRequest request) {
        if (mDelegate != null) {
            mDelegate.onPermissionRequest(request);
        } else {
            super.onPermissionRequest(request);
        }
    }


    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    @Override
    public void onPermissionRequestCanceled(PermissionRequest request) {
        if (mDelegate != null) {
            mDelegate.onPermissionRequestCanceled(request);
        } else {
            super.onPermissionRequestCanceled(request);
        }
    }

    @Override
    public boolean onConsoleMessage(ConsoleMessage consoleMessage) {
        if (mDelegate != null) {
            return mDelegate.onConsoleMessage(consoleMessage);
        }
        return super.onConsoleMessage(consoleMessage);
    }


    @Override
    public View getVideoLoadingProgressView() {
        if (mDelegate != null) {
            return mDelegate.getVideoLoadingProgressView();
        }
        return super.getVideoLoadingProgressView();
    }

    @Override
    public void getVisitedHistory(ValueCallback<String[]> callback) {
        if (mDelegate != null) {
            mDelegate.getVisitedHistory(callback);
        } else {
            super.getVisitedHistory(callback);
        }
    }

    @Override
    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public boolean onShowFileChooser(WebView webView, ValueCallback<Uri[]> filePathCallback,
                                     WebChromeClient.FileChooserParams fileChooserParams) {
        if (mOpenFileChooserCallback != null) {
            mOpenFileChooserCallback.openFileChooserCallBack(filePathCallback, fileChooserParams);
        }
        return true;
    }
}
