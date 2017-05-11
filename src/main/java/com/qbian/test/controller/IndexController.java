package com.qbian.test.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

/**
 * Created by Qbian on 2017/5/11.
 */
@Controller
public class IndexController {

    @GetMapping(value = {"/", "/index"})
    public String index() {

        return "index";
    }

    @GetMapping(value = {"/fragments/add-data", "/fragments/search-data"})
    public String indexIncludePage(HttpServletRequest request) {
        // request.getServletPath() => /fragments/add-data.html
        String path = request.getServletPath();

        return path.substring(0, path.length() - 5);
    }

}
