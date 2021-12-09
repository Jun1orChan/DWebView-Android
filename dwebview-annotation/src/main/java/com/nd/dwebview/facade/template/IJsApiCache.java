package com.nd.dwebview.facade.template;

import java.util.Map;

/**
 * @author cwj
 * @date 2021/10/14 14:14
 */
public interface IJsApiCache {

    /**
     * 填充atlas
     *
     * @param atlas
     */
    void loadInto(Map<String, Class> atlas);
}
