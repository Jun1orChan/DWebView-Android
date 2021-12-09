package com.nd.dwebview.webview;

import android.app.Activity;
import android.content.Context;
import android.util.Log;
import android.webkit.JavascriptInterface;

import androidx.annotation.Keep;

import com.nd.dwebview.callback.CompletionHandler;
import com.nd.dwebview.callback.JavascriptCloseWindowListener;
import com.nd.dwebview.callback.OnReturnValue;

import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

/**
 * @author cwj
 */
public class InnerJavascriptInterface {

    private static final String TAG = "JSBridge_TAG";
    private boolean mIsDebug = false;
    private DWebView mDWebView;

    private Map<String, Object> mJavaScriptNamespaceInterfaceMap = new HashMap<>();

    public InnerJavascriptInterface(DWebView dWebView) {
        this.mDWebView = dWebView;
    }

    public void setDebug(boolean isDebug) {
        this.mIsDebug = isDebug;
    }

    private void PrintDebugInfo(String error) {
        Log.d(TAG, error);
        if (mIsDebug) {
            mDWebView.evaluateJavascript(String.format("alert('%s')", "DEBUG ERR MSG:\\n" + error.replaceAll("\\'", "\\\\'")));
        }
    }

    @Keep
    @JavascriptInterface
    public String call(String methodName, String argStr) {
        String error = "Js bridge  called, but can't find a corresponded " +
                "JavascriptInterface object , please check your code!";
        String[] nameStr = parseNamespace(methodName.trim());
        methodName = nameStr[1];
        Object jsb = mJavaScriptNamespaceInterfaceMap.get(nameStr[0]);
        JSONObject ret = new JSONObject();
        try {
            ret.put("code", -1);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        if (jsb == null) {
            PrintDebugInfo(error);
            return ret.toString();
        }
        Object arg = null;
        Method method = null;
        String callback = null;

        try {
            JSONObject args = new JSONObject(argStr);
            if (args.has("_dscbstub")) {
                callback = args.getString("_dscbstub");
            }
            if (args.has("data")) {
                arg = args.get("data");
            }
        } catch (Exception e) {
            error = String.format("The argument of \"%s\" must be a JSON object string!", methodName);
            PrintDebugInfo(error);
            e.printStackTrace();
            return ret.toString();
        }

        Class<?> cls = jsb.getClass();
        boolean asyn = false;
        try {
            method = cls.getMethod(methodName,
                    new Class[]{Object.class, CompletionHandler.class});
            asyn = true;
        } catch (Exception e) {
            try {
                method = cls.getMethod(methodName, new Class[]{Object.class});
            } catch (Exception ex) {

            }
        }

        if (method == null) {
            error = "Not find method \"" + methodName + "\" implementation! please check if the  signature or namespace of the method is right ";
            PrintDebugInfo(error);
            return ret.toString();
        }
        JavascriptInterface annotation = method.getAnnotation(JavascriptInterface.class);
        if (annotation == null) {
            error = "Method " + methodName + " is not invoked, since  " +
                    "it is not declared with JavascriptInterface annotation! ";
            PrintDebugInfo(error);
            return ret.toString();
        }
        Object retData;
        method.setAccessible(true);
        try {
            if (asyn) {
                final String cb = callback;
                method.invoke(jsb, arg, new CompletionHandler() {
                    @Override
                    public void complete(Object retValue) {
                        complete(retValue, true);
                    }

                    @Override
                    public void complete() {
                        complete(null, true);
                    }

                    @Override
                    public void setProgressData(Object value) {
                        complete(value, false);
                    }

                    private void complete(Object retValue, boolean complete) {
                        try {
                            JSONObject ret = new JSONObject();
                            ret.put("code", 0);
                            ret.put("data", retValue);
                            //retValue = URLEncoder.encode(ret.toString(), "UTF-8").replaceAll("\\+", "%20");
                            if (cb != null) {
                                //String script = String.format("%s(JSON.parse(decodeURIComponent(\"%s\")).data);", cb, retValue);
                                String script = String.format("%s(%s.data);", cb, ret.toString());
                                if (complete) {
                                    script += "delete window." + cb;
                                }
                                //Log.d(LOG_TAG, "complete " + script);
                                mDWebView.evaluateJavascript(script);
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                });
            } else {
                retData = method.invoke(jsb, arg);
                ret.put("code", 0);
                ret.put("data", retData);
                return ret.toString();
            }
        } catch (Exception e) {
            e.printStackTrace();
            error = String.format("Call failed：The parameter of \"%s\" in Java is invalid.", methodName);
            PrintDebugInfo(error);
            return ret.toString();
        }
        return ret.toString();
    }


    private String[] parseNamespace(String method) {
        int pos = method.lastIndexOf('.');
        String namespace = "";
        if (pos != -1) {
            namespace = method.substring(0, pos);
            method = method.substring(pos + 1);
        }
        return new String[]{namespace, method};
    }

    /**
     * Add a java object which implemented the javascript interfaces to dsBridge with namespace.
     * Remove the object using {@link #removeJavascriptObject(String) removeJavascriptObject(String)}
     *
     * @param object
     * @param namespace if empty, the object have no namespace.
     */
    public void addJavascriptObject(Object object, String namespace) {
        if (namespace == null) {
            namespace = "";
        }
        if (object != null) {
            mJavaScriptNamespaceInterfaceMap.put(namespace, object);
        }
    }

    /**
     * remove the javascript object with supplied namespace.
     *
     * @param namespace
     */
    public void removeJavascriptObject(String namespace) {
        if (namespace == null) {
            namespace = "";
        }
        mJavaScriptNamespaceInterfaceMap.remove(namespace);
    }


    class DefaultJavascriptInterface {

        private JavascriptCloseWindowListener mJavascriptCloseWindowListener;


        public void setJavascriptCloseWindowListener(JavascriptCloseWindowListener javascriptCloseWindowListener) {
            mJavascriptCloseWindowListener = javascriptCloseWindowListener;
        }

        @Keep
        @JavascriptInterface
        public boolean hasNativeMethod(Object args) throws JSONException {
            JSONObject jsonObject = (JSONObject) args;
            String methodName = jsonObject.getString("name").trim();
            String type = jsonObject.getString("type").trim();
            String[] nameStr = parseNamespace(methodName);
            Object jsb = mJavaScriptNamespaceInterfaceMap.get(nameStr[0]);
            if (jsb != null) {
                Class<?> cls = jsb.getClass();
                boolean asyn = false;
                Method method = null;
                try {
                    method = cls.getMethod(nameStr[1],
                            new Class[]{Object.class, CompletionHandler.class});
                    asyn = true;
                } catch (Exception e) {
//                    e.printStackTrace();
                    try {
                        method = cls.getMethod(nameStr[1], new Class[]{Object.class});
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }
                if (method != null) {
                    JavascriptInterface annotation = method.getAnnotation(JavascriptInterface.class);
                    if (annotation == null) {
                        return false;
                    }
                    if ("all".equals(type) || (asyn && "asyn".equals(type) || (!asyn && "syn".equals(type)))) {
                        return true;
                    }

                }
            }
            return false;
        }

        @Keep
        @JavascriptInterface
        public String closePage(Object object) throws JSONException {
            mDWebView.runOnMainThread(new Runnable() {
                @Override
                public void run() {
                    if (mJavascriptCloseWindowListener == null
                            || mJavascriptCloseWindowListener.onClose()) {
                        Context context = mDWebView.getContext();
                        if (context instanceof Activity) {
                            ((Activity) context).finish();
                        }
                    }
                }
            });
            return null;
        }

        @Keep
        @JavascriptInterface
        public void dsinit(Object jsonObject) {
            mDWebView.dispatchStartupQueue();
        }

        @Keep
        @JavascriptInterface
        public void returnValue(final Object obj) {
            mDWebView.runOnMainThread(new Runnable() {
                @Override
                public void run() {
                    JSONObject jsonObject = (JSONObject) obj;
                    Object data = null;
                    try {
                        int id = jsonObject.getInt("id");
                        boolean isCompleted = jsonObject.getBoolean("complete");
                        OnReturnValue handler = mDWebView.getHandleMap().get(id);
                        if (jsonObject.has("data")) {
                            data = jsonObject.get("data");
                        }
                        if (handler != null) {
                            handler.onValue(data);
                            if (isCompleted) {
                                mDWebView.getHandleMap().remove(id);
                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });
        }
    }
}