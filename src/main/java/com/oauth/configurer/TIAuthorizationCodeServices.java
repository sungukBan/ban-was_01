package com.oauth.configurer;

import org.springframework.security.oauth2.common.util.RandomValueStringGenerator;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.security.oauth2.provider.code.JdbcAuthorizationCodeServices;

import javax.sql.DataSource;

public class TIAuthorizationCodeServices extends JdbcAuthorizationCodeServices {
	
	//마이데이터 표준 128 길이의 authorization code 생성
	private RandomValueStringGenerator generator = new RandomValueStringGenerator(128);

	public TIAuthorizationCodeServices(DataSource dataSource) {
		super(dataSource);
	}
	
	@Override
	public String createAuthorizationCode(OAuth2Authentication authentication) {
		String code = generator.generate();
		store(code, authentication);
		return code;
	}

}
