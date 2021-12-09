package com.nd.dwebview_android;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebChromeClient;
import android.webkit.WebViewClient;

import androidx.annotation.Nullable;

import com.nd.dwebview.fragment.WebFragment;

/**
 * 可继承WebView实现各种自定义的操作
 *
 * @author cwj
 * @date 2021/10/19 15:08
 */
public class CustomWebFragment extends WebFragment {


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return super.onCreateView(inflater, container, savedInstanceState);
    }

    /**
     * 复写WebFragment中的WebViewClient
     *
     * @return
     */
    @Override
    public WebViewClient getWebViewClient() {
        return new CustomWebViewClient();
    }

    /**
     * 复写WebFragment中的WebChromeClient
     *
     * @return
     */
    @Override
    public WebChromeClient getWebChromeClient() {
        return new CustomWebChromeClient();
    }

    private class CustomWebViewClient extends WebFragment.DefaultWebViewClient {

    }

    private class CustomWebChromeClient extends WebFragment.DefaultWebChromeClient {

    }

    public void reload() {
        mWebViewWrapperLayout.getWebView().reload();
    }

}
