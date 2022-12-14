package com.was.controller;

import com.was.service.PersonalService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;

@RestController
public class PersonalController {

	@Autowired
	private PersonalService personalWasService;

	@PostMapping("/v1.0/Personal/info/insertAgree")
	public String insertAgree(HttpServletRequest request, @RequestBody String jsonStr) {
		return personalWasService.insertAgree(request, jsonStr);
	}
}