package com.msap.zuul.rest;

import org.springframework.web.bind.annotation.GetMapping;

@org.springframework.web.bind.annotation.RestController
public class RestController {
	@GetMapping("/alive")
	public String isAlive() {
		System.out.println("zull is isAlive!");
		return "running!";
	}
}
