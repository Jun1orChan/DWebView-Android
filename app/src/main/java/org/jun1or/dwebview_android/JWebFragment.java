package org.jun1or.dwebview_android;

import android.os.Bundle;
import android.view.View;

import androidx.annotation.Nullable;

import org.jun1or.dwebview.fragment.WebFragment;


public class JWebFragment extends WebFragment {

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }

    public void reload() {
        mWebViewWrapper.getWebView().reload();
    }
}
