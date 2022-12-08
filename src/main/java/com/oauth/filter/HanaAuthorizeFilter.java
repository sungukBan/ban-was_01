package com.oauth.filter;

import org.apache.commons.collections4.MapUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.provider.NoSuchClientException;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.sql.DataSource;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;

public class HanaAuthorizeFilter extends OncePerRequestFilter {


	private static final Logger logger = LoggerFactory.getLogger(HanaAuthorizeFilter.class);
	
	PasswordEncoder passwordEncoder;
    private final JdbcTemplate jdbcTemplate;
    
    private final String selectClientId = "select client_id, client_secret from oauth_client_details where hana_client_id = ? and expire_date >= ?";

    public HanaAuthorizeFilter(PasswordEncoder passwordEncoder, DataSource dataSource) {
    	super();
    	this.passwordEncoder = passwordEncoder;
		this.jdbcTemplate = new JdbcTemplate(dataSource);
	}

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
		logger.debug("HanaAuthorizeFilter.doFilterInternal");
		
		HanaHttpServletRequestWrapper hanaHttpServletRequestWrapper = new HanaHttpServletRequestWrapper((HttpServletRequest) request);
		
		String requestUri = request.getRequestURI();
		if (requestUri.equals("/oauth/authorize")) {
			HttpServletRequest httpRequest = (HttpServletRequest) request;
			String hanaClientId = httpRequest.getParameter("client_id");
			
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
			if(listResultmap.size() > 0) {
				clientId = MapUtils.getString(listResultmap.get(0), "client_id");
			}
			
			hanaHttpServletRequestWrapper.putParameter("client_id", clientId);
		}
		filterChain.doFilter(hanaHttpServletRequestWrapper, response);
	}
}
