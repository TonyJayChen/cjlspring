package org.cjl.spring.demo.mvc.controller;

import com.sun.deploy.net.HttpRequest;
import org.cjl.spring.mvcframework.servlet.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@CJLController
@CJLRequestMapping("/dome")
public class CJLDomeController {


    @CJLRequestMapping("/query")
    public void query(HttpServletRequest request, HttpServletResponse response,@CJLRequestParam("name") String name){
        String result = "my name is " +name + "========";
        try {
            response.getWriter().write(result);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
