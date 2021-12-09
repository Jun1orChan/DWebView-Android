package com.nd.dwebview.facade.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author cwj
 * @date 2021/10/14 11:33
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.CLASS)
public @interface JsApi {

    /**
     * JSApi对应的 namespace
     *
     * @return nameSpace
     */
    String nameSpace() default "";
}
