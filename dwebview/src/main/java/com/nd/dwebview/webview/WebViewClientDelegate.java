package com.nd.dwebview.webview;

import android.graphics.Bitmap;
import android.net.http.SslError;
import android.os.Build;
import android.os.Message;
import android.view.KeyEvent;
import android.webkit.ClientCertRequest;
import android.webkit.HttpAuthHandler;
import android.webkit.SslErrorHandler;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;

import com.nd.dwebview.callback.UrlOverrideListener;
import com.nd.dwebview.callback.WebViewActionHappenListener;

/**
 * @author cwj
 */
public class WebViewClientDelegate extends WebViewClient {

    private WebViewClient mDelegate;
    private WebViewActionHappenListener mWebViewActionHappenListener;

    private UrlOverrideListener mUrlOverrideListener;

    public void setDelegate(WebViewClient delegate) {
        this.mDelegate = delegate;
    }

    public void setWebViewActionHappenListener(WebViewActionHappenListener webViewActionHappenListener) {
        this.mWebViewActionHappenListener = webViewActionHappenListener;
    }

    public void setUrlOverrideListener(UrlOverrideListener urlOverrideListener) {
        this.mUrlOverrideListener = urlOverrideListener;
    }

    @Override
    public boolean shouldOverrideUrlLoading(WebView view, String url) {
        if (mUrlOverrideListener != null) {
            mUrlOverrideListener.shouldOverrideUrlLoadingHappened(url);
        }
        if (mDelegate != null) {
            return mDelegate.shouldOverrideUrlLoading(view, url);
        }
        return super.shouldOverrideUrlLoading(view, url);
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
        return shouldOverrideUrlLoading(view, request.getUrl().toString());
    }

    @Override
    public void onPageStarted(WebView view, String url, Bitmap favicon) {
        if (url.startsWith(DWebView.ERROR_URL_START_ABOUT_BLANK) || url.startsWith(DWebView.ERROR_URL_START_DATA)) {
            super.onPageStarted(view, url, favicon);
            return;
        }
        if (mWebViewActionHappenListener != null) {
            mWebViewActionHappenListener.onPageStartedHappened(url, favicon);
        }
        if (mDelegate != null) {
            mDelegate.onPageStarted(view, url, favicon);
            return;
        }
    }

    @Override
    public void onPageFinished(WebView view, String url) {
        if (url.startsWith(DWebView.ERROR_URL_START_ABOUT_BLANK) || url.startsWith(DWebView.ERROR_URL_START_DATA)) {
            super.onPageFinished(view, url);
            return;
        }
        if (mWebViewActionHappenListener != null) {
            mWebViewActionHappenListener.onPageFinishedHappened(url);
        }
        if (mUrlOverrideListener != null) {
            mUrlOverrideListener.onUrlPageFinishedHappened(url);
        }
        if (mDelegate != null) {
            mDelegate.onPageFinished(view, url);
            return;
        }
        super.onPageFinished(view, url);
    }

    @Override
    public void onLoadResource(WebView view, String url) {
        if (mDelegate != null) {
            mDelegate.onLoadResource(view, url);
            return;
        }
        super.onLoadResource(view, url);
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public void onPageCommitVisible(WebView view, String url) {
        if (mDelegate != null) {
            mDelegate.onPageCommitVisible(view, url);
            return;
        }
        super.onPageCommitVisible(view, url);
    }

    @Nullable
    @Override
    public WebResourceResponse shouldInterceptRequest(WebView view, String url) {
//        Log.e("TAG", "before 21===========" + url);
        if (mDelegate != null) {
            return mDelegate.shouldInterceptRequest(view, url);
        }
        return super.shouldInterceptRequest(view, url);
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Nullable
    @Override
    public WebResourceResponse shouldInterceptRequest(WebView view, WebResourceRequest request) {
//        Log.e("TAG", "after 21===========" + request);
        if (mDelegate != null) {
            return mDelegate.shouldInterceptRequest(view, request);
        }
        return super.shouldInterceptRequest(view, request);
    }

    @Override
    public void onTooManyRedirects(WebView view, Message cancelMsg, Message continueMsg) {
        if (mDelegate != null) {
            mDelegate.onTooManyRedirects(view, cancelMsg, continueMsg);
            return;
        }
        super.onTooManyRedirects(view, cancelMsg, continueMsg);
    }

    @Override
    public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
        if (mWebViewActionHappenListener != null) {
            mWebViewActionHappenListener.onReceivedErrorHappened(errorCode, description, failingUrl);
        }
        if (mDelegate != null) {
            mDelegate.onReceivedError(view, errorCode, description, failingUrl);
            return;
        }
        super.onReceivedError(view, errorCode, description, failingUrl);
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public void onReceivedError(WebView view, WebResourceRequest request, WebResourceError error) {
        if (mWebViewActionHappenListener != null) {
            mWebViewActionHappenListener.onReceivedErrorHappened(request, error);
        }
        if (mDelegate != null) {
            mDelegate.onReceivedError(view, request, error);
            return;
        }
        super.onReceivedError(view, request, error);
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public void onReceivedHttpError(WebView view, WebResourceRequest request, WebResourceResponse errorResponse) {
        if (mWebViewActionHappenListener != null) {
            mWebViewActionHappenListener.onReceivedHttpErrorHappened(request, errorResponse);
        }
        if (mDelegate != null) {
            mDelegate.onReceivedHttpError(view, request, errorResponse);
            return;
        }
        super.onReceivedHttpError(view, request, errorResponse);
    }

    @Override
    public void onFormResubmission(WebView view, Message dontResend, Message resend) {
        if (mDelegate != null) {
            mDelegate.onFormResubmission(view, dontResend, resend);
            return;
        }
        super.onFormResubmission(view, dontResend, resend);
    }

    @Override
    public void doUpdateVisitedHistory(WebView view, String url, boolean isReload) {
        if (mDelegate != null) {
            mDelegate.doUpdateVisitedHistory(view, url, isReload);
            return;
        }
        super.doUpdateVisitedHistory(view, url, isReload);
    }

    @Override
    public void onReceivedSslError(WebView view, SslErrorHandler handler, SslError error) {
        if (mDelegate != null) {
            mDelegate.onReceivedSslError(view, handler, error);
            return;
        }
        super.onReceivedSslError(view, handler, error);
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    public void onReceivedClientCertRequest(WebView view, ClientCertRequest request) {
        if (mDelegate != null) {
            mDelegate.onReceivedClientCertRequest(view, request);
            return;
        }
        super.onReceivedClientCertRequest(view, request);
    }

    @Override
    public void onReceivedHttpAuthRequest(WebView view, HttpAuthHandler handler, String host, String realm) {
        if (mDelegate != null) {
            mDelegate.onReceivedHttpAuthRequest(view, handler, host, realm);
            return;
        }
        super.onReceivedHttpAuthRequest(view, handler, host, realm);
    }

    @Override
    public boolean shouldOverrideKeyEvent(WebView view, KeyEvent event) {
        if (mDelegate != null) {
            return mDelegate.shouldOverrideKeyEvent(view, event);
        }
        return super.shouldOverrideKeyEvent(view, event);
    }

    @Override
    public void onUnhandledKeyEvent(WebView view, KeyEvent event) {
        if (mDelegate != null) {
            mDelegate.onUnhandledKeyEvent(view, event);
            return;
        }
        super.onUnhandledKeyEvent(view, event);
    }

    @Override
    public void onScaleChanged(WebView view, float oldScale, float newScale) {
        if (mDelegate != null) {
            mDelegate.onScaleChanged(view, oldScale, newScale);
            return;
        }
        super.onScaleChanged(view, oldScale, newScale);
    }

    @Override
    public void onReceivedLoginRequest(WebView view, String realm, @Nullable String account, String args) {
        if (mDelegate != null) {
            mDelegate.onReceivedLoginRequest(view, realm, account, args);
            return;
        }
        super.onReceivedLoginRequest(view, realm, account, args);
    }
}
