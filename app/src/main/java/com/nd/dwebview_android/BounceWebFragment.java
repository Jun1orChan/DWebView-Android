package com.nd.dwebview_android;

import android.os.Bundle;
import android.view.View;

import androidx.annotation.Nullable;

import com.nd.dwebview.fragment.WebFragment;
import com.nd.dwebview.wrapper.WebViewWrapperLayout;

/**
 * 回弹效果的WebView
 *
 * @author cwj
 * @date 2021/10/19 15:37
 */
public class BounceWebFragment extends WebFragment {

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }

    @Override
    public WebViewWrapperLayout getWebViewWrapper() {
        return new BounceWebViewWrapperLayout(getContext());
    }
}
