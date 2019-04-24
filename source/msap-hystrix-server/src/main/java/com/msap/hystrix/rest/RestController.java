package com.msap.hystrix.rest;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.web.bind.annotation.GetMapping;

@org.springframework.web.bind.annotation.RestController
//@RefreshScope
public class RestController {
	//@Value("${hostip}")
	private String hostIp;
	
	@GetMapping("/host")
	public String getHostIp() {
		System.out.println("getHostIp:"+hostIp);
		return hostIp;
	}
}
