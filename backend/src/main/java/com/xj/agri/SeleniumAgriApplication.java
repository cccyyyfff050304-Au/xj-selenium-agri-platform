package com.xj.agri;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.xj.agri.mapper")
public class SeleniumAgriApplication {
    public static void main(String[] args) {
        SpringApplication.run(SeleniumAgriApplication.class, args);
    }
}
