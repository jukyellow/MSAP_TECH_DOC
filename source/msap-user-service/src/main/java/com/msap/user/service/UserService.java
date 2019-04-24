package com.msap.user.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;
import com.msap.user.domain.*;

@Service
public class UserService {
	
	public List<MSEntity> getUserService(){
		
		//DB��ȸ �ϴ� �����ϰ� ���������� ����
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
