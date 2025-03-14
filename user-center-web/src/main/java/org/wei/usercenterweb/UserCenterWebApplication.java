package org.wei.usercenterweb;

import org.mybatis.spring.annotation.MapperScan;
import org.mybatis.spring.annotation.MapperScans;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("org.wei.usercenterweb.mapper")
public class UserCenterWebApplication {

    public static void main(String[] args) {
        SpringApplication.run(UserCenterWebApplication.class, args);
    }

}
