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
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.net.URL;
import java.util.*;
import java.util.stream.Stream;

/**
 * @author cjl
 */
public class CJLDispatcherServlet extends HttpServlet {

    //2 IOC容器初始化
    private Map<String,Object> Ioc = new HashMap<>();
    //定义Properties配置文件的变量
    private Properties contextCofig = new Properties();

    private  List<String> classNames = new ArrayList<>();

    private Map<String, Method> handlerMappng = new HashMap<>();

    @Override
    public void init(ServletConfig config) throws ServletException {
        //1 加载配置文件,填写的为web.xml中的对应的配置文件名称
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

    /**
     * 加载请求地址
     */
    private void doInitHandlerMapping() {
        if(Ioc.isEmpty()){
            return;
        }
        Ioc.entrySet().stream().forEach(entry->{
            //获取class
            Class<?> clazz = entry.getValue().getClass();
            if(!clazz.isAnnotationPresent(CJLController.class)){
                return;
            }
            String bsaeUrl = "";
            //判断是否使用的请求注解
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
                //根据类的请求地址加上方法请求地址组合成具体的请求
                String url = ("/" + bsaeUrl + "/" + requestMapping.value()).replaceAll("/+","/");
                //将其放入mappng容器中
                handlerMappng.put(url,method);
                System.out.println("Mapped:"+url+","+method);
            }
        });
    }

    /**
     *  分配实例化的bean到相关类中
     */
    private void doAutowired() {
        if(Ioc.isEmpty()){
            return;
        }
        Ioc.entrySet().forEach(entry->{
            //public、private、protected、default
            //获取所有的声明字段
            Field [] fields = entry.getValue().getClass().getDeclaredFields();
            Arrays.stream(fields).forEach(field -> {
                //判断该字段是否使用的注入注解
                if(!field.isAnnotationPresent(CJLAutowird.class)){
                    return;
                }
                //获取当前注解
                CJLAutowird autowird = field.getAnnotation(CJLAutowird.class);
                //获取自定义字段名
                String beanName = autowird.value().trim();
                //如字段名为空则获取字段名
                if("".equals(beanName)){
                    beanName = field.getType().getName();
                }
                //设置访问权限，实行暴力访问
                field.setAccessible(true);
                try {
                    //注入相关实例化
                    field.set(entry.getValue(),Ioc.get(beanName));
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
            });
        });
    }

    /**
     * 创建所有类名并且实例化对象，加载到IOC容器中
     */
    private void doInstance() {
        //读取类名是否为空
        if(classNames.isEmpty()){
            return;
        }
        //迭代所有类名
        classNames.forEach(className->{
            try {
                //根据类名获取其class
                Class<?> clazz = Class.forName(className);
                //判断当前类是否使用了控制层注解
                if(clazz.isAnnotationPresent(CJLController.class)){
                    //1、默认规则，类名首字母小写
                    //获取bean名
                    String beanName = toLoweFistCase(clazz.getSimpleName());
                    //创建实例化
                    Object instance = clazz.newInstance();
                    //进入IOC容器
                    Ioc.put(beanName,instance);
                }else//判断当前类是否使用了服务层注解
                    if(clazz.isAnnotationPresent(CJLService.class)){
                    //1、默认规则，类名首字母小写
                    //获取bean名
                    String beanName = toLoweFistCase(clazz.getSimpleName());
                    //2、读取其是否有自定义boen名
                    CJLService service = clazz.getAnnotation(CJLService.class);
                    if("".equals(service.value())){
                        beanName = service.value();
                    }
                    //创建实例化
                    Object instance = clazz.newInstance();
                    //进入IOC容器
                    Ioc.put(beanName,instance);
                    //3.接口注入
                    //迭代其类的接口名称，将其接口实例化为实现类
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

    /**
     * 扫描配置文件中所定义包位置
     * @param scanPackage
     */
    private void doScannet(String scanPackage) {
        //获取包地址
        URL url = this.getClass().getClassLoader().getResource("/" + scanPackage.replaceAll("\\.","/"));
        //读取地址中的所有文件或文件夹
        File classPath = new File(url.getFile());
        //迭代所有类文件
        Arrays.stream(classPath.listFiles()).forEach(file->{
            //如果是文件夹的话则进一步迭代
            if(file.isDirectory()){
                doScannet(scanPackage + "." + file.getName());
            }else{
                //获取文件是否为clsaa（字节码）文件
                if(!file.getName().endsWith(".class")){
                    return;
                }
                //读取类名，去除class后缀
                String className =scanPackage + "." + file.getName().replaceAll(".class","");
                classNames.add(className);
            }
        });
    }

    /**
     *  读取Properties配置文件
     * @param contextConfigLocation
     */
    private void doLoadConfig(String contextConfigLocation){
        //读取配置文件为文件流
        InputStream is = this.getClass().getClassLoader().getResourceAsStream(contextConfigLocation);
        try {
            //加载Properties配置文件
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

    /**
     * 具体执行
     * @param req
     * @param resp
     * @throws Exception
     */
    private void doDispatch(HttpServletRequest req, HttpServletResponse resp) throws Exception {
        //获取请求地址
        String url = req.getRequestURI();
        String contextPath = req.getContextPath();
        url = url.replaceAll(contextPath,"").replaceAll("/+","/");
        //校验地址是否存在
        if(!this.handlerMappng.containsKey(url)){
            resp.getWriter().write("404 Not Found!");
        }
        //拿到相关地址method实例化
        Method method =  this.handlerMappng.get(url);
        //获取传递值
        Map<String,String[]> params = req.getParameterMap();
        if(null==method){
            return;
        }
        String beanName = toLoweFistCase(method.getDeclaringClass().getSimpleName());
        Class[] parameterClazzs = method.getParameterTypes();
        List<Object> objectList = new ArrayList<>();
        //判断是否存在request和response
        //目前只支持在常规参数前去传递
        Arrays.stream(parameterClazzs).forEach(clazz -> {
            if("HttpServletRequest".equals(clazz.getSimpleName())){
                objectList.add(req);
            }else if("HttpServletResponse".equals(clazz.getSimpleName())){
                objectList.add(resp);
            }

        });
        //获取常规传参
        params.entrySet().forEach(param->{
            objectList.add(param.getValue()[0]);
        });
        method.invoke(Ioc.get(beanName),objectList.toArray());
    }

}
