package org.cjl.spring.demo.aop.service.impl;

import org.cjl.spring.demo.aop.service.SuperMan;
import org.cjl.spring.mvcframework.servlet.annotation.*;

//切面注解
@CJLAspect
//自定义注解，交由IOC容器
@CJLComponent
public class Student implements SuperMan {

    @CJLAfter
    @CJLAfterReturning
    @CJLBefore
    @CJLAfterThrowing
    @Override
    public int add(int a, int b) {
        System.out.println("--> a + b = " + (a + b));
        return a + b;
    }

    @CJLAround
    @Override
    public int divide(int a, int b) {
        System.out.println("--> a/b = " + (a/b));
        return a/b;
    }
}
