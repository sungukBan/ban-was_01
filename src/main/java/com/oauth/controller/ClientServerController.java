package com.oauth.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.oauth.hanaClient.HanaClientDetails;
import com.oauth.hanaClient.HanaJdbcClientDetailsService;
import org.apache.commons.collections4.MapUtils;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.provider.ClientDetails;
import org.springframework.security.oauth2.provider.NoSuchClientException;
import org.springframework.security.oauth2.provider.client.BaseClientDetails;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.transaction.Transactional;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@RestController
public class ClientServerController extends AbstOAuthController {
	
	@Autowired
    HanaJdbcClientDetailsService jdbcClientDetailsService;
	@Autowired
	PasswordEncoder passwordEncoder;
	@Autowired
	ObjectMapper objectMapper;

	@RequestMapping(value = "/oauth/api/client", method = RequestMethod.POST)
	public  String addClient(@RequestBody Map<String, Object> parameters) throws JSONException {
		JSONObject returnJson = new JSONObject();
		try {
			//직접 발급.
			String plainSecret = genRandomHexString();
			String clientId = UUID.randomUUID().toString();
//			String plainSecret = "a1d4b758856663ed7c15fe6bc522be72";
//			String clientId = "7b797248-0975-41f9-840b-67c183a4f32d";

			String secret = passwordEncoder.encode(plainSecret);
			String expireDate = MapUtils.getString(parameters, "expireDate"); //클라이언트 아이디 만료일.
			
//			String clientId = MapUtils.getString(parameters, "clientId"); //포탈에서 발급.
//			String secret = passwordEncoder.encode(MapUtils.getString(parameters, "clientSecret")); //포탈에서 발급.

			List<String> scope = getRequestParamList(parameters,"scope");

			Set<String> redirectUris ;
			if(parameters.get("redirectUris") == null) {
				redirectUris = new HashSet<String>(); //기본값으로 설정
				redirectUris.add(DEFAULT_REDIRECT_URI);
			} else {
				redirectUris = getRequestParamSet(parameters, "redirectUris");
			}

			Set<String> authorizedGrantTypes = getRequestParamSet(parameters, "authorizedGrantTypes");
			Set<String> autoapprove = getRequestParamSet(parameters, "autoapprove");
			Set<String> authorities = getRequestParamSet(parameters,"authorities");
			
			HanaClientDetails newClientDetails = new HanaClientDetails();
			newClientDetails.setClientId(clientId);
			newClientDetails.setClientSecret(secret);
			newClientDetails.setScope(scope);
			newClientDetails.setRegisteredRedirectUri(redirectUris);
			newClientDetails.setAuthorizedGrantTypes(authorizedGrantTypes);
			newClientDetails.setAutoApproveScopes(autoapprove);
			newClientDetails.setExpire_date(expireDate);
			newClientDetails.setAuthorities(AuthorityUtils.createAuthorityList(authorities.toArray(new String[authorities.size()])));
			
			jdbcClientDetailsService.addClientDetails(newClientDetails);
			returnJson.put("resultCode", 0);
			returnJson.put("clientId", clientId);
			returnJson.put("clientSecret", plainSecret);
			returnJson.put("expireDate", expireDate);
		}catch(Exception e) {
			e.printStackTrace();
			returnJson.put("resultCode", 1);
		}
		
		return returnJson.toString();
	}

	/*
	 * 직접발급하지 않고 입력된 client_id, client_secret로 발급
	 */
	@RequestMapping(value = "/oauth/api/mydataClient", method = RequestMethod.POST)
	public  ResponseEntity<?> addMydataClient(@RequestBody Map<String, Object> parameters) throws JSONException {
		JSONObject returnJson = new JSONObject();
		HttpStatus httpStatus ;
		try {
			String expireDate = MapUtils.getString(parameters, "expire_date"); //클라이언트 아이디 만료일.
			String orgCode = MapUtils.getString(parameters, "org_code"); //종합포탈에서 발급. 마이데이터 사업자 기관코드 
			String clientId = MapUtils.getString(parameters, "client_id"); //종합포탈에서 발급.
			String plainSecret = MapUtils.getString(parameters, "client_secret");
			String secret = passwordEncoder.encode(plainSecret); //종합포탈에서 발급.
			int accessTokenValiditySeconds = MapUtils.getIntValue(parameters, "access_token_validity_seconds");
			int refreshTokenValiditySeconds = MapUtils.getIntValue(parameters, "refresh_token_validity_seconds");
			List<String> scope = getRequestParamList(parameters,"scope");

			Set<String> redirectUris ;
			if(MapUtils.getString(parameters, "redirect_uris") == null) {
				redirectUris = new HashSet<String>(); //기본값으로 설정
				redirectUris.add(DEFAULT_REDIRECT_URI);
			} else {
				redirectUris = getRequestParamSet(parameters, "redirect_uris");
			}

			Set<String> authorizedGrantTypes = getRequestParamSet(parameters, "authorized_grant_types");
			Set<String> autoapprove = getRequestParamSet(parameters, "autoapprove");
			Set<String> authorities = getRequestParamSet(parameters,"authorities");
			
			HanaClientDetails newClientDetails = new HanaClientDetails();
			newClientDetails.setOrgCode(orgCode);
			newClientDetails.setClientId(clientId);
			newClientDetails.setClientSecret(secret);
			newClientDetails.setScope(scope);
			newClientDetails.setRegisteredRedirectUri(redirectUris);
			newClientDetails.setAuthorizedGrantTypes(authorizedGrantTypes);
			newClientDetails.setAutoApproveScopes(autoapprove);
			newClientDetails.setExpire_date(expireDate);
			newClientDetails.setAccessTokenValiditySeconds(accessTokenValiditySeconds);
			newClientDetails.setRefreshTokenValiditySeconds(refreshTokenValiditySeconds);
			newClientDetails.setAuthorities(AuthorityUtils.createAuthorityList(authorities.toArray(new String[authorities.size()])));
			
			jdbcClientDetailsService.addClientDetails(newClientDetails);
			returnJson.put("rsp_code", "00000");
			returnJson.put("clientId", clientId);
			returnJson.put("clientSecret", plainSecret);
			returnJson.put("expireDate", expireDate);
			
			httpStatus= HttpStatus.OK;
			
		}catch(Exception e) {
			e.printStackTrace();
			returnJson.put("rsp_code", "50004");
			returnJson.put("rsp_msg", e.getMessage());
			httpStatus = HttpStatus.INTERNAL_SERVER_ERROR;
		}
		
		return ResponseEntity.status(httpStatus).body(returnJson.toString());
	}
	
	/**
	 * @deprecated hana_client_id 롤백
	 * @param parameters
	 * @return
	 * @throws JSONException
	 */
	public  String addClient_hanaClientId(@RequestBody Map<String, Object> parameters) throws JSONException {
		JSONObject returnJson = new JSONObject();
		try {
			//직접 발급.
			String plainSecret = genRandomHexString(); 
			String clientId = UUID.randomUUID().toString(); 
			String secret = passwordEncoder.encode(plainSecret);
			String hanaClientId = "HANA_" + clientId;
			String expireDate = MapUtils.getString(parameters, "expireDate"); //클라이언트 아이디 만료일.

			List<String> scope = getRequestParamList(parameters,"scope");
			Set<String> redirectUris = getRequestParamSet(parameters, "redirectUris");
			Set<String> authorizedGrantTypes = getRequestParamSet(parameters, "authorizedGrantTypes");
			Set<String> autoapprove = getRequestParamSet(parameters, "autoapprove");
			Set<String> authorities = getRequestParamSet(parameters,"authorities");
			
			HanaClientDetails newClientDetails = new HanaClientDetails();
			newClientDetails.setClientId(clientId);
			newClientDetails.setClientSecret(secret);
			newClientDetails.setScope(scope);
			newClientDetails.setRegisteredRedirectUri(redirectUris);
			newClientDetails.setAuthorizedGrantTypes(authorizedGrantTypes);
			newClientDetails.setAutoApproveScopes(autoapprove);
			newClientDetails.setExpire_date(expireDate);
			newClientDetails.setAuthorities(AuthorityUtils.createAuthorityList(authorities.toArray(new String[authorities.size()])));
			newClientDetails.setHana_client_id(hanaClientId);
			newClientDetails.setHana_client_seq(1);
			
			jdbcClientDetailsService.addClientDetails(newClientDetails);
			returnJson.put("resultCode", 0);
			returnJson.put("clientId", hanaClientId);
			returnJson.put("clientSecret", plainSecret);
			returnJson.put("expireDate", expireDate);
		}catch(Exception e) {
			e.printStackTrace();
			returnJson.put("resultCode", 1);
		}
		return returnJson.toString();
	}
	
	public static String genRandomHexString() {
		Random random = new Random();
		StringBuilder sb = new StringBuilder();
		while (sb.length() < 32) {//32byte
			sb.append(Integer.toHexString(random.nextInt()));
		}
		return sb.toString();
	}
	
	/**
	 * @deprecated
	 * client 정보의 수정은 client_secret 재발급만 제공.
	 * @param parameters
	 * @return
	 * @throws JSONException
	 * 
	 */
//	@RequestMapping(value = "/oauth/api/client", method = RequestMethod.PUT)
	public  String editClient(@RequestBody Map<String, Object> parameters) throws JSONException {
		
		JSONObject returnJson = new JSONObject();
		
		try {
			String clientId = MapUtils.getString(parameters, "clientId");
			List<String> scope = getRequestParamList(parameters,"scope");
			Set<String> redirectUris = getRequestParamSet(parameters, "redirectUris");
			Set<String> authorizedGrantTypes = getRequestParamSet(parameters, "authorizedGrantTypes");
			Set<String> authorities = getRequestParamSet(parameters,"authorities");
			
			BaseClientDetails newClientDetails = new BaseClientDetails();
			newClientDetails.setClientId(clientId);
			newClientDetails.setScope(scope);
			newClientDetails.setRegisteredRedirectUri(redirectUris);
			newClientDetails.setAuthorizedGrantTypes(authorizedGrantTypes);
			newClientDetails.setAuthorities(AuthorityUtils.createAuthorityList(authorities.toArray(new String[authorities.size()])));
			
			jdbcClientDetailsService.updateClientDetails(newClientDetails);
			returnJson.put("resultCode", 0);
		}catch(Exception e) {
			e.printStackTrace();
			returnJson.put("resultCode", 1);
		}
		return returnJson.toString();
	}
	
	@RequestMapping(value = "/oauth/api/client/expireDate", method = RequestMethod.POST)
	public  String changeClientExpireDate(@RequestBody Map<String, Object> parameters) throws JSONException {
		JSONObject returnJson = new JSONObject();
		try {
			String clientId = MapUtils.getString(parameters, "clientId");
			String expireDate = MapUtils.getString(parameters, "expireDate");
			
//			ClientDetails dbLatestClientDetails = jdbcClientDetailsService.getLatestOauthClient(hanaClientId);
//			String clientId = dbLatestClientDetails.getClientId();
			
			jdbcClientDetailsService.updateClientExpireDate(clientId, expireDate);
			returnJson.put("resultCode", 0);
			returnJson.put("clientId", clientId);
			returnJson.put("expireDate", expireDate);
		}catch(NoSuchClientException e) {
			//client not found
			returnJson.put("resultCode", 2);
		}
		catch(Exception e) {
			e.printStackTrace();
			returnJson.put("resultCode", 1);
		}
		return returnJson.toString();
	}
	/**
	 * @deprecated hana_client_id 롤백
	 * @param parameters
	 * @return
	 * @throws JSONException
	 */
	public  String changeClientExpireDate_hanaClientId(@RequestBody Map<String, Object> parameters) throws JSONException {
		JSONObject returnJson = new JSONObject();
		try {
			String hanaClientId = MapUtils.getString(parameters, "clientId");
			String expireDate = MapUtils.getString(parameters, "expireDate");
			ClientDetails dbLatestClientDetails = jdbcClientDetailsService.getLatestOauthClient(hanaClientId);
			String clientId = dbLatestClientDetails.getClientId();
			
			jdbcClientDetailsService.updateClientExpireDate(clientId, expireDate);
			returnJson.put("resultCode", 0);
			returnJson.put("clientId", hanaClientId);
			returnJson.put("expireDate", expireDate);
		}catch(NoSuchClientException e) {
			//client not found
			returnJson.put("resultCode", 2);
		}
		catch(Exception e) {
			e.printStackTrace();
			returnJson.put("resultCode", 1);
		}
		return returnJson.toString();
	}

	@Transactional
	@RequestMapping(value = "/oauth/api/client/newPassword", method = RequestMethod.POST)
	public  String newPassword(@RequestBody Map<String, Object> parameters) throws JSONException {
		JSONObject returnJson = new JSONObject();
		String clientId = MapUtils.getString(parameters, "clientId");
		String expireDate = MapUtils.getString(parameters, "expireDate"); //portal로부터 받아옴.
		try {
			String newClientId = UUID.randomUUID().toString(); 
			String plainSecret = genRandomHexString(); 
			String secret = passwordEncoder.encode(plainSecret);
 			
			HanaClientDetails dbLatestClientDetails = (HanaClientDetails) jdbcClientDetailsService.loadClientByClientId(clientId);
					
			//신규 client_id, client_secret 발급.
			HanaClientDetails newClientDetails = new HanaClientDetails(dbLatestClientDetails);
			newClientDetails.setClientId(newClientId);
			newClientDetails.setClientSecret(secret);
			newClientDetails.setAutoApproveScopes(dbLatestClientDetails.getAutoApproveScopes());
			newClientDetails.setExpire_date(dbLatestClientDetails.getExpire_date());
			jdbcClientDetailsService.addClientDetails(newClientDetails);
			
			dbLatestClientDetails.setExpire_date(expireDate);
			jdbcClientDetailsService.updateClientDetails(dbLatestClientDetails);

			returnJson.put("resultCode", 0);
			returnJson.put("clientId", newClientId);
			returnJson.put("clientSecret", plainSecret);
		}catch(NoSuchClientException e) {
			//client not found
			returnJson.put("resultCode", 2);
		}
		catch(Exception e) {
			e.printStackTrace();
			returnJson.put("resultCode", 1);
		}
		return returnJson.toString();
	}
	
	/**
	 * @deprecated hana_client_id 롤백
	 * @param parameters
	 * @return
	 * @throws JSONException
	 */
	public  String newPassword_hanaClientId(@RequestBody Map<String, Object> parameters) throws JSONException {
		JSONObject returnJson = new JSONObject();
		String hanaClientId = MapUtils.getString(parameters, "clientId");
		try {
			//직접 발급.
			String plainSecret = genRandomHexString(); 
			String secret = passwordEncoder.encode(plainSecret);
 			String clientId = UUID.randomUUID().toString();
 			
			HanaClientDetails dbLatestClientDetails = (HanaClientDetails) jdbcClientDetailsService.getLatestOauthClient(hanaClientId);
			Integer currentSeq = dbLatestClientDetails.getHana_client_seq();
					
			//신규 client_id, client_secret 발급.
			HanaClientDetails newClientDetails = new HanaClientDetails(dbLatestClientDetails);
			newClientDetails.setClientId(clientId);
			newClientDetails.setClientSecret(secret);
			newClientDetails.setAutoApproveScopes(dbLatestClientDetails.getAutoApproveScopes());
			newClientDetails.setHana_client_seq(currentSeq+1);
			newClientDetails.setHana_client_id(hanaClientId);
			newClientDetails.setExpire_date(dbLatestClientDetails.getExpire_date());
			jdbcClientDetailsService.addClientDetails(newClientDetails);
			
			DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd");
			LocalDateTime dbExpireDate = LocalDate.parse(dbLatestClientDetails.getExpire_date(), formatter).atStartOfDay();
			LocalDateTime newExpireDate = LocalDate.now().atStartOfDay().plusDays(3);
			
			if(dbExpireDate.compareTo(newExpireDate) > 0 ) {
				dbLatestClientDetails.setExpire_date(newExpireDate.format(formatter));
				jdbcClientDetailsService.updateClientDetails(dbLatestClientDetails);
			} 

			returnJson.put("resultCode", 0);
			returnJson.put("clientId", hanaClientId);
			returnJson.put("clientSecret", plainSecret);
		}catch(NoSuchClientException e) {
			//client not found
			returnJson.put("resultCode", 2);
		}
		catch(Exception e) {
			e.printStackTrace();
			returnJson.put("resultCode", 1);
		}
		return returnJson.toString();
	}
	
	
	// 보안 대응건으로 불필요 메소드 삭제
//	@RequestMapping(value = "/oauth/api/client", method = RequestMethod.GET)
	public  String getClient() throws JSONException, JsonProcessingException {
		Map<String , Object> returnMap = new HashMap<String,Object>();
		try {
			List<ClientDetails> list = jdbcClientDetailsService.listClientDetails();
			returnMap.put("resultCode", 0);
			returnMap.put("list", list);
		}catch(Exception e) {
			e.printStackTrace();
			returnMap.put("resultCode", 1);
		}
		return objectMapper.writeValueAsString(returnMap);
	}
	
	// 보안 대응건으로 불필요 메소드 삭제
//	@RequestMapping(value = "/oauth/api/client/deleteClient", method = RequestMethod.POST)
	public  String deleteClient(@RequestBody Map<String, Object> parameters) throws JSONException {
		JSONObject returnJson = new JSONObject();
		String clientId = MapUtils.getString(parameters, "clientId");
		try {
			jdbcClientDetailsService.removeClientDetails(clientId);
			returnJson.put("resultCode", 0);
			returnJson.put("clientId", clientId);
		}catch(NoSuchClientException e) {
			//client not found
			returnJson.put("resultCode", 2);
		}catch(Exception e) {
			e.printStackTrace();
			returnJson.put("resultCode", 1);
		}
		return returnJson.toString();
	}
	
	private boolean validateGrantType(Set<String> grantTypes) {
		Boolean result = false;
		Iterator<String>  iter = grantTypes.iterator();
		while(iter.hasNext()) {
			String grantType = iter.next();
			if(grantType.equals("client_credentials")) {
				result = true;
			}else if(grantType.equals("authorization_code")) {
				result = true;
			}else if(grantType.equals("password")) {
				result = true;
			}else if(grantType.equals("implicit")) {
				result = true;
			}
		}
		return result;
	}
	
	@SuppressWarnings("unchecked")
	private List<String> getRequestParamList(Map<String, Object> parameters, String key){
		if(parameters.get(key) == null) {
			return new ArrayList<String>();
		}
		
		List<String> result;
		if(parameters.get(key) instanceof String) {
			result = new ArrayList<String>();
			result.add((String) parameters.get(key));
		}else {
			result = (List<String>) parameters.get(key);
		}
		return result;
	}
	
	@SuppressWarnings("unchecked")
	private Set<String> getRequestParamSet(Map<String, Object> parameters, String key){
		if(parameters.get(key) == null) {
			return new HashSet<String>();
		}
		
		Set<String> result = new HashSet<String>();
		if(parameters.get(key) instanceof String){
			result.add((String) parameters.get(key));
		}else {
			ArrayList<String> list = (ArrayList<String>) parameters.get(key);
			for(String value : list) {
				result.add(value);
			}	
		}
		return result;
	}
	
}

