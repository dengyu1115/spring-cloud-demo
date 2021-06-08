package org.nature.feign.service;

import org.nature.common.model.Res;
import org.nature.feign.model.Demo;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;

@FeignClient(name = "HTTP", path = "demo")
public interface DemoService {

    @PostMapping("test")
    Res<Demo> test(Demo demo);
}
