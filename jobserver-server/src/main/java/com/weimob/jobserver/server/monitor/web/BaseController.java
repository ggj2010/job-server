package com.weimob.jobserver.server.monitor.web;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * @author: kevin
 * @date 2016/08/10.
 */
@Controller
public class BaseController {
    @RequestMapping(value = "/")
    public String index() {
        return "redirect:/monitor/job/jobList";
    }
}
