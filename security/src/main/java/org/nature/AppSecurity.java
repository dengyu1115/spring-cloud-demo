package org.nature;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication
@EnableDiscoveryClient
public class AppSecurity {

    public static void main(String[] args) {
        SpringApplication.run(AppSecurity.class, args);
    }
}
