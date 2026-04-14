package com.smartek.courseservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableDiscoveryClient
@EnableFeignClients
public class CourseServiceApplication {
    
    public static void main(String[] args) {
        SpringApplication.run(CourseServiceApplication.class, args);
        System.out.println("\n" +
                "╔═══════════════════════════════════════════════════════╗\n" +
                "║                                                       ║\n" +
                "║        SMARTEK Course Service Started Successfully   ║\n" +
                "║                                                       ║\n" +
                "║        Port: 8082                                     ║\n" +
                "║        Database: smartek_db                           ║\n" +
                "║                                                       ║\n" +
                "╚═══════════════════════════════════════════════════════╝\n");
    }
}
