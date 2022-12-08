package com.oauth.configurer;

import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.AuthorityUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Optional;

public class IntegratedLoginAuthenticationProvider implements AuthenticationProvider {

	@Override
	public Authentication authenticate(Authentication authentication) throws AuthenticationException {
		//통합 인증은 인증 서버에서 사용자 인증을 하기 때문에 
		//여기에서는 별도의 인증 채크는 하지 않고 권한만 넣어서 패스 시킴. 
		String name = authentication.getName();
	    Object details = authentication.getDetails();
	    
	    List<GrantedAuthority> authoritys = null;
	    if(details instanceof HashMap) {
	    	HashMap map = (HashMap) details;
	    	
	    	Object scope = map.get("scope");
	    	if(scope != null && scope instanceof String) {
	    		authoritys = getAuthority((String) scope);
	    	}
	    }
	    return new UsernamePasswordAuthenticationToken(name, null, authoritys);
	}

	@Override
	public boolean supports(Class<?> authentication) {
		return authentication.equals(
                UsernamePasswordAuthenticationToken.class);
	}
	
	/**
	 * scope를 파싱하여 authority 리스트로 리턴.
	 * @param scope
	 * @return
	 */
	public List<GrantedAuthority> getAuthority(String scope) {
		String newScope = Optional.ofNullable(scope).orElse("default");
		String[] scopes = newScope.split(" ");

		List<GrantedAuthority> authorities = null;
		if (scopes != null && scopes.length > 0) {
			authorities = AuthorityUtils.createAuthorityList(scopes);
		}
		return authorities;
	}

}
