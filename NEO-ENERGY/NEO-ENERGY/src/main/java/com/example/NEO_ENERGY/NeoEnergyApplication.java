package com.example.NEO_ENERGY;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
// escutando as entidades
@EnableJpaAuditing
public class NeoEnergyApplication {

	public static void main(String[] args) {
		SpringApplication.run(NeoEnergyApplication.class, args);
	}

}
