package com.oauth.feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Map;

@FeignClient(value="md-svc")
public interface MDServiceProxy {
	
	@RequestMapping(value = "/check_ca", method = RequestMethod.GET)
	public ResponseEntity checkCA(@RequestParam Map<String, String> parameters);
	
}
