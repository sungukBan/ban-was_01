package com.was.biz;

import com.was.constant.Constant;

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

	
}
