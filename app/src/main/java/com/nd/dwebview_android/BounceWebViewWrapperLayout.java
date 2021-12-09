package com.nd.dwebview_android;

import android.content.Context;
import android.net.Uri;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.nd.dwebview.callback.IWebLayout;
import com.nd.dwebview.utils.WebViewUtil;
import com.nd.dwebview.webview.DWebView;
import com.nd.dwebview.wrapper.WebViewWrapperLayout;

/**
 * @author cwj
 * @date 2021/10/19 18:46
 */
public class BounceWebViewWrapperLayout extends WebViewWrapperLayout implements IWebLayout {

    private FrameLayout mViewGroup;
    private DWebView mWebView;

    public BounceWebViewWrapperLayout(@NonNull Context context) {
        super(context);
    }

    public BounceWebViewWrapperLayout(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public BounceWebViewWrapperLayout(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected IWebLayout getWebLayout() {
        mViewGroup = (FrameLayout) LayoutInflater.from(getContext()).inflate(R.layout.view_twk_web, null);
        mWebView = mViewGroup.findViewById(R.id.webview);
        return this;
    }


    @Override
    public void onLoadUrl(String url) {
        super.onLoadUrl(url);
        shouldOverrideUrlLoadingHappened(url);
    }

    @Override
    public void shouldOverrideUrlLoadingHappened(String url) {
        super.shouldOverrideUrlLoadingHappened(url);
        String host = getUrlHost(url);
        ((TextView) mViewGroup.findViewById(R.id.tvFlag)).setText("网页由" + host + "提供");
    }

    private String getUrlHost(String url) {
        if (TextUtils.isEmpty(url)) {
            return null;
        }
        Uri uri = Uri.parse(url);
        return uri.getHost();
    }

    @Override
    public ViewGroup getLayout() {
        return mViewGroup;
    }

    @Override
    public DWebView getWebView() {
        WebViewUtil.generalSetting(mWebView);
        return mWebView;
    }
}
