package com.userfront;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;
import org.springframework.boot.autoconfigure.security.SecurityAutoConfiguration;

@SpringBootApplication(exclude = {
        SecurityAutoConfiguration.class,
        HibernateJpaAutoConfiguration.class
})
public class UserFrontApplication {

    public static void main(String[] args) {
        SpringApplication.run(UserFrontApplication.class, args);
    }
}
