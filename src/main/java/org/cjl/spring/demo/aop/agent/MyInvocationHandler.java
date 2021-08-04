package org.cjl.spring.demo.aop.agent;

import org.cjl.spring.mvcframework.servlet.annotation.*;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

public class MyInvocationHandler implements InvocationHandler {

    //被代理类
    private Object clazz;
    private Object invoke;

    public void setClazz(Object clazz) {
        this.clazz = clazz;
    }

    /**
     * 动态代理：实现环绕通知，前置通知，后置通知，返回通知，异常通知
     * @param proxy
     * @param method
     * @param args
     * @return
     * @throws Throwable
     */
    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        //入参的类型的处理，返回被代理对象真正要执行的方法
        Method declaredMethod = handleArgs(method);
        //环绕通知
        Boolean bool =false;
        if(null != declaredMethod.getAnnotation(CJLAround.class)){
            bool = true;
        }
        aroundInform(declaredMethod,bool,method,args);
        //前置通知、后置通知、返回通知、异常通知
        try {
            if(null != declaredMethod.getAnnotation(CJLBefore.class)){
                System.out.println(declaredMethod.getName() + " begings with : " + declaredMethod.getParameters());
            }
            //通过放射，真正执行被代理对象的方法
            invoke = method.invoke(clazz,args);
            if(null!=declaredMethod.getAnnotation(CJLAfterReturning.class)){
                System.out.println(declaredMethod.getName() + "  ends with : " + invoke);
            }
        }catch (Exception e){
            if(null!=declaredMethod.getAnnotation(CJLAfterThrowing.class)){
                System.out.println(declaredMethod.getName() + " occurs exception : " + e);
            }
        }finally {
            if(null!=declaredMethod.getAnnotation(CJLAfter.class)){
                System.out.println(declaredMethod.getName() + " ends.");
            }
        }
        return invoke;
    }


    /**
     * 入参的类型的处理，这个方法很重要。
     * @return 被代理对象真正要执行的那个方法
     * @param method 被代理对象的接口中声明的被代理方法
     * @throws NoSuchMethodException
     * @throws SecurityException
     */
    private Method handleArgs(Method method) throws NoSuchMethodException {
        Class<?>[] parameterTypes = method.getParameterTypes();
        switch (parameterTypes.length) {
            case 1:
                System.out.println("parameterTypes.length = 1 : " + parameterTypes[0]);
                return clazz.getClass().getDeclaredMethod(method.getName(), parameterTypes[0]);
            case 2:
                System.out.println("parameterTypes.length = 2 : " + parameterTypes[0] + " ; " + parameterTypes[1]);
                return clazz.getClass().getDeclaredMethod(method.getName(), parameterTypes[0], parameterTypes[1]);
            case 3:
                System.out.println("parameterTypes.length = 3 : " + parameterTypes[0] + " ; " + parameterTypes[1] + " ; "
                        + parameterTypes[2]);
                return clazz.getClass().getDeclaredMethod(method.getName(), parameterTypes[0], parameterTypes[1],
                        parameterTypes[2]);
            default:
                System.out.println("parameterTypes.length = 0 : " + parameterTypes.length);
                return clazz.getClass().getDeclaredMethod(method.getName());
            }
    }
    /**
     * 环绕通知
     *
     * @param declaredMethod 被代理对象的被代理方法
     * @param bool
     * @param method         被代理对象的接口中声明的被代理方法
     * @param args           被代理方法的声明的入参
     */
    private void aroundInform(Method declaredMethod, Boolean bool, Method method, Object[] args) {
        if (bool) {
            try {
                System.out.println(declaredMethod.getName() + " begings with : " + declaredMethod.getParameters());
                invoke = method.invoke(clazz, args);
                System.out.println(declaredMethod.getName() + "  ends with : " + invoke);
            } catch (Exception e) {
                System.out.println(declaredMethod.getName() + " occurs exception : " + e);
            } finally {
                System.out.println(declaredMethod.getName() + " ends.");
            }
        }
    }
}
