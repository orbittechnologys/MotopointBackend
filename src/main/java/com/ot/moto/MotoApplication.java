package com.ot.moto;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication
public class MotoApplication {

    public static void main(String[] args) {
        SpringApplication.run(MotoApplication.class, args);
    }

}
