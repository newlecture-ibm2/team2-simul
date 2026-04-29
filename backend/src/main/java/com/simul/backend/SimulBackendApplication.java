package com.simul.backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;
import org.springframework.boot.autoconfigure.flyway.FlywayAutoConfiguration;

@SpringBootApplication(exclude = {
    DataSourceAutoConfiguration.class, 
    HibernateJpaAutoConfiguration.class, 
    FlywayAutoConfiguration.class
})
@org.springframework.context.annotation.ComponentScan(basePackages = "com.simul")
public class SimulBackendApplication {

	public static void main(String[] args) {
		SpringApplication.run(SimulBackendApplication.class, args);
	}

}
