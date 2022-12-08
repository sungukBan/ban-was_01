package com.oauth.model;

import org.apache.commons.codec.binary.Base64;
import org.springframework.security.oauth2.common.OAuth2RefreshToken;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.util.SerializationUtils;

import java.util.Date;

public class OAuthRefreshToken {
	private String tokenId;
	private String token;
	private String authentication;
	private String username;
	private String clientId;
	private Date expireDate;
	
	
	public String getTokenId() {
		return tokenId;
	}

	public void setTokenId(String tokenId) {
		this.tokenId = tokenId;
	}

	public String getToken() {
		return token;
	}

	public void setToken(String token) {
		this.token = token;
	}

	public String getAuthentication() {
		return authentication;
	}

	public void setAuthentication(String authentication) {
		this.authentication = authentication;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getClientId() {
		return clientId;
	}

	public void setClientId(String clientId) {
		this.clientId = clientId;
	}

	public Date getExpireDate() {
		return expireDate;
	}

	public void setExpireDate(Date expireDate) {
		this.expireDate = expireDate;
	}
	
	public void setTokenObject(OAuth2RefreshToken token) {
		this.token = serialize(token);
	}
	
	public void setAuthenticationObject(OAuth2Authentication auth) {
		this.authentication = serialize(auth);
	}
	
	
	
	public OAuth2RefreshToken getTokenObject() {
		if(token != null) {
			Object obj = deserialize(token);
			if(obj != null) {
				return (OAuth2RefreshToken) obj;
			}
		}
		return null;
	}
	
	public OAuth2Authentication getAuthenticationObject() {
		if(authentication != null) {
			Object obj = deserialize(authentication);
			if(obj != null) {
				return (OAuth2Authentication) obj;
			}
		}
		return null;
	}
	
	public static String serialize(Object object) {
        byte[] bytes = SerializationUtils.serialize(object);
        return Base64.encodeBase64String(bytes);
    }

    public static Object deserialize(String encodedObject) {
        byte[] bytes = Base64.decodeBase64(encodedObject);
        return SerializationUtils.deserialize(bytes);
    }
}
