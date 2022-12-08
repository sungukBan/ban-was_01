package com.oauth;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableAuthorizationServer;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

@EnableFeignClients
@EnableAuthorizationServer
@EnableSwagger2
@SpringBootApplication
public class BanOauthApplication {

	public static void main(String[] args) {
		SpringApplication.run(BanOauthApplication.class, args);
	}
	
}
