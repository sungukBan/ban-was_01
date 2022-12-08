package com.oauth.controller;

import com.oauth.filter.HanaHttpServletRequestWrapper;
import com.oauth.hanaClient.HanaClientDetails;
import com.oauth.hanaClient.HanaJdbcClientDetailsService;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.oauth2.provider.endpoint.CheckTokenEndpoint;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Map;

/**
 * @deprecated
 * @author joeyna
 *
 */
//@RestController
public class HanaCheckTokenController {
	
	@Autowired
	CheckTokenEndpoint checkTokenEndpoint;
	@Autowired
    HanaJdbcClientDetailsService hanaJdbcClientDetailsService;

	protected final Log logger = LogFactory.getLog(getClass());

/*
	@SuppressWarnings("unchecked")
	@RequestMapping(value = "/oauth/check_token")
	@ResponseBody
*/	
	public Map<String, Object> checkToken(@RequestParam("token") String value, HanaHttpServletRequestWrapper request) {
		//토큰 검증 후 hana_client_id 매핑. 
		Map<String, Object> result = (Map<String, Object>) checkTokenEndpoint.checkToken(value);
		String clientId = MapUtils.getString(result, "client_id");
		HanaClientDetails clientDetails = (HanaClientDetails) hanaJdbcClientDetailsService.loadClientByClientId(clientId);
		result.put("client_id", clientDetails.getHana_client_id());
		return result;
	}
}
