package com.msap.eureka.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import com.msap.eureka.service.ConfigService;

@RestController
public class ConfigController {
	
	@Autowired
	ConfigService cs;
	
	@GetMapping("/dbinfo") 
	public String getDBInfo() { 
		return cs.getDbInfo(); 
	}
	 
}
