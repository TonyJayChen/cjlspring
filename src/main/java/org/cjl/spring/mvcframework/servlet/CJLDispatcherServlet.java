package org.cjl.spring.mvcframework.servlet;

import org.cjl.spring.mvcframework.servlet.annotation.CJLAutowird;
import org.cjl.spring.mvcframework.servlet.annotation.CJLController;
import org.cjl.spring.mvcframework.servlet.annotation.CJLRequestMapping;
import org.cjl.spring.mvcframework.servlet.annotation.CJLService;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.*;
import java.util.stream.Stream;

/**
 * @author cjl
 */
public class CJLDispatcherServlet extends HttpServlet {

    //2 IOC容器初始化
    private Map<String,Object> Ioc = new HashMap<>();

    private Properties contextCofig = new Properties();

    private  List<String> classNames = new ArrayList<>();

    private Map<String, Method> handlerMappng = new HashMap<>();

    @Override
    public void init(ServletConfig config) throws ServletException {
        //1 加载配置文件
        doLoadConfig(config.getInitParameter("contextConfigLocation"));
        //3 扫描相关的类
        doScannet(contextCofig.getProperty("scanPackage"));
        //4 创建实例化并保存至IOC容器
        doInstance();
        //5 完成依赖注入(DI)
        doAutowired();
        //6 初始化HandlerMapping
        doInitHandlerMapping();

        System.out.println("Cjl Spring init Ok!!!");
    }

    private void doInitHandlerMapping() {
        if(Ioc.isEmpty()){
            return;
        }
        Ioc.entrySet().stream().forEach(entry->{
            Class<?> clazz = entry.getValue().getClass();
            if(!clazz.isAnnotationPresent(CJLController.class)){
                return;
            }
            String bsaeUrl = "";
            if(clazz.isAnnotationPresent(CJLRequestMapping.class)){
                CJLRequestMapping cjlRequestMapping = clazz.getAnnotation(CJLRequestMapping.class);
                bsaeUrl = cjlRequestMapping.value();
            }
            //拿到所有public方法
            for (Method method: clazz.getMethods()) {
                if(!method.isAnnotationPresent(CJLRequestMapping.class)){
                    continue;
                }
                CJLRequestMapping requestMapping = method.getAnnotation(CJLRequestMapping.class);
                String url = ("/" + bsaeUrl + "/" + requestMapping.value()).replaceAll("/+","/");
                handlerMappng.put(url,method);
                System.out.println("Mapped:"+url+","+method);
            }
        });
    }

    private void doAutowired() {
        if(Ioc.isEmpty()){
            return;
        }
        Ioc.entrySet().forEach(entry->{
            //public、private、protected、default
            Field [] fields = entry.getValue().getClass().getDeclaredFields();
            Arrays.stream(fields).forEach(field -> {
                if(!field.isAnnotationPresent(CJLAutowird.class)){
                    return;
                }
                CJLAutowird autowird = field.getAnnotation(CJLAutowird.class);
                String beanName = autowird.value().trim();
                if("".equals(beanName)){
                    beanName = field.getType().getName();
                }
                //设置访问权限，实行暴力访问
                field.setAccessible(true);
                try {
                    field.set(entry.getValue(),Ioc.get(beanName));
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
            });
        });
    }

    private void doInstance() {
        if(classNames.isEmpty()){
            return;
        }
        classNames.forEach(className->{
            try {
                Class<?> clazz = Class.forName(className);
                if(clazz.isAnnotationPresent(CJLController.class)){
                    //1、默认规则，类名首字母小写
                    String beanName = toLoweFistCase(clazz.getSimpleName());
                    Object instance = clazz.newInstance();
                    Ioc.put(beanName,instance);
                }else if(clazz.isAnnotationPresent(CJLService.class)){
                    //1、默认规则，类名首字母小写
                    String beanName = toLoweFistCase(clazz.getSimpleName());
                    //2、自定义类名
                    CJLService service = clazz.getAnnotation(CJLService.class);
                    if("".equals(service.value())){
                        beanName = service.value();
                    }
                    Object instance = clazz.newInstance();
                    Ioc.put(beanName,instance);
                    //3.接口注入
                    Arrays.stream(clazz.getInterfaces()).forEach(azz->{
                        if(Ioc.containsKey(azz.getName())){
                            try {
                                throw new Exception("The beanName is exists!!");
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                        Ioc.put(azz.getName(),instance);
                    });
                }else {
                    return;
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    /**
     * 该方法如果首字母不是大写英文或不按常规取名会出现问题
     * @param simpleName
     * @return
     */
    private String toLoweFistCase(String simpleName) {
        if(simpleName.isEmpty()){
            return null;
        }
        char[] chars = simpleName.toCharArray();
        chars [0] +=32;
        return String.valueOf(chars);
    }

    private void doScannet(String scanPackage) {
        URL url = this.getClass().getClassLoader().getResource("/" + scanPackage.replaceAll("\\.","/"));
        File classPath = new File(url.getFile());
        //迭代类名
        Arrays.stream(classPath.listFiles()).forEach(file->{
            if(file.isDirectory()){
                doScannet(scanPackage + "." + file.getName());
            }else{
                if(!file.getName().endsWith(".class")){
                    return;
                }
                String className =scanPackage + "." + file.getName().replaceAll(".class","");
                classNames.add(className);
            }
        });
    }

    private void doLoadConfig(String contextConfigLocation){
        InputStream is = this.getClass().getClassLoader().getResourceAsStream(contextConfigLocation);
        try {
            contextCofig.load(is);
        }catch (IOException e){
            e.printStackTrace();
        }finally {
            if(null == is){
                try {
                    is.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        this.doPost(req,resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        //8 调用阶段
        try {
            doDispatch(req,resp);
        } catch (Exception e) {
            e.printStackTrace();
            resp.getWriter().write("500 Exception Dtail:"+Arrays.toString(e.getStackTrace()));
        }
    }

    private void doDispatch(HttpServletRequest req, HttpServletResponse resp) throws Exception {
        String url = req.getRequestURI();
        String contextPath = req.getContextPath();
        url = url.replaceAll(contextPath,"").replaceAll("/+","/");
        if(!this.handlerMappng.containsKey(url)){
            resp.getWriter().write("404 Not Found!");
        }

        Method method =  this.handlerMappng.get(url);
        Map<String,String[]> params = req.getParameterMap();
        if(null==method){
            return;
        }
        String beanName = toLoweFistCase(method.getDeclaringClass().getSimpleName());
        method.invoke(Ioc.get(beanName),new Object[]{req,resp,params.get("name")[0]});
    }

}
