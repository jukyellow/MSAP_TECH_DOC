package com.msap.eureka;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.server.EnableEurekaServer;

@SpringBootApplication
@EnableEurekaServer
public class MsapEurekaServerApplication {

	public static void main(String[] args) {
		SpringApplication.run(MsapEurekaServerApplication.class, args);
	}

}
