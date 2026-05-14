package com.smartek.offersservice;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

    private static final Logger logger = LoggerFactory.getLogger(OffersServiceApplication.class);

    public static void main(String[] args) {
        SpringApplication.run(OffersServiceApplication.class, args);
        // Use proper logging instead of System.out.println
        logger.info("╔═══════════════════════════════════════════════════════╗");
        logger.info("║       SMARTEK Offers Service Started Successfully    ║");
        logger.info("║        Port: 8085  |  Database: offers_db            ║");
        logger.info("╚═══════════════════════════════════════════════════════╝");
    }
}
