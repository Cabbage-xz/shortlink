package org.cabbage.shortlink.admin;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@MapperScan("org.cabbage.shortlink.admin.dao.mapper")
@ComponentScan("org.cabbage.shortlink")
public class ShortLinkAdminApplication {
    public static void main(String[] args) {
        SpringApplication.run(ShortLinkAdminApplication.class, args);
    }
}
