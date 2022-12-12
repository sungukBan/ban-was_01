package com.was.controller;

import com.was.service.BanWasService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import javax.servlet.http.HttpServletRequest;

@RestController
public class BanWasController {

	@Autowired
	private BanWasService banWasService;

	@PostMapping("/v1/pay/send")
	public String Send(HttpServletRequest request, @RequestBody String jsonStr) {
		return banWasService.insPaySend(request, jsonStr);
	}

	@PostMapping("/v1/pay/recv")
	public String Recv(HttpServletRequest request,  @RequestBody String jsonStr) {
		return banWasService.selPayList(request, jsonStr);
	}

	@PostMapping("/v1/pay/inq")
	public String Inq(HttpServletRequest request,  @RequestBody String jsonStr) {
		return banWasService.insPaySend(request, jsonStr);
	}
}