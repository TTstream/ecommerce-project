package com.portfolio.commerceflow.common.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI commerceFlowOpenApi() {
        return new OpenAPI()
                .info(new Info()
                        .title("CommerceFlow API")
                        .description("E-Commerce backend portfolio API")
                        .version("v1"));
    }
}
