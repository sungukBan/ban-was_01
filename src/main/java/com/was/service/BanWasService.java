package com.was.service;

import com.was.dto.PaySendVo;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

public interface BanWasService {

	String insPaySend(HttpServletRequest request, String jsonStr);

	String selPayList(HttpServletRequest request, String jsonStr);

	String selPayListAll();

	String selPayInfo(HttpServletRequest request, String jsonStr);

	/*
	PaySendVo insPaySend(PaySendVo dto);

	List<PaySendVo> selPayList(String dto);

	List<PaySendVo> selPayListAll();

	PaySendVo selPayInfo(String dto);
	*/
}
