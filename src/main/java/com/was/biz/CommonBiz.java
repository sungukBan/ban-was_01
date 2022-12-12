package com.was.biz;

import com.was.Constant.Constant;
import com.was.biz.APIStatus;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import javax.servlet.http.HttpServletRequest;

public class CommonBiz {

	public boolean doComValidation(HttpServletRequest request, APIStatus status) {
		
		if ( request.getHeader("X-ROOM-ID") == null || request.getHeader("X-ROOM-ID").equals("") ) {
			status.setAPIStatus(Constant.RLST_CD_9001, Constant.RLST_MSG_9001);
			return false;
		}
		
		if ( request.getHeader("X-USER-ID") == null || request.getHeader("X-USER-ID").equals("") ) {
			status.setAPIStatus(Constant.RLST_CD_9002, Constant.RLST_MSG_9002);
			return false;
		}

		return true;
	}
	
	public boolean do101Validation(String jsonStr, APIStatus status) {
		
		JSONParser jsonParser = new JSONParser();

		try {
			JSONObject jsonObj    = (JSONObject) jsonParser.parse(jsonStr);
			
			if ( jsonObj.get("SEND_AMT") == null ) {
				status.setAPIStatus(Constant.RLST_CD_9003, Constant.RLST_MSG_9003);
				return false;
			}
			if ( Integer.parseInt(String.valueOf(jsonObj.get("SEND_AMT"))) == 0 ) {
				status.setAPIStatus(Constant.RLST_CD_9004, Constant.RLST_MSG_9004);
				return false;
			}
			
			if ( jsonObj.get("SEND_CNT") == null ) {
				status.setAPIStatus(Constant.RLST_CD_9005, Constant.RLST_MSG_9005);
				return false;
			}
			if ( Integer.parseInt(String.valueOf(jsonObj.get("SEND_CNT"))) == 0 ) {
				status.setAPIStatus(Constant.RLST_CD_9006, Constant.RLST_MSG_9006);
				return false;
			}
		} catch(Exception e) {
			e.printStackTrace();
			status.setAPIStatus(Constant.RLST_CD_9999, Constant.RLST_MSG_9999);
			return false;
		}
		return true;
	}
	
	public boolean doTokenValidation(String jsonStr, APIStatus status) {
		
		JSONParser jsonParser = new JSONParser();

		try {
			JSONObject jsonObj    = (JSONObject) jsonParser.parse(jsonStr);
			
			if ( jsonObj.get("TOKEN") == null ) {
				status.setAPIStatus(Constant.RLST_CD_9007, Constant.RLST_MSG_9007);
				return false;
			}
			if ( String.valueOf(jsonObj.get("TOKEN")).equals("") ) {
				status.setAPIStatus(Constant.RLST_CD_9007, Constant.RLST_MSG_9007);
				return false;
			}
		} catch(Exception e) {
			e.printStackTrace();
			status.setAPIStatus(Constant.RLST_CD_9999, Constant.RLST_MSG_9999);
			return false;
		}
		return true;
	}
	
}
