package org.cjl.spring.mvcframework.servlet.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 扫描BeanFactory，找出方法上有@Aspect注解的bean，为其创建代理类对象，并替代原bean。
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface CJLAspect {
}
