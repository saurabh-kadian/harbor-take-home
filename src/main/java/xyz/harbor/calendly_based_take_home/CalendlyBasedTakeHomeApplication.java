package xyz.harbor.calendly_based_take_home;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@SpringBootApplication
@EnableCaching
public class CalendlyBasedTakeHomeApplication {

	public static void main(String[] args) {
		SpringApplication.run(CalendlyBasedTakeHomeApplication.class, args);
	}

}
