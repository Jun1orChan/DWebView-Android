package com.nd.dwebview.callback;

/**
 * @author du
 * @date 16/12/31
 */

public interface CompletionHandler<T> {

    /**
     * 完成
     *
     * @param retValue
     */
    void complete(T retValue);

    /**
     * 完成
     */
    void complete();

    /**
     * 进度
     *
     * @param value
     */
    void setProgressData(T value);
}
