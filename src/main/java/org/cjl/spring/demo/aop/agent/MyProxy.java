package org.cjl.spring.demo.aop.agent;

import org.cjl.spring.mvcframework.servlet.CJLDispatcherServlet;

import java.lang.reflect.Proxy;

public class MyProxy {

    /**
     * 动态的创建一个代理类对象
     * @param clazz
     * @return
     */
    public static Object getProxyInstance(Object clazz){
        MyInvocationHandler handler = new MyInvocationHandler();
        handler.setClazz(clazz);
        try {
            Object proxyInstance = Proxy.newProxyInstance(clazz.getClass().getClassLoader(), clazz.getClass().getInterfaces(), handler);
            return proxyInstance;
        }catch (Exception e){
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 对于有@InOutLog注解的，用代理类的bean来替代BeanFactory中的被代理类的bean。
     * 这一步很重要，因为当执行到bean.method()，执行的就一定是bean对应的method()方法，
     * 如果此时没有用代理类对象去替换，那么执行的就是没有InOutLog的原来的那个方法。
     */
    public static void updateBean(String completeClassName, Object clazz) {
        // (全类名，代理类的bean)
        CJLDispatcherServlet.updateBeanFromBeanFactory(completeClassName, getProxyInstance(clazz));
    }
}
