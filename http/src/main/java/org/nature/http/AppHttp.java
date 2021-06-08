package org.nature.http;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication
@EnableDiscoveryClient
public class AppHttp {

    public static void main(String[] args) {
        SpringApplication.run(AppHttp.class, args);
    }
}
