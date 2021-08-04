package org.cjl.spring.demo.mvc.controller;

import com.sun.deploy.net.HttpRequest;
import org.cjl.spring.demo.aop.service.SuperMan;
import org.cjl.spring.demo.aop.service.impl.Student;
import org.cjl.spring.demo.mvc.service.IDemoService;
import org.cjl.spring.mvcframework.servlet.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@CJLController
@CJLRequestMapping("/dome")
public class CJLDomeController {

    @CJLAutowird
    private IDemoService iDemoService;
    @CJLAutowird
    private SuperMan superMan;
    @CJLRequestMapping("/query")
    public void query(HttpServletRequest request,HttpServletResponse response,@CJLRequestParam("name") String name){
        try {
            superMan.add(1,2);
            superMan.divide(3,4);
            String result = "my name is " +name + "========";
            response.getWriter().write(result);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
