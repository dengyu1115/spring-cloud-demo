package org.nature.http.controller;

import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RequestMapping("demo")
@RestController
public class DemoController {

    @RequestMapping("test")
    public Map<String, Object> test(@RequestBody Map<String, Object> req) {
        Map<String, Object> res = new HashMap<>();
        res.put("data", req.get("data"));
        return res;
    }
}
