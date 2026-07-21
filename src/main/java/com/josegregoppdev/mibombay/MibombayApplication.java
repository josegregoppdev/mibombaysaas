package com.josegregoppdev.mibombay;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableJpaAuditing
public class MibombayApplication {

	public static void main(String[] args) {
		SpringApplication.run(MibombayApplication.class, args);
	}

}
