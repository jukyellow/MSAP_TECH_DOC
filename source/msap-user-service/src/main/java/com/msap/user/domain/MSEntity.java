package com.msap.user.domain;

import java.util.ArrayList;
import java.util.List;

//Micro Service Entity
public class MSEntity {
	public String toString() {
		return "[serviceName:"+serviceName+",registerDate:"+registerDate+",registerName:"+registerName+"]";
	}
	
	public String getServiceName() {
		return serviceName;
	}
	public void setServiceName(String serviceName) {
		this.serviceName = serviceName;
	}
	public String getRegisterDate() {
		return registerDate;
	}
	public void setRegisterDate(String registerDate) {
		this.registerDate = registerDate;
	}
	public String getRegisterName() {
		return registerName;
	}
	public void setRegisterName(String registerName) {
		this.registerName = registerName;
	}
	
	private String serviceName;			
	private String registerDate;
	private String registerName;
}
