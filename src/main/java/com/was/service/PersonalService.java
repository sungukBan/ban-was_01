package com.was.service;

import javax.servlet.http.HttpServletRequest;

public interface PersonalService {

	String selectAgree(HttpServletRequest request, String jsonStr);
	String insertAgree(HttpServletRequest request, String jsonStr);

}
