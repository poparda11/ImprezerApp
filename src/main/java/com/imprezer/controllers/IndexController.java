package com.imprezer.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * Created by robert on 08.05.16.
 */
@Controller
public class IndexController {
    @RequestMapping("/")
    String index(){
        return "index.html";
    }
}
