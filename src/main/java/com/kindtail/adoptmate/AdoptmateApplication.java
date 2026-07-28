package com.kindtail.adoptmate;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@EnableJpaAuditing
@SpringBootApplication
public class AdoptmateApplication {

    public static void main(String[] args) {
        SpringApplication.run(AdoptmateApplication.class, args);
    }

}
