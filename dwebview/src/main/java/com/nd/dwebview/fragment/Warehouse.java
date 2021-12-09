package com.nd.dwebview.fragment;

import java.util.HashMap;
import java.util.Map;

/**
 * 缓存 JSApi相关实体类
 *
 * @author cwj
 * @date 2021/10/14 14:02
 */
class Warehouse {

    /**
     * 大部分应用而言，可能JSApi不会很多，大部分是一个类就搞定，所以这边初始化为：4，节约内存
     */
    static Map<String, Class> JSAPI_ATLAS = new HashMap<>(4);
}
