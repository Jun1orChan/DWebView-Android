package com.nd.dwebview.wrapper;

import android.content.Context;
import android.view.ViewGroup;

import com.nd.dwebview.callback.IWebLayout;
import com.nd.dwebview.webview.DWebView;

/**
 * @author cwj
 * @date 2021/10/19 18:31
 */
public class DefaultWebLayout implements IWebLayout {

    private Context mContext;
    private DWebView mWebView;

    public DefaultWebLayout(Context context) {
        this.mContext = context;
        mWebView = new DWebView(mContext);
    }

    @Override
    public ViewGroup getLayout() {
        return mWebView;
    }

    @Override
    public DWebView getWebView() {
        return mWebView;
    }
}
