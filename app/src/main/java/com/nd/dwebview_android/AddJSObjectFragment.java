package com.nd.dwebview_android;

import android.os.Bundle;
import android.view.View;

import androidx.annotation.Nullable;

import com.nd.dwebview.fragment.WebFragment;
import com.nd.dwebview.webview.DWebView;


/**
 * 测试添加项目自定义通信方法
 */
public class AddJSObjectFragment extends WebFragment {

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        DWebView.setWebContentsDebuggingEnabled(true);
//        mWebViewWrapperLayout.getWebView().addJavascriptObject(new JsApi2(), null);
//        mWebViewWrapperLayout.getWebView().addJavascriptObject(new JsEchoApi(), "echo");
    }
}
