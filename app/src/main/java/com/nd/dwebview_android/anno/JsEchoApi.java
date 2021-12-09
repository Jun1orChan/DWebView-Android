package com.nd.dwebview_android.anno;

import android.util.Log;
import android.webkit.JavascriptInterface;

import com.nd.dwebview.callback.CompletionHandler;
import com.nd.dwebview.facade.annotations.JsApi;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * @author Administrator
 */
@JsApi(nameSpace = "echo")
public class JsEchoApi {

    @JavascriptInterface
    public Object syn1(Object a) throws JSONException {
        Log.e("TAG", "syn1=====>args:NULL" + a);
        return new JSONObject();
    }

    @JavascriptInterface
    public Object syn(Object args) throws JSONException {
        Log.e("TAG", "args:" + args);
        return args;
    }

    @JavascriptInterface
    public void asyn(Object args, CompletionHandler handler) {
        handler.complete(args);
    }
}