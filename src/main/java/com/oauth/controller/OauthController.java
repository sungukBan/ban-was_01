package com.oauth.controller;

import com.oauth.log.CasdTokenLog;
import com.oauth.http.TIHttpStatus;
import com.oauth.model.ErrorResponse;
import com.oauth.service.OauthService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.common.OAuth2AccessToken;
import org.springframework.security.oauth2.common.exceptions.InvalidGrantException;
import org.springframework.security.oauth2.common.exceptions.InvalidScopeException;
import org.springframework.security.oauth2.common.exceptions.RedirectMismatchException;
import org.springframework.security.oauth2.provider.NoSuchClientException;
import org.springframework.security.oauth2.provider.endpoint.FrameworkEndpoint;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.support.SessionStatus;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.servlet.view.RedirectView;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.nio.charset.Charset;
import java.security.Principal;
import java.util.*;

@RequestMapping("/2.0")
@RestController
@FrameworkEndpoint
@SessionAttributes("authorizationRequest")
public class OauthController extends AbstOAuthController {
    private static final Logger log = LoggerFactory.getLogger(OauthController.class);
    
    
    private final Set<String> PARAM_AUTHORIZE = new HashSet<String>(Arrays.asList("org_code", "response_type", "client_id", "redirect_uri", "state"));
    private final Set<String> PARAM_REVOKE = new HashSet<String>(Arrays.asList("org_code", "token", "client_id", "client_secret"));
    private final Set<String> PARAM_TOKEN_REQUEST = new HashSet<String>(Arrays.asList("org_code", "grant_type", "code", "client_id", "client_secret", "redirect_uri"));
    private final Set<String> PARAM_TOKEN_REFRESH = new HashSet<String>(Arrays.asList("org_code", "grant_type", "refresh_token", "client_id", "client_secret"));
    private final Set<String> PARAM_TOKEN_PASSWORD = new HashSet<String>(Arrays.asList("tx_id", "org_code", "grant_type", "client_id", "client_secret", "ca_code", "consent_type", "username", "password_len", "password", "auth_type", "consent_nonce"));
    private final Set<String> PARAM_TOKEN_AUTHORIZATION_CODE = new HashSet<String>(Arrays.asList("org_code", "grant_type", "code", "client_id", "client_secret", "redirect_uri"));
        
    
    @Value("${spring.profiles.active}")
    private String activeProfile;
    
    @Autowired
    private OauthService oauthService;
    
    
    
    @RequestMapping(value = "/authorize")
    @ResponseBody
    public Object authorize(Map<String, Object> model, @RequestParam Map<String, String> parameters,
			SessionStatus sessionStatus, Principal principal, HttpServletRequest request, RedirectAttributes redirect) {
    	boolean isValid = validParam(PARAM_AUTHORIZE, parameters);
    	
    	if(!isValid) {
    		//필수 파라메터가 없는 경우.
    		return createResponseEntity(TIHttpStatus.INVALID_REQUEST_CODE, HttpStatus.FOUND, "Required parameter error.");
		}
		
    	String clientId = parameters.get("client_id");
    	String redirectUri = parameters.get("redirect_uri").replace("%3A", ":").replace("%2F", "/");
    	String responseType = parameters.get("response_type");
    	if(responseType == null || !responseType.equals("code")) {
    		return createResponseEntity(TIHttpStatus.UNSUPPORTED_RESPONSE_TYPE, HttpStatus.FOUND);
    	}
    	
    	Object result = null;
    	try {
    		result = oauthService.authorize(parameters, principal, request, redirect);
    	} catch (Exception e) {
    		if(e instanceof NoSuchClientException) {
    			//등록되지 않은 Client ID일 경우
    			return createResponseEntity(TIHttpStatus.INVALID_REQUEST_CODE, HttpStatus.FOUND, "No client with requested id: " + clientId);
    		} else if(e instanceof RedirectMismatchException) {
    			//등록된 redirect url과 일치하지 않는 경우. 
        		return createResponseEntity(TIHttpStatus.INVALID_REQUEST_CODE, HttpStatus.FOUND, "Invalid redirect: " + redirectUri
        				+ " does not match one of the registered values: ");
    		} else {
    			log.error("[OauthController] - ", e);
    		}
    		
		}
    	return result;
    }
    
    
    
    @GetMapping(value = "/revoke")
    @ResponseBody
    public Object revoke(HttpServletRequest request
            , @RequestParam Map<String, String> parameters) {
    	
    	boolean isValid = validParam(PARAM_REVOKE, parameters);
    	
    	if(!isValid) {
    		//필수 파라메터가 없는 경우.
    		return createResponseEntity(TIHttpStatus.INVALID_REQUEST_ACCESS, HttpStatus.BAD_REQUEST);
		}
    	
    	String token = parameters.get("token");
    	String tokenTypeHint = parameters.get("token_type_hint");
    	
    	String orgCode = parameters.get("org_code");
    	String clientId = parameters.get("client_id");
    	String clientSecret = parameters.get("client_secret");
    	
    	boolean result = false;
    	if(tokenTypeHint != null) {
    		if(tokenTypeHint.equals("access_token") || tokenTypeHint.equals("refresh_token")) {
    			result = oauthService.revoke(tokenTypeHint, token);
    		} else {
    			return createResponseEntity(TIHttpStatus.UNSUPPORTED_TOKEN_TYPE, HttpStatus.BAD_REQUEST, "unsupported token type hint: " + tokenTypeHint);
    		}
    	} else {
    		result = oauthService.revoke(tokenTypeHint, token);
    	}
    	
    	if(activeProfile.equals("card")) {
			//카드사 토큰 발급 이력 남기기.
    		String msg = String.format("Revoke token - org_code: %s, token_type: %s, client_id: %s, token: %s", 
    				orgCode, tokenTypeHint, clientId, token);
    		CasdTokenLog.print(msg);
    	}
    	
    	if(!result) {
    		return createResponseEntity(TIHttpStatus.INVALID_TOKEN, HttpStatus.BAD_REQUEST, "invalid token: " + token);
    	}
		return new ResponseEntity(HttpStatus.OK);
    }
    
    
    
    @PostMapping(value = "/check_token")
    @ResponseBody
    public String checkToken(@RequestParam String token) throws IOException {
    	if(token != null) {
    		return oauthService.checkToken(token);
    	}
    	return "invalid";
    }
    
    @RequestMapping(value = "/token")
	public Object postAccessToken(Principal principal, @RequestParam
			Map<String, String> parameters) {
    	
    	boolean isValid = true;
    	String grantType = parameters.get("grant_type");
    	
    	if(grantType.equals("password")) {
    		isValid = validParam(PARAM_TOKEN_PASSWORD, parameters);
    	} else if(grantType.equals("authorization_code")) {
    		isValid = validParam(PARAM_TOKEN_AUTHORIZATION_CODE, parameters);
    	} else if(grantType.equals("refresh_token")) {
    		isValid = validParam(PARAM_TOKEN_REFRESH, parameters);
    	} else {
    		return createResponseEntity(TIHttpStatus.UNSUPPORTED_GRANT_TYPE, HttpStatus.BAD_REQUEST, "Unsupported grant type.");
    	}
    	
    	if(!isValid) {
    		//필수 파라메터가 없는 경우.
    		return createResponseEntity(TIHttpStatus.INVALID_REQUEST_ACCESS, HttpStatus.BAD_REQUEST, "Required parameter error.");
		}
    	
		try {
			ResponseEntity response = oauthService.accessToken(principal, parameters);
			Object body = response.getBody();
			if(body != null && body instanceof OAuth2AccessToken) {
				OAuth2AccessToken token = (OAuth2AccessToken) body;
				
				
				if(activeProfile.equals("card")) {
					//카드사 토큰 발급 이력 남기기.
					String orgCode = parameters.get("org_code");
					String msg = String.format("Create token - org_code: %s, grant_type: %s, token: %s", 
							orgCode, grantType, token.getValue());
			    	CasdTokenLog.print(msg);
				}
				
				Map<String, Object> map = new HashMap<>();
		        map.put("token_type", "Bearer");
		        map.put("access_token", token.getValue());
		        map.put("expires_in", token.getExpiresIn());
		        
		        Set<String> scope = token.getScope();
		        if(scope != null) {
		        	map.put("scope", oauthService.readScope(scope));
		        }
		        if(!grantType.equals("client_credentials")) {
		        	map.put("refresh_token", token.getRefreshToken().getValue());
			        map.put("refresh_token_expires_in", oauthService.refreshTokenExpiresIn(token));
		        }
		        return ResponseEntity.ok(map);
		        
			}
			return response;
			
		} catch (InvalidGrantException e) {
			return createResponseEntity(TIHttpStatus.INVALID_GRANT, HttpStatus.BAD_REQUEST, e.getMessage());
		} catch (InvalidScopeException e) {
			return createResponseEntity(TIHttpStatus.INVALID_SCOPE, HttpStatus.BAD_REQUEST, e.getMessage());
		} catch (HttpRequestMethodNotSupportedException e) {
			log.error("[OauthController] - ", e);
		} catch (Exception e) {
			log.error("[OauthController] - ", e);
		}
		
		return createResponseEntity(TIHttpStatus.ERROR, HttpStatus.SERVICE_UNAVAILABLE, "");
    }
    
    
    
    @GetMapping(value = "/clientId")
    @ResponseBody
    public String getClientId(@RequestParam String token) {
    	return oauthService.getClientId(token);
    }
    
    /**
     * Code 발급해서 Redirect
     * @param parameters
     * @return
     * @throws IOException
     */
    @RequestMapping(value = "/code_redirect")
    @ResponseBody
    public Object codeRedirect(Map<String, Object> model, 
    		@RequestParam Map<String, String> parameters, SessionStatus sessionStatus, RedirectAttributes redirect) {
    	
    	String ci = parameters.get("ci");
    	String clientId = parameters.get("client_id");
    	String scope = parameters.get("scope");
    	String state = parameters.get("state");
    	String redirectUri = parameters.get("redirect_uri");
    	if(ci != null) {
    		String code = oauthService.generateCode(clientId, ci, redirectUri, scope);
			
			Map<String, Object> map = new HashMap<>();
	        map.put("code", code);
	        map.put("state", state);
	        
	        redirect.addAllAttributes(map);
    		return new RedirectView(redirectUri);
    	}
    	
    	log.error("[OauthController] - /code_redirect: ci is null.");
    	return null;
    }
    
    /**
     * 바로 Redirect
     * @param parameters
     * @return
     * @throws IOException
     */
    @RequestMapping(value = "/code_issue")
    @ResponseBody
    public Object codeIssue(Map<String, Object> model, 
    		@RequestParam Map<String, String> parameters, SessionStatus sessionStatus) {
    	
    	boolean isValid = validParam(PARAM_AUTHORIZE, parameters);
    	if(!isValid) {
    		//필수 파라메터가 없는 경우.
    		return createResponseEntity(TIHttpStatus.INVALID_REQUEST_CODE, HttpStatus.FOUND, "Required parameter error.");
		}
    	
    	String ci = parameters.get("ci");
    	String clientId = parameters.get("client_id");
    	String redirectUri = parameters.get("redirect_uri");
    	String scope = parameters.get("scope");
    	String state = parameters.get("state");
    	      	
    	String code = oauthService.generateCode(clientId, ci, redirectUri, scope);
        Map<String, Object> map = new HashMap<>();
        map.put("code", code);
        map.put("state", state);
        return ResponseEntity.ok(map);
    }

    
    private boolean validParam(Set<String> notBlanks, Map<String, String> parameters) {
    	boolean isValid = true;
    	Iterator<String> iter = notBlanks.iterator();
    	while(iter.hasNext()) {
    		String notBlankParam = iter.next();
    		
    		Object value = parameters.get(notBlankParam);
    		if(value == null) {
    			isValid = false;
    			break;
    		}
    	}
    	return isValid;
    }
    
    /**
     * 응답 전문 생성.
     * @param status
     * @return
     */    
    public ResponseEntity<ErrorResponse> createResponseEntity(TIHttpStatus tiStatus, HttpStatus status) {
    	return createResponseEntity(tiStatus, status, null);
    }
    
    public ResponseEntity<ErrorResponse> createResponseEntity(TIHttpStatus tiStatus, HttpStatus status, String message) {
    	HttpHeaders headers= new HttpHeaders();
        headers.setContentType(new MediaType("application", "json", Charset.forName("UTF-8")));
    	
    	ErrorResponse response = new ErrorResponse(tiStatus);
    	response.setError_description(message);
    	return new ResponseEntity<>(response, headers, status);
    }
    
}
