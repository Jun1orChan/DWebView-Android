package com.nd.dwebview_android;

import android.graphics.Color;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.nd.dwebview.fragment.WebFragment;

/**
 * @author cwj
 * @date 2021/10/19 15:53
 */
public class BounceWebActivity extends AppCompatActivity {

    BounceWebFragment mBounceWebFragment;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bounce_web);
        initViews();
    }

    private void initViews() {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction ft = fragmentManager.beginTransaction();
        mBounceWebFragment = new BounceWebFragment();
        Bundle bundle = new Bundle();
        bundle.putString(WebFragment.URL, getIntent().getStringExtra("url"));
        bundle.putInt(WebFragment.PROGRESSBAR_COLOR, Color.RED);
        mBounceWebFragment.setArguments(bundle);
        ft.add(R.id.flContainer, mBounceWebFragment);
        ft.commit();
    }


    @Override
    public void onBackPressed() {
        if (mBounceWebFragment.canGoBack()) {
            mBounceWebFragment.goBack();
        } else {
            super.onBackPressed();
        }
    }
}
