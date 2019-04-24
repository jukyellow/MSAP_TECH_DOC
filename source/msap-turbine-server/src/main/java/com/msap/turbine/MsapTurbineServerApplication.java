package com.msap.turbine;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;
import org.springframework.cloud.netflix.turbine.EnableTurbine;

@SpringBootApplication
@EnableEurekaClient
@EnableTurbine
public class MsapTurbineServerApplication {

	public static void main(String[] args) {
		SpringApplication.run(MsapTurbineServerApplication.class, args);
	}

}
