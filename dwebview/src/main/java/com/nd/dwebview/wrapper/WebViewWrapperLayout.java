package com.nd.dwebview.wrapper;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;

import com.nd.dwebview.R;
import com.nd.dwebview.callback.IWebLayout;
import com.nd.dwebview.callback.UrlOverrideListener;
import com.nd.dwebview.callback.WebViewActionHappenListener;
import com.nd.dwebview.webview.DWebView;

import java.util.HashSet;
import java.util.Set;

/**
 * @author cwj
 */
public class WebViewWrapperLayout extends FrameLayout implements View.OnClickListener, WebViewActionHappenListener,
        WebHorizontalProgressBar.OnProgressStopFinishedListener, UrlOverrideListener {


    private static final String ERROR_INTERNET_DISCONNECTED = "net::ERR_INTERNET_DISCONNECTED";
    private DWebView mWebView;
    private WebHorizontalProgressBar mWebHorizontalProgressBar;
    private LinearLayout mLlErrorContainer;
    protected IWebLayout mWebLayout;
    private Handler mMainHandler = new Handler(Looper.getMainLooper());

    /**
     * 缓存当前出现错误的页面
     */
    private Set<String> mErrorUrlsSet = new HashSet<>();

    /**
     * 缓存等待加载完成的页面 onPageStart()执行之后 ，onPageFinished()执行之前
     */
    private Set<String> mWaitingFinishSet = new HashSet<>();

    public WebViewWrapperLayout(@NonNull Context context) {
        this(context, null);
    }

    public WebViewWrapperLayout(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public WebViewWrapperLayout(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs, defStyleAttr);
    }

    private void init(Context context, AttributeSet attrs, int defStypeAttr) {
        //添加布局
        LayoutInflater.from(context).inflate(R.layout.dwebview_view_web_wrapper, this, true);
        mLlErrorContainer = findViewById(R.id.llErrorContainer);
        mLlErrorContainer.setOnClickListener(this);
        mWebHorizontalProgressBar = findViewById(R.id.progressBar);
        mWebHorizontalProgressBar.setOnProgressStopFinishedListener(this);
        if (mWebLayout == null) {
            mWebLayout = getWebLayout();
        }
        mWebView = mWebLayout.getWebView();
        mWebView.setWebViewActionHappenListener(this);
        mWebView.setUrlOverrideListener(this);
        addView(mWebLayout.getLayout(), 0,
                new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT));
    }

    protected IWebLayout getWebLayout() {
        return new DefaultWebLayout(getContext());
    }

    public WebHorizontalProgressBar getWebHorizontalProgressBar() {
        return mWebHorizontalProgressBar;
    }

    public DWebView getWebView() {
        return mWebView;
    }

    public void removeWebView() {
        if (mWebView == null) {
            return;
        }
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            if (mWebView.getParent() != null) {
                ((ViewGroup) mWebView.getParent()).removeView(mWebView);
            }
            mWebView.removeAllViews();
            mWebView.destroy();
        } else {
            mWebView.removeAllViews();
            mWebView.destroy();
            if (mWebView.getParent() != null) {
                ((ViewGroup) mWebView.getParent()).removeView(mWebView);
            }
        }
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.llErrorContainer) {
            mWebView.reload();
        }
    }

    private void setErrorStatus(String url) {
        mMainHandler.removeCallbacksAndMessages(null);
        mErrorUrlsSet.add(url);
        mLlErrorContainer.setVisibility(View.VISIBLE);
        mWebView.evaluateJavascript("javascript:document.body.innerHTML=\"\"");
    }


    private synchronized void hideErrorLayout() {
        mMainHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                mLlErrorContainer.setVisibility(View.GONE);
            }
        }, 300);
    }

    @Override
    public void onReceivedTitleHappened(String title) {
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public void onReceivedHttpErrorHappened(WebResourceRequest request, WebResourceResponse errorResponse) {
//        if (shouldIgnore(request)) {
//            return;
//        }
//        // 这个方法在6.0才出现
//        int statusCode = errorResponse.getStatusCode();
//        if (request.isForMainFrame()) {
//            if (statusCode < 200 || statusCode >= 400) {
//                setErrorStatus(request.getUrl() + "");
//            }
//        }
    }

//    private boolean shouldIgnore(WebResourceRequest request) {
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
//            String url = request.getUrl().toString();
//            if (url.contains(".ico") || url.contains(".json") || url.contains(".js") || url.contains(".css") || url.contains(".xml")) {
//                return true;
//            }
//        }
//        return false;
//    }

    /**
     * 6.0以上版本
     *
     * @param request
     * @param error
     */
    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public void onReceivedErrorHappened(WebResourceRequest request, WebResourceError error) {
        //避免内嵌iframe导致的显示错误界面
        if (request.isForMainFrame()) {
            // 显示错误界面
            setErrorStatus("" + request.getUrl());
        }
    }

    @Override
    public void onReceivedErrorHappened(int errorCode, String description, String failingUrl) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            return;
        }
        if (failingUrl.equals(mWebView.getUrl()) || description.equals(ERROR_INTERNET_DISCONNECTED)) {
            // 显示错误界面
            setErrorStatus(failingUrl);
        }
    }

    @Override
    public void onPageStartedHappened(String url, Bitmap favicon) {
        if (!mWaitingFinishSet.contains(url)) {
            mWaitingFinishSet.add(url);
        }
        mWebHorizontalProgressBar.start();
    }

    @Override
    public void onLoadUrl(String url) {

    }

    @Override
    public void shouldOverrideUrlLoadingHappened(String url) {
    }

    @Override
    public void onUrlPageFinishedHappened(String url) {
    }

    @Override
    public void onPageFinishedHappened(String url) {
        mWebHorizontalProgressBar.stop();
        if (!mErrorUrlsSet.contains(url) && mWaitingFinishSet.contains(url)) {
            hideErrorLayout();
        }
        if (mWaitingFinishSet.contains(url)) {
            mWaitingFinishSet.remove(url);
        }
        if (!mErrorUrlsSet.isEmpty()) {
            mErrorUrlsSet.clear();
        }
    }

    @Override
    public void onProgressStopFinished() {
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if (mMainHandler != null) {
            mMainHandler.removeCallbacksAndMessages(null);
        }
    }
}
