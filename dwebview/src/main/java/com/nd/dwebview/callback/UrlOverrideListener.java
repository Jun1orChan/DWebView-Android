package com.nd.dwebview.callback;

/**
 * @author Administrator
 */
public interface UrlOverrideListener {


    /**
     * 加载Url
     *
     * @param url
     */
    void onLoadUrl(String url);

    /**
     * 重载
     *
     * @param url
     */
    void shouldOverrideUrlLoadingHappened(String url);

    /**
     * 页面结束
     *
     * @param url
     */
    void onUrlPageFinishedHappened(String url);


}
