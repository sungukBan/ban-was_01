package com.oauth.configurer;

import org.springframework.security.authentication.InsufficientAuthenticationException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.oauth2.common.*;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.security.oauth2.provider.TokenRequest;
import org.springframework.security.oauth2.provider.token.DefaultTokenServices;
import org.springframework.security.oauth2.provider.token.TokenEnhancer;
import org.springframework.security.oauth2.provider.token.TokenStore;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.UUID;

public class TITokenService extends DefaultTokenServices {
	
	private TITokenStore tokenStore;
	
	private TokenEnhancer accessTokenEnhancer;
	

	@Override
	public OAuth2AccessToken refreshAccessToken(String refreshTokenValue, TokenRequest tokenRequest)
			throws AuthenticationException {
		OAuth2AccessToken accessToken =  super.refreshAccessToken(refreshTokenValue, tokenRequest);
		
		return accessToken;
	}

	/**
	 * Access token revoke
	 * @param tokenValue
	 * @return
	 */
	public boolean revokeAccessToken(String tokenValue) {
		OAuth2AccessToken accessToken = tokenStore.readAccessToken(tokenValue);
		if (accessToken == null) {
			return false;
		}
		tokenStore.removeAccessToken(accessToken);
		return true;
	}
	
	/**
	 * Refresh token revoke
	 * @param tokenValue
	 * @return
	 */
	public boolean revokeRefreshToken(String tokenValue) {
		OAuth2RefreshToken refreshToken = tokenStore.readRefreshToken(tokenValue);
		if (refreshToken == null) {
			return false;
		}
		tokenStore.removeRefreshToken(refreshToken);
		return true;
	}

	@Override
	public void setTokenStore(TokenStore tokenStore) {
		this.tokenStore = (TITokenStore)tokenStore;
		super.setTokenStore(tokenStore);
	}
	
	@Override
	@Transactional
	public OAuth2AccessToken createAccessToken(OAuth2Authentication authentication) throws AuthenticationException {
		String ci = getCI(authentication);
		String clientId = getClientId(authentication);
		
		tokenStore.revokeTokenAll(ci, clientId);
		
		/*
		System.out.println(ci + ", " + clientId);
		
		OAuth2AccessToken existingAccessToken = tokenStore.getAccessToken(authentication);
		if (existingAccessToken != null) {
			//토큰이 존재하는 경우 삭제 후 재생성
			revokeToken(existingAccessToken.getValue());
		}
		*/

		OAuth2RefreshToken refreshToken = createRefreshToken(authentication);

		OAuth2AccessToken accessToken = createAccessToken(authentication, refreshToken);
		tokenStore.storeAccessToken(accessToken, authentication);
		// In case it was modified
		refreshToken = accessToken.getRefreshToken();
		if (refreshToken != null) {
			tokenStore.storeRefreshToken(refreshToken, authentication);
		}
		return accessToken;

	}
	
	private OAuth2RefreshToken createRefreshToken(OAuth2Authentication authentication) {
		if (!isSupportRefreshToken(authentication.getOAuth2Request())) {
			return null;
		}
		int validitySeconds = getRefreshTokenValiditySeconds(authentication.getOAuth2Request());
		String value = UUID.randomUUID().toString();
		if (validitySeconds > 0) {
			return new DefaultExpiringOAuth2RefreshToken(value, new Date(System.currentTimeMillis()
					+ (validitySeconds * 1000L)));
		}
		return new DefaultOAuth2RefreshToken(value);
	}
	
	private OAuth2AccessToken createAccessToken(OAuth2Authentication authentication, OAuth2RefreshToken refreshToken) {
		DefaultOAuth2AccessToken token = new DefaultOAuth2AccessToken(UUID.randomUUID().toString());
		int validitySeconds = getAccessTokenValiditySeconds(authentication.getOAuth2Request());
		if (validitySeconds > 0) {
			token.setExpiration(new Date(System.currentTimeMillis() + (validitySeconds * 1000L)));
		}
		token.setRefreshToken(refreshToken);
		token.setScope(authentication.getOAuth2Request().getScope());

		return accessTokenEnhancer != null ? accessTokenEnhancer.enhance(token, authentication) : token;
	}
	
	@Override
	public void setTokenEnhancer(TokenEnhancer accessTokenEnhancer) {
		this.accessTokenEnhancer = accessTokenEnhancer;
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
	
	
	/*
	public boolean isRevokeToken(String tokenValue) {
		tokenStore.jdbcReadAccessToken(tokenValue);
		return false;
	}
	*/
	

}
