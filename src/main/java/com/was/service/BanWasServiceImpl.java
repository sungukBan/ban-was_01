package com.was.service;

import com.was.dto.PaySendVo;
import com.was.mapper.BanWasMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;

@Service
public class BanWasServiceImpl implements BanWasService {

	@Autowired
	private BanWasMapper banWasMapper;

	@Override
	public String insPaySend(HttpServletRequest request, String jsonStr) {
		String resultStr = "";
		PaySendVo vo = new PaySendVo();
		return resultStr;
	}

	@Override
	public String selPayList(HttpServletRequest request, String jsonStr) {
		return null;
	}

	@Override
	public String selPayListAll() {
		return null;
	}

	@Override
	public String selPayInfo(HttpServletRequest request, String jsonStr) {
		return null;
	}


	/**
	 * 사용자 데이터
	 * @return

	public List<Member> listData(){
		List<Member> list = new ArrayList<>();
		Member member1 = new Member("kb01", "Amelia" , 3000,  "amelia@kbstar.com", "female");
		Member member2 = new Member("kb02", "Hazel"  , 5000,  "hazel@kbstar.com",  "female");
		Member member3 = new Member("kb03", "Oliver" , 0,     "oliver@kbstar.com", "male");
		Member member4 = new Member("kb04", "Harry"  , 12000, "harry@kbstar.com",  "male");
		Member member5 = new Member("kb05", "James"  , 2300,  "james@kbstar.com",  "male");
		Member member6 = new Member("kb06", "Adela"  , 0,     "adela@kbstar.com",  "female");
		Member member7 = new Member("kb07", "John"   , 500,   "john@kbstar.com",   "male");
		Member member8 = new Member("kb08", "Oskar"  , 3700,  "oskar@kbstar.com",  "male");

		list.add(member1);
		list.add(member2);
		list.add(member3);
		list.add(member4);
		list.add(member5);
		list.add(member6);
		list.add(member7);
		list.add(member8);

		return list;
	}


	public String commonBiz(HttpServletRequest request, String jsonStr, String bizCd) {

		APIStatus status = new APIStatus();
		System.out.println("-----------------------------------------------------");
		System.out.println("X-ROOM-ID :: " + request.getHeader("X-ROOM-ID"));
		System.out.println("X-USER-ID :: " + request.getHeader("X-USER-ID"));
		System.out.println("jsonStr :: " + jsonStr);
		System.out.println("bizCd :: " + bizCd);
		System.out.println("-----------------------------------------------------");

		//--------------------------------------
		// Http header입력값 검증
		//--------------------------------------
		if ( commonBiz.doComValidation(request, status) == false ) {
			return setResponseData(jsonStr, status);
		}

		return setResponseData(jsonStr, status);
	}

	private String setResponseData(String jsonStr, APIStatus status) {

		try {
			JSONParser jsonParser = new JSONParser();
			JSONObject jsonObj    = (JSONObject) jsonParser.parse(jsonStr);
			jsonObj.put(Constant.RLST_CD, status.getRLST_CD());
			jsonObj.put(Constant.RLST_MSG, status.getRLST_MSG());

			jsonStr = jsonObj.toJSONString();
		} catch(Exception e) {
			e.printStackTrace();
		}

		System.out.println("jsonStr :: " + jsonStr);
		return jsonStr;
	}
	 */
}
