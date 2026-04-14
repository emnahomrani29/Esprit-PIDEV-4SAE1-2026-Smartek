package com.smartek.offersservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableDiscoveryClient
@EnableScheduling
@EnableFeignClients
public class OffersServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(OffersServiceApplication.class, args);
        System.out.println("╔═══════════════════════════════════════════════════════╗");
        System.out.println("║       SMARTEK Offers Service Started Successfully    ║");
        System.out.println("║        Port: 8085  |  Database: offers_db            ║");
        System.out.println("╚═══════════════════════════════════════════════════════╝");
    }
}
