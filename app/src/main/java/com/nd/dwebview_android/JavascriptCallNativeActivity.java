package com.nd.dwebview_android;

import android.graphics.Color;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.nd.dwebview.fragment.WebFragment;


public class JavascriptCallNativeActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_js_call_native);
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction ft = fragmentManager.beginTransaction();
        AddJSObjectFragment addJSObjectFragment = new AddJSObjectFragment();
        Bundle bundle = new Bundle();
        bundle.putString(WebFragment.URL, "file:///android_asset/js-call-native.html");
        bundle.putInt(WebFragment.PROGRESSBAR_COLOR, Color.RED);
        addJSObjectFragment.setArguments(bundle);
        ft.add(R.id.flContainer, addJSObjectFragment);
        ft.commit();
    }
}
