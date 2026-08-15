package com.portfolio.commerceflow;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class CommerceFlowApplication {

    public static void main(String[] args) {
        SpringApplication.run(CommerceFlowApplication.class, args);
    }
}
