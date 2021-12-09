package com.nd.dwebview.callback;

/**
 * @author Administrator
 */
public interface JavascriptCloseWindowListener {
    /**
     * JS调用window.close
     *
     * @return If true, close the current activity, otherwise, do nothing.
     */
    boolean onClose();
}