package com.oauth.service;

import com.google.gson.JsonObject;
import com.oauth.configurer.TITokenStore;
import com.oauth.configurer.TIAuthorizationCodeServices;
import com.oauth.configurer.TITokenService;
import com.oauth.feign.MDServiceProxy;
import com.oauth.hanaClient.HanaJdbcClientDetailsService;
import com.oauth.model.OAuthRefreshToken;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.oauth2.common.OAuth2AccessToken;
import org.springframework.security.oauth2.common.OAuth2RefreshToken;
import org.springframework.security.oauth2.provider.*;
import org.springframework.security.oauth2.provider.endpoint.DefaultRedirectResolver;
import org.springframework.security.oauth2.provider.endpoint.RedirectResolver;
import org.springframework.security.oauth2.provider.endpoint.TokenEndpoint;
import org.springframework.security.oauth2.provider.request.DefaultOAuth2RequestFactory;
import org.springframework.security.oauth2.provider.token.TokenStore;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.servlet.view.RedirectView;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletRequest;
import javax.sql.DataSource;
import java.io.IOException;
import java.nio.charset.Charset;
import java.security.Principal;
import java.util.*;

@Service
public class OauthService {

	private static final Logger log = LoggerFactory.getLogger(OauthService.class);

	@Autowired
	DataSource dataSource;

	TIAuthorizationCodeServices codeService;

	@Autowired
	HanaJdbcClientDetailsService hanaJdbcClientDetailsService;
	
	@Autowired
	private TokenStore tokenStore;
	
	@Autowired
    private TokenEndpoint tokenEndpoint;
	
	@Autowired
	private MDServiceProxy mdServiceProxy;
	
	@Autowired
	private ClientDetailsService clientDetailsService;
	
	@Autowired
    private TITokenService tokenService;

	private RedirectResolver redirectResolver = new DefaultRedirectResolver();
	
	@Value("${login-page}")
	private String loginPage;
	

	@PostConstruct
	public void init() {
		codeService = new TIAuthorizationCodeServices(dataSource);
	}
	
	
	public ResponseEntity accessToken(Principal principal, Map<String, String> parameters) throws Exception {
		String grantType = parameters.get("grant_type");
		
		ResponseEntity entity = null;
		if(grantType.equals("password")) {
    		//통합인증
			/*
			ResponseEntity response = mdServiceProxy.checkCA(parameters);
			if(response.getStatusCode() == HttpStatus.OK) {
				entity = tokenEndpoint.postAccessToken(principal, parameters);
			} else {
				//정상 응답(200)이 아닌 경우에 그대로 리턴.
				entity = response;
			}
			*/
			entity = tokenEndpoint.postAccessToken(principal, parameters);
    	} else if(grantType.equals("authorization_code")) {
    		//개별인증
    		entity = tokenEndpoint.postAccessToken(principal, parameters);
    	} else if(grantType.equals("refresh_token")) {
    		//토큰 갱신
    		entity = tokenEndpoint.postAccessToken(principal, parameters);
    	}		
		return entity;
	}

	/**
	 * Authorization Code 생성
	 * 
	 * @param clientId
	 * @param ci
	 * @param uuid
	 * @param affiliateCode
	 * @param redirectUri
	 * @param scope
	 * @return
	 */
	public String generateCode(String clientId, String ci, String redirectUri, String scope) {

		// AuthorizationRequest
		AuthorizationRequest authorizationRequest = new AuthorizationRequest();

		// - ApprovalParameters
		Map<String, String> approvalPrameters = new HashMap<>();
		approvalPrameters.put("user_oauth_approval", "true");
		approvalPrameters.put("authorize", "Authorize");
		authorizationRequest.setApprovalParameters(approvalPrameters);

		// - ResponseTypes
		Set<String> responseTypes = new TreeSet<>();
		responseTypes.add("code");
		authorizationRequest.setResponseTypes(responseTypes);

		// - Authorities
		scope = Optional.ofNullable(scope).orElse("default");
		String[] scopes = scope.split(" ");

		List<GrantedAuthority> authorities = null;
		if (scopes != null && scopes.length > 0) {
			authorities = AuthorityUtils.createAuthorityList(scopes);
			authorizationRequest.setAuthorities(authorities);
		}

		// - Approved
		authorizationRequest.setApproved(false);

		// - RedirectUr
		authorizationRequest.setRedirectUri(redirectUri);

		// - ClientId
		authorizationRequest.setClientId(clientId);

		// - Scope
		List<String> scopeList = Arrays.asList(scopes);
		authorizationRequest.setScope(scopeList);

		// - Approved
		authorizationRequest.setApproved(true);

		// OAuth2RequestFactory
		OAuth2RequestFactory oAuth2RequestFactory = new DefaultOAuth2RequestFactory(hanaJdbcClientDetailsService);

		OAuth2Request storedOAuth2Request = oAuth2RequestFactory.createOAuth2Request(authorizationRequest);

		UsernamePasswordAuthenticationToken usernamePasswordAuthenticationToken = new UsernamePasswordAuthenticationToken(
				ci, null, authorities);

		OAuth2Authentication combinedAuth = new OAuth2Authentication(storedOAuth2Request,
				(Authentication) usernamePasswordAuthenticationToken);

		String code = codeService.createAuthorizationCode(combinedAuth);

		log.debug("code: " + code);
		return code;
	}
	
	public long refreshTokenExpiresIn(OAuth2AccessToken accessToken) {
    	OAuth2RefreshToken refreshToke = accessToken.getRefreshToken();
		if(refreshToke != null) {
			OAuthRefreshToken tokenInfo = ((TITokenStore)tokenStore).getRefreshToken(refreshToke);
			if(tokenInfo != null) {
				long expireTime = (tokenInfo.getExpireDate().getTime() - new Date().getTime()) / 1000;
				return expireTime;
			}
			
		}
		return -1l;
    }
	
	/**
	 * Authorize code 생성.
	 * @param parameters
	 * @param principal
	 * @param request
	 * @param redirect
	 * @return
	 * @throws Exception
	 */
	public Object authorize(Map<String, String> parameters,
			Principal principal, HttpServletRequest request, RedirectAttributes redirect) throws Exception {
		
		String clientId = parameters.get("client_id");
    	String redirectUri = parameters.get("redirect_uri").replace("%3A", ":").replace("%2F", "/");
    	String scope = parameters.get("scope");
    	String state = parameters.get("state");
		
		
		ClientDetails client = clientDetailsService.loadClientByClientId(clientId);
		redirectResolver.resolveRedirect(redirectUri, client);
		
		if (!(principal instanceof Authentication) || !((Authentication) principal).isAuthenticated()) {
    		//로그인이 필요한 경우.
    		if(isSkipLogin(request)) {
    			//해당 헤더가 존재할 경우 코드 바로 발급(마이데이터 테스트배드용.)
    			//String ci = TIUserApprovalHandler.TFSI_UTCT_TYPE_USER;
    			String ci = "hclee";
    			String code = generateCode(clientId, ci, redirectUri, scope);
		        
		        HttpHeaders headers= new HttpHeaders();
		        headers.setContentType(new MediaType("application", "x-www-form-urlencoded", Charset.forName("UTF-8")));
		        
		        JsonObject obj = new JsonObject();
		        obj.addProperty("code", code);
		        obj.addProperty("state", state);
		        
		        String body = obj.toString();
		    	return new ResponseEntity<>(body, headers, HttpStatus.OK);    			
    		} else {
    			//로그인 페이지로 이동.
    			String referer = request.getHeader("referer");
    			parameters.put("referer", referer);    			
    			redirect.addAllAttributes(parameters);
        		return new RedirectView(loginPage);
    		}
		}
		return null;
	}
	
	/**
	 * token revoke
	 * @param tokenTypeHint
	 * @param token
	 * @return
	 */
	public boolean revoke(String tokenTypeHint, String token) {
		boolean result = false;
    	if(tokenTypeHint != null) {
    		if(tokenTypeHint.equals("access_token")) {
    			result = tokenService.revokeAccessToken(token);
    		} else if(tokenTypeHint.equals("refresh_token")) {
    			result = tokenService.revokeRefreshToken(token);
    		}
    	} else {
    		tokenTypeHint = "access_token|refresh_token";
    		result = tokenService.revokeToken(token);
    	}
    	return result;
	}
	
	/**
	 * 토큰 체크
	 * @param token
	 * @return
	 * @throws IOException
	 */
	public String checkToken(String token) throws IOException {
    	OAuth2AccessToken accessToken = tokenService.readAccessToken(token);
    	if(accessToken != null) {
    		return accessToken.isExpired() ? "invalid":"valid";
    	}
    	return "revoked";
    }
	
	/**
	 * 클라이언트 아이디 리턴
	 * @param token
	 * @return
	 * @throws IOException
	 */
	public String getClientId(@RequestParam String token) {
    	try {
    		return tokenService.getClientId(token);
    	} catch (Exception e) {
    		log.error("Token Error", e);
		}
    	return null;
    }
	
	/**
	 * scope 리스트를 문자열로 변경하여 리턴
	 * @param scopes
	 * @return
	 */
	public String readScope(Set<String> scopes) {
		String scope = String.join(" ", scopes);
		return scope;
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
	
	/**
     * 마이데이터 표준에서 정하고있는 로그인 건너뛰기 헤더 정보 채크.
     * @param request
     * @return
     */
    private boolean isSkipLogin(HttpServletRequest request) {
    	String xFsiType = request.getHeader("X-FSI-UTCT-TYPE");
		if(xFsiType != null && xFsiType.equals("TGC00001")) {
			return true;
		}
		return false;
    }
}
