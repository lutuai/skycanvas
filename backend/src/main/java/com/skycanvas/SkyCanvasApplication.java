package com.skycanvas;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * SkyCanvas应用启动类
 */
@SpringBootApplication
@MapperScan("com.skycanvas.mapper")
@EnableAsync
@EnableScheduling
public class SkyCanvasApplication {

    public static void main(String[] args) {
        SpringApplication.run(SkyCanvasApplication.class, args);
        System.out.println("\n====================================");
        System.out.println("SkyCanvas Backend Started Successfully!");
        System.out.println("API地址: http://localhost:8080/api");
        System.out.println("====================================\n");
    }
}

