package org.cjl.spring.mvcframework.servlet.annotation;

import java.lang.annotation.*;

@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface CJLRequestParam {

    String value() default "";
}
