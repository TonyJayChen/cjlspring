package org.cjl.spring.mvcframework.servlet.annotation;

import java.lang.annotation.*;

@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface CJLService {

    String value() default "";
}
