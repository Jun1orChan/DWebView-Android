package com.nd.dwebview.webview;

import android.app.Activity;
import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.util.AttributeSet;
import android.util.Log;
import android.util.Pair;
import android.view.KeyEvent;
import android.view.View;
import android.webkit.DownloadListener;
import android.webkit.WebBackForwardList;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.nd.dwebview.callback.IVideo;
import com.nd.dwebview.callback.JavascriptCloseWindowListener;
import com.nd.dwebview.callback.OnReturnValue;
import com.nd.dwebview.callback.OpenFileChooserCallback;
import com.nd.dwebview.callback.UrlOverrideListener;
import com.nd.dwebview.callback.WebViewActionHappenListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author Junior
 */
public class DWebView extends WebView implements DownloadListener {


    public static final String ERROR_URL_START_DATA = "data:text/html";
    public static final String ERROR_URL_START_ABOUT_BLANK = "about:blank";

    private static final String BRIDGE_NAME = "_dsbridge";

    private DownloadListener mDownloadListener;

    private static boolean sIsDebug = false;
    private InnerJavascriptInterface mInnerJavascriptInterface = null;
    private InnerJavascriptInterface.DefaultJavascriptInterface mDefaultJavascriptInterface;

    private JavascriptCloseWindowListener mJavascriptCloseWindowListener;

    private UrlOverrideListener mUrlOverrideListener;

    private Handler mMainHandler = new Handler(Looper.getMainLooper());

    private List<CallInfo> mCallInfoList;

    private Map<Integer, OnReturnValue> mHandleMap = new ConcurrentHashMap<>();

    private int mCallID = 0;

    private boolean mIsInited;

    private FixedOnReceivedTitle mFixedOnReceivedTitle;

    private IVideo mVideoImpl;

    private AgentWebChromeClient mAgentWebChromeClient;
    private AgentWebClient mAgentWebClient;


    public DWebView(Context context) {
        super(getFixedContext(context));
        init();
    }

    public DWebView(Context context, AttributeSet attrs) {
        super(getFixedContext(context), attrs);
        init();
    }

    public DWebView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(getFixedContext(context), attrs, defStyleAttr);
        init();
    }

    private static Context getFixedContext(Context context) {
        if (Build.VERSION.SDK_INT >= 21 && Build.VERSION.SDK_INT < 23) {
            // Avoid  on Androicrashingd 5 and 6 (API level 21 to 23)
            return context.createConfigurationContext(new Configuration());
        }
        return context;
    }


    private void init() {
        mInnerJavascriptInterface = new InnerJavascriptInterface(this);
        mAgentWebChromeClient = new AgentWebChromeClient(this);
        mAgentWebClient = new AgentWebClient(this);
        if (getContext() instanceof Activity) {
            mVideoImpl = new VideoImpl((Activity) getContext(), this);
            mAgentWebChromeClient.setVideoImpl(mVideoImpl);
        }
        super.setDownloadListener(this);
        mDefaultJavascriptInterface = mInnerJavascriptInterface.new DefaultJavascriptInterface();
        mInnerJavascriptInterface.setDebug(sIsDebug);
        mInnerJavascriptInterface.addJavascriptObject(mDefaultJavascriptInterface, "_dsb");
        super.addJavascriptInterface(mInnerJavascriptInterface, BRIDGE_NAME);
        //移除有风险的WebView系统隐藏接口漏洞
        removeJavascriptInterface("searchBoxJavaBridge_");
        removeJavascriptInterface("accessibility");
        removeJavascriptInterface("accessibilityTraversal");
        mIsInited = true;
        mFixedOnReceivedTitle = new FixedOnReceivedTitle();
    }

    @Override
    public boolean canGoBack() {
        return super.canGoBack();
    }

    @Override
    public void goBack() {
        super.goBack();
    }

    @Override
    public void loadUrl(final String url) {
        if (url != null && url.startsWith("javascript:")) {
            DWebView.super.loadUrl(url);
            return;
        }
        loadUrl(url, null);
    }

    @Override
    public void loadUrl(@NonNull String url, @NonNull Map<String, String> additionalHttpHeaders) {
//        super.loadUrl(url, additionalHttpHeaders);
        if (mUrlOverrideListener != null) {
            mUrlOverrideListener.onLoadUrl(url);
        }
        runOnMainThread(new Runnable() {
            @Override
            public void run() {
                mCallInfoList = Collections.synchronizedList(new ArrayList<>());
                DWebView.super.loadUrl(url, additionalHttpHeaders);
            }
        });
    }

    public void setOpenFileChooserCallback(OpenFileChooserCallback openFileChooserCallback) {
        this.mAgentWebChromeClient.setOpenFileChooserCallback(openFileChooserCallback);
    }

    public void setUrlOverrideListener(UrlOverrideListener urlOverrideListener) {
        this.mUrlOverrideListener = urlOverrideListener;
    }

    @Override
    public void setWebViewClient(WebViewClient webViewClient) {
        mAgentWebClient.setDelegate(webViewClient);
        mAgentWebClient.setUrlOverrideListener(mUrlOverrideListener);
        super.setWebViewClient(mAgentWebClient);
    }

    @Override
    public void setWebChromeClient(WebChromeClient webChromeClient) {
        mAgentWebChromeClient.setDelegate(webChromeClient);
        mFixedOnReceivedTitle.setWebChromeClient(webChromeClient);
        super.setWebChromeClient(mAgentWebChromeClient);
    }

    public void setWebViewActionHappenListener(WebViewActionHappenListener webViewActionHappenListener) {
        mAgentWebChromeClient.setWebViewActionHappenListener(webViewActionHappenListener);
        mAgentWebClient.setWebViewActionHappenListener(webViewActionHappenListener);
    }

    @Override
    public void setDownloadListener(DownloadListener downloadListener) {
        this.mDownloadListener = downloadListener;
    }

    public void setJavascriptCloseWindowListener(JavascriptCloseWindowListener javascriptCloseWindowListener) {
        this.mJavascriptCloseWindowListener = javascriptCloseWindowListener;
        mDefaultJavascriptInterface.setJavascriptCloseWindowListener(mJavascriptCloseWindowListener);
    }

    public static void setWebContentsDebuggingEnabled(boolean enabled) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            WebView.setWebContentsDebuggingEnabled(enabled);
        }
        sIsDebug = enabled;
    }

    public void addJavascriptObject(Object object, String namespace) {
        mInnerJavascriptInterface.addJavascriptObject(object, namespace);
    }

    /**
     * remove the javascript object with supplied namespace.
     *
     * @param namespace
     */
    public void removeJavascriptObject(String namespace) {
        mInnerJavascriptInterface.removeJavascriptObject(namespace);
    }


    protected Map<Integer, OnReturnValue> getHandleMap() {
        return mHandleMap;
    }


    public void evaluateJavascript(final String script) {
        runOnMainThread(new Runnable() {
            @Override
            public void run() {
                _evaluateJavascript(script);
            }
        });
    }

    private void _evaluateJavascript(String script) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            super.evaluateJavascript(script, null);
        } else {
            super.loadUrl("javascript:" + script);
        }
    }


    protected synchronized void dispatchStartupQueue() {
        if (mCallInfoList != null) {
            for (CallInfo info : mCallInfoList) {
                dispatchJavascriptCall(info);
            }
            mCallInfoList = null;
        }
    }

    private void dispatchJavascriptCall(CallInfo info) {
        evaluateJavascript(String.format("window._handleMessageFromNative(%s)", info.toString()));
    }


    public synchronized <T> void callHandler(String method, Object[] args, final OnReturnValue<T> handler) {

        CallInfo callInfo = new CallInfo(method, mCallID, args);
        if (handler != null) {
            mHandleMap.put(mCallID++, handler);
        }
        if (mCallInfoList != null) {
            mCallInfoList.add(callInfo);
        } else {
            dispatchJavascriptCall(callInfo);
        }
    }

    public void callHandler(String method, Object[] args) {
        callHandler(method, args, null);
    }

    public <T> void callHandler(String method, OnReturnValue<T> handler) {
        callHandler(method, null, handler);
    }

    protected void runOnMainThread(Runnable runnable) {
        if (Looper.getMainLooper() == Looper.myLooper()) {
            runnable.run();
            return;
        }
        mMainHandler.post(runnable);
    }

    public void hasJavascriptMethod(String handlerName, OnReturnValue<Boolean> existCallback) {
        callHandler("_hasJavascriptMethod", new Object[]{handlerName}, existCallback);
    }

    @Override
    public void onDownloadStart(String url, String userAgent, String contentDisposition, String mimetype, long contentLength) {
        if (mDownloadListener != null) {
            mDownloadListener.onDownloadStart(url, userAgent, contentDisposition, mimetype, contentLength);
        }
    }

    @Override
    public void setOverScrollMode(int mode) {
        try {
            super.setOverScrollMode(mode);
        } catch (Throwable e) {
            Pair<Boolean, String> pair = isWebViewPackageException(e);
            if (pair.first) {
                Toast.makeText(getContext(), pair.second, Toast.LENGTH_SHORT).show();
                destroy();
            } else {
                throw e;
            }
        }
    }

    @Override
    public void clearHistory() {
        if (mIsInited) {
            super.clearHistory();
        }
    }

    @Override
    public void destroy() {
        setVisibility(View.GONE);
        removeAllViewsInLayout();
        if (mIsInited) {
            super.destroy();
        }
    }

    private static Pair<Boolean, String> isWebViewPackageException(Throwable e) {
        String messageCause = e.getCause() == null ? e.toString() : e.getCause().toString();
        String trace = Log.getStackTraceString(e);
        if (trace.contains("android.content.pm.PackageManager$NameNotFoundException")
                || trace.contains("java.lang.RuntimeException: Cannot load WebView")
                || trace.contains("android.webkit.WebViewFactory$MissingWebViewPackageException: Failed to load WebView provider: No WebView installed")) {
            return new Pair<Boolean, String>(true, "WebView load failed, " + messageCause);
        }
        return new Pair<Boolean, String>(false, messageCause);
    }

    /**
     * 解决部分手机webView返回时不触发onReceivedTitle的问题（如：三星SM-G9008V 4.4.2）；
     */
    private static class FixedOnReceivedTitle {
        private WebChromeClient mWebChromeClient;
        private boolean mIsOnReceivedTitle;

        public void setWebChromeClient(WebChromeClient webChromeClient) {
            mWebChromeClient = webChromeClient;
        }

        public void onPageStarted() {
            mIsOnReceivedTitle = false;
        }

        public void onPageFinished(WebView view) {
            if (!mIsOnReceivedTitle && mWebChromeClient != null) {
                WebBackForwardList list = null;
                try {
                    list = view.copyBackForwardList();
                } catch (NullPointerException e) {
                    e.printStackTrace();
                }
                if (list != null
                        && list.getSize() > 0
                        && list.getCurrentIndex() >= 0
                        && list.getItemAtIndex(list.getCurrentIndex()) != null) {
                    String previousTitle = list.getItemAtIndex(list.getCurrentIndex()).getTitle();
                    mWebChromeClient.onReceivedTitle(view, previousTitle);
                }
            }
        }

        public void onReceivedTitle() {
            mIsOnReceivedTitle = true;
        }
    }


    public static class AgentWebChromeClient extends WebChromeClientDelegate {

        private DWebView mAgentWebView;

        private AgentWebChromeClient(DWebView agentWebView) {
            this.mAgentWebView = agentWebView;
        }

        @Override
        public void onReceivedTitle(WebView view, String title) {
            this.mAgentWebView.mFixedOnReceivedTitle.onReceivedTitle();
            super.onReceivedTitle(view, title);
        }

    }

    public static class AgentWebClient extends WebViewClientDelegate {

        private DWebView mAgentWebView;

        private AgentWebClient(DWebView agentWebView) {
            this.mAgentWebView = agentWebView;
        }

        @Override
        public void onPageStarted(WebView view, String url, Bitmap favicon) {
            super.onPageStarted(view, url, favicon);
            mAgentWebView.mFixedOnReceivedTitle.onPageStarted();
        }

        @Override
        public void onPageFinished(WebView view, String url) {
            super.onPageFinished(view, url);
            mAgentWebView.mFixedOnReceivedTitle.onPageFinished(view);
        }
    }

    /**
     * 视频播放期间，回退按钮的处理
     *
     * @param keyCode
     * @param event
     * @return
     */
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (mVideoImpl != null) {
            if (keyCode == KeyEvent.KEYCODE_BACK) {
                if (mVideoImpl.isVideoState()) {
                    mVideoImpl.onHideCustomView();
                    return true;
                }
            }
        }
        return super.onKeyDown(keyCode, event);
    }


    @Override
    public boolean isPrivateBrowsingEnabled() {
        if (Build.VERSION.SDK_INT == Build.VERSION_CODES.ICE_CREAM_SANDWICH_MR1
                && getSettings() == null) {
            return false; // getSettings().isPrivateBrowsingEnabled()
        } else {
            return super.isPrivateBrowsingEnabled();
        }
    }
}
