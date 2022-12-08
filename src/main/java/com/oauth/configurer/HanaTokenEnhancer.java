package com.oauth.configurer;

import com.oauth.hanaClient.HanaClientDetails;
import com.oauth.hanaClient.HanaJdbcClientDetailsService;
import org.springframework.security.authentication.InsufficientAuthenticationException;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.common.DefaultOAuth2AccessToken;
import org.springframework.security.oauth2.common.OAuth2AccessToken;
import org.springframework.security.oauth2.provider.ClientDetails;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.security.oauth2.provider.OAuth2Request;
import org.springframework.security.oauth2.provider.token.TokenEnhancer;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class HanaTokenEnhancer implements TokenEnhancer {
	
	private HanaJdbcClientDetailsService clientService;
	
	public HanaTokenEnhancer(HanaJdbcClientDetailsService clientService) {
		this.clientService = clientService;
	}

	@Override
	public OAuth2AccessToken enhance(OAuth2AccessToken accessToken, OAuth2Authentication authentication) {
		Map<String, Object> additionalInfo = new HashMap<>();
		
		//접근토큰 발급자 추가 org_code
		String orgCode = Optional.ofNullable(getRequestParam(authentication, "org_code")).orElse("");
		additionalInfo.put("iss", orgCode);
		
		//접근토큰 수신자 추가
		String aud = "";
		String clientId = getClientId(authentication);
		ClientDetails details = clientService.loadClientByClientId(clientId);
		if(details != null && details instanceof HanaClientDetails) {
			HanaClientDetails hanaDetails = (HanaClientDetails) details;
			aud = hanaDetails.getOrgCode();
		}
		additionalInfo.put("aud", aud);
		
		
		//jti = ci
		String ci = getCI(authentication);
		additionalInfo.put("jti", ci);
		
		DefaultOAuth2AccessToken oauthToken = (DefaultOAuth2AccessToken) accessToken;
		oauthToken.setAdditionalInformation(additionalInfo);
		return accessToken;
	}

	/**
	 * 로그인한 사용자 정보를 리턴.
	 * @return
	 */
	public String getCI(OAuth2Authentication authentication) {
		String ci = "";
		Object principal = authentication.getPrincipal();
		if(principal instanceof Authentication) {
			Authentication auth = (Authentication) principal;
			ci = auth.getName();
		} else if(principal instanceof String) {
			ci = (String) principal;
		}
		return ci;
	}
	
	/**
	 * 토큰 요청 파라메터 중 키에 맞는 값을 리턴.
	 * @param authentication
	 * @param key
	 * @return
	 */
	public String getRequestParam(OAuth2Authentication authentication, String key) {
		OAuth2Request request = authentication.getOAuth2Request();
		Map<String, String> param = request.getRequestParameters();
		return param.get(key);
	}
	
	
	protected String getClientId(Authentication client) {
		if (!client.isAuthenticated()) {
			throw new InsufficientAuthenticationException("The client is not authenticated.");
		}
		String clientId = client.getName();
		if (client instanceof OAuth2Authentication) {
			// Might be a client and user combined authentication
			clientId = ((OAuth2Authentication) client).getOAuth2Request().getClientId();
		}
		return clientId;
	}
}
