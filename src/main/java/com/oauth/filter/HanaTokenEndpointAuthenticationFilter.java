package com.oauth.filter;

import org.apache.commons.collections4.MapUtils;
import org.apache.tomcat.util.codec.binary.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.provider.NoSuchClientException;
import org.springframework.security.oauth2.provider.OAuth2RequestFactory;
import org.springframework.security.oauth2.provider.endpoint.TokenEndpointAuthenticationFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.sql.DataSource;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class HanaTokenEndpointAuthenticationFilter extends TokenEndpointAuthenticationFilter {
	
    private static final Logger logger = LoggerFactory.getLogger(HanaTokenEndpointAuthenticationFilter.class);
    
    PasswordEncoder passwordEncoder;
    private final JdbcTemplate jdbcTemplate;
    
    private final String selectClientId = "select client_id, client_secret from oauth_client_details where hana_client_id = ? and expire_date >= ?";

	public HanaTokenEndpointAuthenticationFilter(AuthenticationManager authenticationManager,
			OAuth2RequestFactory oAuth2RequestFactory,
			PasswordEncoder passwordEncoder,
			DataSource dataSource) {
		super(authenticationManager, oAuth2RequestFactory);
		this.passwordEncoder = passwordEncoder;
		this.jdbcTemplate = new JdbcTemplate(dataSource);
	}

	@Override
	protected void onSuccessfulAuthentication(HttpServletRequest request, HttpServletResponse response,
			Authentication authResult) throws IOException {
	}

	@Override
	public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain) throws IOException, ServletException {
		boolean condition = true;
		
		String requestUri = getCurrentUrlFromRequest(req);
		logger.info("===== CustomTokenEndpointAuthenticationFilter Start =====");
		logger.info("requestUri : {}", requestUri);
		
		HanaHttpServletRequestWrapper hanaHttpServletRequestWrapper = new HanaHttpServletRequestWrapper((HttpServletRequest) req);
		
		if (requestUri.equals("/oauth/token")) {
			// 신규 생성 테이블 조회 및 client_id 치환 처리..
			
			//reqest header에서 authorization(basic 토큰)파싱
			String basicAuth = hanaHttpServletRequestWrapper.getHeader("authorization");
			if(basicAuth.startsWith("Basic")) {
				String decBasicAuth = new String(Base64.decodeBase64(basicAuth.substring(6).getBytes("UTF-8")));
				String[] arrDecBasicAuth = decBasicAuth.split(":");
				String hanaClientId = arrDecBasicAuth[0];
				String clientSecret = arrDecBasicAuth[1];
			
				//client_secret 암호화후 client_id 조회
				List<Map<String, Object>> listResultmap;
				try {
					String newdate = new SimpleDateFormat("yyyyMMdd").format(new Date());
					listResultmap = jdbcTemplate.queryForList(selectClientId, hanaClientId, newdate);
				}
				catch (EmptyResultDataAccessException e) {
					throw new NoSuchClientException("No client with requested id: " + hanaClientId);
				}
				
				String clientId = null;
				Iterator<Map<String, Object>> itr = listResultmap.iterator();
				while(itr.hasNext() )
				{
					Map<String, Object> clientDetails = itr.next();
					String dbClientSecret = MapUtils.getString(clientDetails, "client_secret");
					if(passwordEncoder.matches(clientSecret, dbClientSecret)) {
						clientId = MapUtils.getString(clientDetails, "client_id");
						break;
					}
				}

				if(clientId != null) {
					//Base64.enc(client_id:client_secret BasicToken) 암호화
					String StringClient = clientId + ":" + clientSecret;
					byte[] encodedClient = Base64.encodeBase64(StringClient.getBytes()); 
					String newBasicAuth = "Basic " + new String(encodedClient);
					
					//헤더에 추가.
					hanaHttpServletRequestWrapper.putHeader("Authorization", newBasicAuth);
					hanaHttpServletRequestWrapper.putHeader("authorization", newBasicAuth);

					//Param에 추가 
					//for authorization_code, implicit
					if(hanaHttpServletRequestWrapper.getParameter("client_id") != null) {
						hanaHttpServletRequestWrapper.putParameter("client_id", clientId);
					}
				}else {
					condition = false;
				}
			}
		}

		if (!condition) {
			throw new AuthenticationServiceException("condition not satisfied");
		}
//		super.doFilter(hanaHttpServletRequestWrapper, res, chain);
		chain.doFilter(hanaHttpServletRequestWrapper, res);
	}
	
	public static String getCurrentUrlFromRequest(ServletRequest request)
	{
	   if (! (request instanceof HttpServletRequest))
	       return null;

	   return getCurrentUrlFromRequest((HttpServletRequest)request);
	}

	public static String getCurrentUrlFromRequest(HttpServletRequest request)
	{
	    String requestURI = request.getRequestURI();

	    return requestURI;
	}
}