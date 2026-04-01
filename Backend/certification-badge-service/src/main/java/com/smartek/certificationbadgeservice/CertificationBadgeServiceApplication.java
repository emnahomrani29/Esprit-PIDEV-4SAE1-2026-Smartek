package com.smartek.certificationbadgeservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@SpringBootApplication
@EnableDiscoveryClient
@EnableTransactionManagement
public class CertificationBadgeServiceApplication {
    
    public static void main(String[] args) {
        SpringApplication.run(CertificationBadgeServiceApplication.class, args);
        System.out.println("╔═══════════════════════════════════════════════════════╗");
        System.out.println("║                                                       ║");
        System.out.println("║   SMARTEK Certification Badge Service Started        ║");
        System.out.println("║                                                       ║");
        System.out.println("║        Port: 8082                                     ║");
        System.out.println("║        Database: smartek_db                           ║");
        System.out.println("║                                                       ║");
        System.out.println("╚═══════════════════════════════════════════════════════╝");
    }
}
