package com.nd.dwebview.callback;

/**
 * @author du
 * @date 16/12/31
 */

public interface OnReturnValue<T> {
    /**
     * 返回值
     *
     * @param retValue
     */
    void onValue(T retValue);
}
