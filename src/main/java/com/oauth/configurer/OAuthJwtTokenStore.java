package com.oauth.configurer;

import com.oauth.mapper.OAuthRefreshTokenMapper;
import com.oauth.model.OAuthRefreshToken;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.common.OAuth2RefreshToken;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.security.oauth2.provider.token.store.JwtAccessTokenConverter;
import org.springframework.security.oauth2.provider.token.store.JwtTokenStore;

import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Date;

public class OAuthJwtTokenStore extends JwtTokenStore {

	@Autowired
	private OAuthRefreshTokenMapper mapper;
	
	@Value("${oauth.config.refreshTokenExpireSecond}")
	private long refreshTokenExpireSecond;

	public OAuthJwtTokenStore(JwtAccessTokenConverter jwtTokenEnhancer) {
		super(jwtTokenEnhancer);
	}
	
	
	/**
	 * 리프레시 토큰 저장
	 */
	@Override
	public void storeRefreshToken(OAuth2RefreshToken refreshToken, OAuth2Authentication authentication) {
		OAuthRefreshToken crt = new OAuthRefreshToken();
		crt.setTokenId(extractTokenKey(refreshToken.getValue()));
		crt.setTokenObject(refreshToken);
		crt.setAuthenticationObject(authentication);
		crt.setUsername(authentication.isClientOnly() ? null : authentication.getName());
		crt.setClientId(authentication.getOAuth2Request().getClientId());
		
		//유효기간 설정
		crt.setExpireDate(new Date(expireDate()));
		mapper.saveRefreshToken(crt);
	}
	
	/**
	 * Jwt에서 Refresh Token을 읽어오고 값이 존재한다면 데이터베이스에서 Refresh Token의 정보를 읽어옵니다.
	 */
	@Override
	public OAuth2RefreshToken readRefreshToken(String tokenValue) {
		OAuth2RefreshToken token = super.readRefreshToken(tokenValue);
		if(token != null) {
			OAuthRefreshToken refreshToken = getRefreshToken(token);
			if(refreshToken != null) {
				long now = new Date().getTime();
				if(refreshToken.getExpireDate().getTime() > now) {
					//유효기간 체크하여 아직 유효한 토큰인 경우 리턴.
					return refreshToken.getTokenObject();
				}				
				//유효기간 지난 경우 리플레시 토큰 삭제
				removeRefreshToken(token);
			}			
		}
		return null;
	}

	/**
	 * Jwt에서 인증정보를 살펴보고 값이 존재한다면 데이터베이스에서도 읽어옵니다.
	 */
	@Override
	public OAuth2Authentication readAuthenticationForRefreshToken(OAuth2RefreshToken refreshToken) {
		OAuth2Authentication oauth = super.readAuthenticationForRefreshToken(refreshToken);
		if(oauth != null) {
			OAuthRefreshToken rtk = getRefreshToken(refreshToken);
			return rtk != null ? rtk.getAuthenticationObject() : null;
		}
		return oauth;
	}

	/**
	 * Refresh Token을 삭제합니다.
	 */
	@Override
	public void removeRefreshToken(OAuth2RefreshToken refreshToken) {
		super.removeRefreshToken(refreshToken);
		
		OAuthRefreshToken rtk = getRefreshToken(refreshToken);
		if (rtk != null) {
			mapper.deleteRefreshToken(rtk);
		}
	}
	
	
	public OAuthRefreshToken getRefreshToken(OAuth2RefreshToken refreshToken) {
		return mapper.findRefreshTokenByTokenId(extractTokenKey(refreshToken.getValue()));
	}

	private String extractTokenKey(String value) {
		if (value == null) {
			return null;
		} else {
			MessageDigest digest;
			try {
				digest = MessageDigest.getInstance("MD5");
			} catch (NoSuchAlgorithmException var5) {
				throw new IllegalStateException("MD5 algorithm not available.  Fatal (should be in the JDK).");
			}

			try {
				byte[] e = digest.digest(value.getBytes("UTF-8"));
				return String.format("%032x", new Object[] { new BigInteger(1, e) });
			} catch (UnsupportedEncodingException var4) {
				throw new IllegalStateException("UTF-8 encoding not available.  Fatal (should be in the JDK).");
			}
		}
	}
	
	/**
	 * 현재 시간부터 설정된 유효기간을 정해 리턴.
	 * @return
	 */
	private Long expireDate() {
		return new Date().getTime() + refreshTokenExpireSecond;
	}
	
}