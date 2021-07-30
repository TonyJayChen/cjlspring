package org.cjl.spring.mvcframework.servlet.annotation;

import java.lang.annotation.*;

@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface CJLAutowird {

    String value() default "";
}
