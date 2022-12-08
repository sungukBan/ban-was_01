package com.oauth.hanaClient;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.security.crypto.password.NoOpPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.common.exceptions.InvalidClientException;
import org.springframework.security.oauth2.common.util.DefaultJdbcListFactory;
import org.springframework.security.oauth2.common.util.JdbcListFactory;
import org.springframework.security.oauth2.provider.*;
import org.springframework.util.Assert;
import org.springframework.util.ClassUtils;
import org.springframework.util.StringUtils;

import javax.sql.DataSource;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Basic, JDBC implementation of the client details service.
 */
public class HanaJdbcClientDetailsService implements ClientDetailsService, ClientRegistrationService {

	private static final Log logger = LogFactory.getLog(HanaJdbcClientDetailsService.class);

	private JsonMapper mapper = createJsonMapper();

	private final static String FIELD_CLIENT_ID = "client_id";
	private final static String FIELD_RESOURCE_IDS = "resource_ids";
	private final static String FIELD_SCOPE = "scope";
	private final static String FIELD_GRANT_TYPE = "authorized_grant_types";
	private final static String FIELD_AUTHORITIES = "authorities";
	private final static String FIELD_REDIRECT_URI = "web_server_redirect_uri";
	private final static String FIELD_ORG_CODE = "org_code";
	
	
	private static final String CLIENT_FIELDS_FOR_UPDATE = "resource_ids, scope, "
			+ "authorized_grant_types, web_server_redirect_uri, authorities, access_token_validity, "
			+ "refresh_token_validity, additional_information, autoapprove, expire_date, org_code";
//			+ ", hana_client_id, hana_client_seq";

	private static final String CLIENT_FIELDS = "client_secret, " + CLIENT_FIELDS_FOR_UPDATE;

	private static final String BASE_FIND_STATEMENT = "select client_id, " + CLIENT_FIELDS
			+ " from oauth_client_details";

	private static final String DEFAULT_FIND_STATEMENT = BASE_FIND_STATEMENT + " order by client_id";
	
	private static final String DEFAULT_SELECT_STATEMENT = BASE_FIND_STATEMENT + " where client_id = ? and expire_date >= ?";

	private static final String DEFAULT_INSERT_STATEMENT = "insert into oauth_client_details (" + CLIENT_FIELDS
			+ ", client_id) values (?,?,?,?,?,?,?,?,?,?,?,?,?)";
//			+ ", client_id) values (?,?,?,?,?,?,?,?,?,?,?,?,?,?)";

	private static final String DEFAULT_UPDATE_STATEMENT = "update oauth_client_details " + "set "
			+ CLIENT_FIELDS_FOR_UPDATE.replaceAll(", ", "=?, ") + "=? where client_id = ?";

	private static final String DEFAULT_UPDATE_SECRET_STATEMENT = "update oauth_client_details "
			+ "set client_secret = ? where client_id = ?";

	private static final String DEFAULT_DELETE_STATEMENT = "delete from oauth_client_details where client_id = ?";

	private static final String INSERT_USERINFO_STATEMENT = "insert into oauth_user_token_history (unique_id, sso_token, sso_period, oauth_token) values (?,?,?,?)";

	private RowMapper<ClientDetails> rowMapper = new ClientDetailsRowMapper();

	private String deleteClientDetailsSql = DEFAULT_DELETE_STATEMENT;

	private String findClientDetailsSql = DEFAULT_FIND_STATEMENT;
	
	private String updateClientDetailsSql = DEFAULT_UPDATE_STATEMENT;

	private String updateClientSecretSql = DEFAULT_UPDATE_SECRET_STATEMENT;
	
	private String updateClientExpireDateSql = "update oauth_client_details " + "set expire_date = ? where client_id = ?";

	private String insertClientDetailsSql = DEFAULT_INSERT_STATEMENT;

	private String selectClientDetailsSql = DEFAULT_SELECT_STATEMENT;

	private PasswordEncoder passwordEncoder = NoOpPasswordEncoder.getInstance();

	private final JdbcTemplate jdbcTemplate;

	private JdbcListFactory listFactory;

	
	private final String selectLatestClientDetailsSql = BASE_FIND_STATEMENT +  " where hana_client_id =? and hana_client_seq = (SELECT MAX(hana_client_seq) FROM oauth_client_details WHERE hana_client_id = ?)";
	
	public HanaJdbcClientDetailsService(DataSource dataSource) {
		Assert.notNull(dataSource, "DataSource required");
		this.jdbcTemplate = new JdbcTemplate(dataSource);
		this.listFactory = new DefaultJdbcListFactory(new NamedParameterJdbcTemplate(jdbcTemplate));
	}

	/**
	 * @param passwordEncoder the password encoder to set
	 */
	public void setPasswordEncoder(PasswordEncoder passwordEncoder) {
		this.passwordEncoder = passwordEncoder;
	}
	
	/**
	 * @deprecated
	 * @param hanaClientId
	 * @return
	 * @throws InvalidClientException
	 */
	public ClientDetails getLatestOauthClient(String hanaClientId) throws InvalidClientException{
		HanaClientDetails details;
		try {
			details = (HanaClientDetails) jdbcTemplate.queryForObject(selectLatestClientDetailsSql, new ClientDetailsRowMapper(), hanaClientId, hanaClientId);
		}
		catch (EmptyResultDataAccessException e) {
			throw new NoSuchClientException("No client with requested id: " + hanaClientId);
		}
		return details;
	}

	public ClientDetails loadClientByClientId(String clientId) throws InvalidClientException {
		HanaClientDetails details;
		try {
			String newdate = new SimpleDateFormat("yyyyMMdd").format(new Date());
			details = (HanaClientDetails)jdbcTemplate.queryForObject(selectClientDetailsSql, new ClientDetailsRowMapper(), clientId, newdate);
		}
		catch (EmptyResultDataAccessException e) {
			throw new NoSuchClientException("No client with requested id: " + clientId);
		}

		return details;
	}
	
	public void addClientDetails(ClientDetails clientDetails) throws ClientAlreadyExistsException {
		try {
			jdbcTemplate.update(insertClientDetailsSql, getFields((HanaClientDetails)clientDetails));
		}
		catch (DuplicateKeyException e) {
			throw new ClientAlreadyExistsException("Client already exists: " + clientDetails.getClientId(), e);
		}
	}

	public void updateClientDetails(ClientDetails clientDetails) throws NoSuchClientException {
		int count = jdbcTemplate.update(updateClientDetailsSql, getFieldsForUpdate((HanaClientDetails)clientDetails));
		if (count != 1) {
			throw new NoSuchClientException("No client found with id = " + clientDetails.getClientId());
		}
	}

	public void updateClientSecret(String clientId, String secret) throws NoSuchClientException {
		int count = jdbcTemplate.update(updateClientSecretSql, passwordEncoder.encode(secret), clientId);
		if (count != 1) {
			throw new NoSuchClientException("No client found with id = " + clientId);
		}
	}
	
	public void updateClientExpireDate(String clientId, String expireDate) throws NoSuchClientException {
		int count = jdbcTemplate.update(updateClientExpireDateSql, expireDate, clientId);
		if (count != 1) {
			throw new NoSuchClientException("No client found with id = " + clientId);
		}
	}

	// 보안 대응건으로 불필요 메소드 삭제 (메소드만 남겨둠)
	public void removeClientDetails(String clientId) throws NoSuchClientException {
//		int count = jdbcTemplate.update(deleteClientDetailsSql, clientId);
//		if (count != 1) {
//			throw new NoSuchClientException("No client found with id = " + clientId);
//		}
	}

	public List<ClientDetails> listClientDetails() {
		return listFactory.getList(findClientDetailsSql, Collections.<String, Object> emptyMap(), rowMapper);
	}

	/**
	 * insert oauth_user_token_history
	 * @param paramMap
	 * @throws Exception
	 */
	public void insertUserTokenHistory(Map<String, String> paramMap) throws Exception {
		if (paramMap == null)
			throw new NullPointerException("paramMap is null..");

		String unique_id = paramMap.containsKey("unique_id") ? paramMap.get("unique_id") : "";
		String sso_token = paramMap.containsKey("sso_token") ? paramMap.get("sso_token") : "";
		String sso_period = paramMap.containsKey("sso_period") ? paramMap.get("sso_period") : "";
		String oauth_token = paramMap.containsKey("oauth_token") ? paramMap.get("oauth_token") : "";

		jdbcTemplate.update(INSERT_USERINFO_STATEMENT, unique_id, sso_token, sso_period, oauth_token);
	}

	private Object[] getFields(HanaClientDetails clientDetails) {
		Object[] fieldsForUpdate = getFieldsForUpdate(clientDetails);
		Object[] fields = new Object[fieldsForUpdate.length + 1];
		System.arraycopy(fieldsForUpdate, 0, fields, 1, fieldsForUpdate.length);
		fields[0] = clientDetails.getClientSecret() != null ? passwordEncoder.encode(clientDetails.getClientSecret())
				: null;
		return fields;
	}

	private Object[] getFieldsForUpdate(HanaClientDetails clientDetails) {

		String json = null;
		try {
			json = mapper.write(clientDetails.getAdditionalInformation());
		}
		catch (Exception e) {
			logger.warn("Could not serialize additional information: " + clientDetails, e);
		}
		
		if(clientDetails instanceof HanaClientDetails) {
			HanaClientDetails hanaClientDetails = (HanaClientDetails)clientDetails;
			
			return new Object[] {
					clientDetails.getResourceIds() != null ? StringUtils.collectionToCommaDelimitedString(clientDetails
							.getResourceIds()) : null,
					clientDetails.getScope() != null ? StringUtils.collectionToCommaDelimitedString(clientDetails
							.getScope()) : null,
					clientDetails.getAuthorizedGrantTypes() != null ? StringUtils
							.collectionToCommaDelimitedString(clientDetails.getAuthorizedGrantTypes()) : null,
					clientDetails.getRegisteredRedirectUri() != null ? StringUtils
							.collectionToCommaDelimitedString(clientDetails.getRegisteredRedirectUri()) : null,
					clientDetails.getAuthorities() != null ? StringUtils.collectionToCommaDelimitedString(clientDetails
							.getAuthorities()) : null, clientDetails.getAccessTokenValiditySeconds(),
					clientDetails.getRefreshTokenValiditySeconds(), json, getAutoApproveScopes(clientDetails), 
					hanaClientDetails.getExpire_date() != null ? hanaClientDetails.getExpire_date() : null,
//					hanaClientDetails.getHana_client_id() != null ? hanaClientDetails.getHana_client_id() : null,
//					hanaClientDetails.getHana_client_seq() != null ? hanaClientDetails.getHana_client_seq() : null,
					clientDetails.getOrgCode(),
					clientDetails.getClientId() 
					};
			
		}else {
		
			return new Object[] {
					clientDetails.getResourceIds() != null ? StringUtils.collectionToCommaDelimitedString(clientDetails
							.getResourceIds()) : null,
					clientDetails.getScope() != null ? StringUtils.collectionToCommaDelimitedString(clientDetails
							.getScope()) : null,
					clientDetails.getAuthorizedGrantTypes() != null ? StringUtils
							.collectionToCommaDelimitedString(clientDetails.getAuthorizedGrantTypes()) : null,
					clientDetails.getRegisteredRedirectUri() != null ? StringUtils
							.collectionToCommaDelimitedString(clientDetails.getRegisteredRedirectUri()) : null,
					clientDetails.getAuthorities() != null ? StringUtils.collectionToCommaDelimitedString(clientDetails
							.getAuthorities()) : null, clientDetails.getAccessTokenValiditySeconds(),
					clientDetails.getRefreshTokenValiditySeconds(), json, getAutoApproveScopes(clientDetails), null, 
					clientDetails.getClientId() 
					};
		}
	}

	private String getAutoApproveScopes(ClientDetails clientDetails) {
		if (clientDetails.isAutoApprove("true")) {
			return "true"; // all scopes autoapproved
		}
		Set<String> scopes = new HashSet<String>();
		for (String scope : clientDetails.getScope()) {
			if (clientDetails.isAutoApprove(scope)) {
				scopes.add(scope);
			}
		}
		return StringUtils.collectionToCommaDelimitedString(scopes);
	}

	public void setSelectClientDetailsSql(String selectClientDetailsSql) {
		this.selectClientDetailsSql = selectClientDetailsSql;
	}

	public void setDeleteClientDetailsSql(String deleteClientDetailsSql) {
		this.deleteClientDetailsSql = deleteClientDetailsSql;
	}

	public void setUpdateClientDetailsSql(String updateClientDetailsSql) {
		this.updateClientDetailsSql = updateClientDetailsSql;
	}

	public void setUpdateClientSecretSql(String updateClientSecretSql) {
		this.updateClientSecretSql = updateClientSecretSql;
	}

	public void setInsertClientDetailsSql(String insertClientDetailsSql) {
		this.insertClientDetailsSql = insertClientDetailsSql;
	}

	public void setFindClientDetailsSql(String findClientDetailsSql) {
		this.findClientDetailsSql = findClientDetailsSql;
	}

	/**
	 * @param listFactory the list factory to set
	 */
	public void setListFactory(JdbcListFactory listFactory) {
		this.listFactory = listFactory;
	}

	/**
	 * @param rowMapper the rowMapper to set
	 */
	public void setRowMapper(RowMapper<ClientDetails> rowMapper) {
		this.rowMapper = rowMapper;
	}

	/**
	 * Row mapper for ClientDetails.
	 * 
	 * @author Dave Syer
	 * 
	 */
	private static class ClientDetailsRowMapper implements RowMapper<ClientDetails> {
		private JsonMapper mapper = createJsonMapper();

		public HanaClientDetails mapRow(ResultSet rs, int rowNum) throws SQLException {
			HanaClientDetails details = new HanaClientDetails(rs.getString(FIELD_CLIENT_ID)
					, rs.getString(FIELD_RESOURCE_IDS), rs.getString(FIELD_SCOPE),
					rs.getString(FIELD_GRANT_TYPE), rs.getString(FIELD_AUTHORITIES),
					rs.getString(FIELD_REDIRECT_URI));
			details.setClientSecret(rs.getString(2));
			if (rs.getObject(8) != null) {
				details.setAccessTokenValiditySeconds(rs.getInt(8));
			}
			if (rs.getObject(9) != null) {
				details.setRefreshTokenValiditySeconds(rs.getInt(9));
			}
			if (rs.getObject(12) != null) {
				details.setExpire_date(rs.getString(12));
			}
//			if (rs.getObject(13) != null) {
//				details.setHana_client_id(rs.getString(13));
//			}
//			if (rs.getObject(14) != null) {
//				details.setHana_client_seq(rs.getInt(14));
//			}
			
			String json = rs.getString(10);
			if (json != null) {
				try {
					@SuppressWarnings("unchecked")
					Map<String, Object> additionalInformation = mapper.read(json, Map.class);
					details.setAdditionalInformation(additionalInformation);
				}
				catch (Exception e) {
					logger.warn("Could not decode JSON for additional information: " + details, e);
				}
			}
			String scopes = rs.getString(11);
			if (scopes != null) {
				details.setAutoApproveScopes(StringUtils.commaDelimitedListToSet(scopes));
			}
			
			details.setOrgCode(rs.getString(FIELD_ORG_CODE));
			return details;
		}
	}

	interface JsonMapper {
		String write(Object input) throws Exception;

		<T> T read(String input, Class<T> type) throws Exception;
	}

	private static JsonMapper createJsonMapper() {
		if (ClassUtils.isPresent("org.codehaus.jackson.map.ObjectMapper", null)) {
			return new JacksonMapper();
		}
		else if (ClassUtils.isPresent("com.fasterxml.jackson.databind.ObjectMapper", null)) {
			return new Jackson2Mapper();
		}
		return new NotSupportedJsonMapper();
	}

	private static class JacksonMapper implements JsonMapper {
		private org.codehaus.jackson.map.ObjectMapper mapper = new org.codehaus.jackson.map.ObjectMapper();

		@Override
		public String write(Object input) throws Exception {
			return mapper.writeValueAsString(input);
		}

		@Override
		public <T> T read(String input, Class<T> type) throws Exception {
			return mapper.readValue(input, type);
		}
	}

	private static class Jackson2Mapper implements JsonMapper {
		private com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();

		@Override
		public String write(Object input) throws Exception {
			return mapper.writeValueAsString(input);
		}

		@Override
		public <T> T read(String input, Class<T> type) throws Exception {
			return mapper.readValue(input, type);
		}
	}

	private static class NotSupportedJsonMapper implements JsonMapper {
		@Override
		public String write(Object input) throws Exception {
			throw new UnsupportedOperationException(
					"Neither Jackson 1 nor 2 is available so JSON conversion cannot be done");
		}

		@Override
		public <T> T read(String input, Class<T> type) throws Exception {
			throw new UnsupportedOperationException(
					"Neither Jackson 1 nor 2 is available so JSON conversion cannot be done");
		}
	}

}
