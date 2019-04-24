package com.msap.eureka.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.stereotype.Service;

@Service
@RefreshScope
public class ConfigService {
	// 설정파일 로딩안됨? 왜?
	//@Value("${msapconfig.greeting}") 
	private String configDbIp;
	//@Value("${msapconfig.db.port}") 
	private int configDbPort;
	
	public String getDbInfo() { 
		return "준비중...ing...";
	}
}
