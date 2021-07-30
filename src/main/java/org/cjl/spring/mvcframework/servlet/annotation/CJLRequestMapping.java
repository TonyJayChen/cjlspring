package org.cjl.spring.mvcframework.servlet.annotation;

import java.lang.annotation.*;

@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface CJLRequestMapping {

    String value() default "";
}
