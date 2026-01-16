package com.scnai.plant;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * SCNAI植鉴系统 - 主应用入口
 *
 * @author SCNAI Team
 * @version 1.0
 */
@SpringBootApplication
public class PlantApplication {

    public static void main(String[] args) {
        SpringApplication.run(PlantApplication.class, args);
        System.out.println("========================================");
        System.out.println("SCNAI植鉴系统后端服务启动成功！");
        System.out.println("访问地址: http://localhost:5000/api");
        System.out.println("========================================");
    }
}
