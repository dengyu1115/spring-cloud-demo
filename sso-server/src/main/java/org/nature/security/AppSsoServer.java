package org.nature.security;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication
@EnableDiscoveryClient
public class AppSsoServer {

    public static void main(String[] args) {
        SpringApplication.run(AppSsoServer.class, args);
    }
}
