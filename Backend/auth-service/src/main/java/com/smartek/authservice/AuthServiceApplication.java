package com.smartek.authservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication
@EnableDiscoveryClient
public class AuthServiceApplication {
    
    public static void main(String[] args) {
        SpringApplication.run(AuthServiceApplication.class, args);
        System.out.println("╔═══════════════════════════════════════════════════════╗");
        System.out.println("║                                                       ║");
        System.out.println("║        SMARTEK Auth Service Started Successfully     ║");
        System.out.println("║                                                       ║");
        System.out.println("║        Port: 8081                                     ║");
        System.out.println("║        Database: smartek_db                           ║");
        System.out.println("║                                                       ║");
        System.out.println("╚═══════════════════════════════════════════════════════╝");
    }
}
