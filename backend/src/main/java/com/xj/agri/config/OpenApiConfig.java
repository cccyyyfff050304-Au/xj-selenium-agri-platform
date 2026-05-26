package com.xj.agri.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {
    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI().info(new Info()
                .title("新疆特色作物富硒品质预测与智慧决策平台 API")
                .version("0.1.0")
                .description("面向新疆智慧农业、富硒品质预测、土壤环境评估和决策建议的接口文档"));
    }
}
