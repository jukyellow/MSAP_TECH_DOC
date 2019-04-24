package com.msap.user.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;
import com.msap.user.domain.*;

@Service
public class UserService {
	
	public List<MSEntity> getUserService(){
		
		//DB조회 일단 생략하고 수동생성후 응답
		List userServiceList = new ArrayList();
		
		MSEntity ms = new MSEntity();
		ms.setServiceName("AI Service");
		ms.setRegisterDate("2019/03/23");
		ms.setRegisterName("Tester..");
		userServiceList.add(ms);
		
		ms = new MSEntity();
		ms.setServiceName("Tracking Service");
		ms.setRegisterDate("2019/03/24");
		ms.setRegisterName("AAAAA Tester");
		userServiceList.add(ms);
		
		return userServiceList;
	}

}
