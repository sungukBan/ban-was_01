package com.oauth.configurer;

import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.provider.AuthorizationRequest;
import org.springframework.security.oauth2.provider.approval.ApprovalStoreUserApprovalHandler;

public class TIUserApprovalHandler extends ApprovalStoreUserApprovalHandler {
	public static final String TFSI_UTCT_TYPE_USER = "TEST-USER";  

	@Override
	public boolean isApproved(AuthorizationRequest authorizationRequest, Authentication userAuthentication) {
		/*
		if(userAuthentication instanceof AnonymousAuthenticationToken) {
			Object principal = userAuthentication.getPrincipal();
			if(principal instanceof String) {
				String user = (String) principal;
				return user.equals(TFSI_UTCT_TYPE_USER); 
			}
		}
		return authorizationRequest.isApproved();
		*/
		
		return true;
	}
	
	
}
