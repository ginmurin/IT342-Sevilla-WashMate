package edu.cit.sevilla.washmate;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication
@EnableJpaRepositories(basePackages = "edu.cit.sevilla.washmate.features")
@EntityScan(basePackages = "edu.cit.sevilla.washmate.features")
public class WashmateApplication {

	public static void main(String[] args) {
		SpringApplication.run(WashmateApplication.class, args);
	}

}

