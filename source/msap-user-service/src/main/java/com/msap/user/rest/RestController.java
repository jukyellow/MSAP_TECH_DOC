package com.msap.user.rest;

import java.sql.Date;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.msap.user.domain.MSEntity;
import com.msap.user.service.UserService;
import com.netflix.hystrix.contrib.javanica.annotation.HystrixCommand;


@org.springframework.web.bind.annotation.RestController
public class RestController {
	@Autowired 
	UserService userService;

	@HystrixCommand(fallbackMethod = "getUserServiceFallback")
	@RequestMapping(value = "/user/service/v1.0/{id}", method =RequestMethod.GET) 
	//produces="text/plain;charset=UTF-8" > error occurs..
	public @ResponseBody List<MSEntity> getUserService(@PathVariable String id) {
		System.out.println("called user/v1.0/id:"+id);
		
		long time = System.currentTimeMillis(); 
		SimpleDateFormat dayTime = new SimpleDateFormat("yyyy-mm-dd hh:mm:ss");
		String dateTime = dayTime.format(new Date(time));
		System.out.println(dateTime);
			
		List<MSEntity> result = userService.getUserService();
		for(MSEntity entity: result) {
			System.out.println(entity.toString());
		}
		
		return result;
	}	
	
	//fallback
	public @ResponseBody List<MSEntity> getUserServiceFallback(@PathVariable String id){
		System.out.println("called getUserServiceFallback:"+ id);	
		List<MSEntity> msTemp = Arrays.asList(new MSEntity());
		return msTemp;
	}
	
	@HystrixCommand(fallbackMethod = "getUserService2Fallback")
	@RequestMapping(value = "/user/service/v2.0/{id}", method =RequestMethod.GET) 
	public String getUserService2(@PathVariable String id) {
		System.out.println("called user/v2.0/id:"+id);
		 
		long time = System.currentTimeMillis(); SimpleDateFormat dayTime = new
		SimpleDateFormat("yyyy-mm-dd hh:mm:ss"); String dateTime = dayTime.format(new
		Date(time)); System.out.println(dateTime);
		
		return "getUserService2 test..."; 
	}
	
	public String getUserService2Fallback(@PathVariable String id){
		System.out.println("called getUserService2Fallback:"+id);
		return "fallback test...";
	}
	
	
	//fallback test : input/output type must be equal!
	@HystrixCommand(fallbackMethod = "fallbackFunction")
	@RequestMapping(value = "/user/fallbackTest", method = RequestMethod.GET)
	public String fallbackTest() throws Throwable{
		throw new Throwable("fallback Throwable...");
	}
	
	public String fallbackFunction(){
		System.out.println("called fallbackFunction");
		return "fallbackFunction()";
	}
}
