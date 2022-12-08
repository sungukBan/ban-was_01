package com.oauth.controller;

import org.apache.commons.collections4.MapUtils;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.JdbcUserDetailsManager;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@RestController
public class UserDetailManagerController {

	@Autowired
	JdbcUserDetailsManager jdbcUserDetailsManager;
	
	@Autowired
	PasswordEncoder passwordEncoder;
	
	@RequestMapping(value = "/oauth/api/user", method = RequestMethod.POST)
	public  String addUser(@RequestBody Map<String, Object> parameters) throws JSONException {
		
		JSONObject returnJson = new JSONObject();

		try {
			String username = MapUtils.getString(parameters, "username");
			String password = MapUtils.getString(parameters, "password");
			Set<String> authorities = getRequestParamSet(parameters,"authorities");
			
			AuthorityUtils.createAuthorityList(authorities.toArray(new String[authorities.size()]));
			
			User user = new User(username, passwordEncoder.encode(password), new HashSet<GrantedAuthority>());
			jdbcUserDetailsManager.createUser(user);
			returnJson.put("resultcode", 0);
		}catch(Exception e) {
			e.printStackTrace();
			returnJson.put("resultcode", 1);
		}
	
		return returnJson.toString();
	}
	
	@SuppressWarnings("unchecked")
	private Set<String> getRequestParamSet(Map<String, Object> parameters, String key){
		if(parameters.get(key) == null) {
			return new HashSet<String>();
		}
		
		Set<String> result = new HashSet<String>();;
		if(String.class.isInstance(parameters.get(key))) {
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