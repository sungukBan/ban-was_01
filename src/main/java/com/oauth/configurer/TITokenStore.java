package com.oauth.configurer;

import com.oauth.mapper.OAuthRefreshTokenMapper;
import com.oauth.model.OAuthRefreshToken;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.oauth2.common.DefaultExpiringOAuth2RefreshToken;
import org.springframework.security.oauth2.common.OAuth2AccessToken;
import org.springframework.security.oauth2.common.OAuth2RefreshToken;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.security.oauth2.provider.OAuth2Request;
import org.springframework.security.oauth2.provider.TokenRequest;
import org.springframework.security.oauth2.provider.token.AuthenticationKeyGenerator;
import org.springframework.security.oauth2.provider.token.DefaultAuthenticationKeyGenerator;
import org.springframework.security.oauth2.provider.token.store.redis.RedisTokenStore;

import javax.sql.DataSource;
import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.Types;
import java.util.Date;
import java.util.List;
import java.util.Map;

public class TITokenStore extends RedisTokenStore {
	private static final Logger log = LoggerFactory.getLogger(TITokenStore.class);
	
	private static final String DEFAULT_ACCESS_TOKEN_INSERT_STATEMENT = "insert into oauth_access_token (token_id, token_value, authentication_id, user_name, client_id, refresh_token, refresh_token_value, expire_date, refreshed) values (?, ?, ?, ?, ?, ?, ?, ?, ?)";
	private static final String DEFAULT_REFRESH_TOKEN_INSERT_STATEMENT = "insert into oauth_refresh_token (token_id, token_value, user_name, client_id, expire_date) values (?, ?, ?, ?, ?)";
	private static final String DEFAULT_ACCESS_TOKEN_REVOKE_STATEMENT = "update oauth_access_token set revoked = 1, revoke_date=?  where token_id = ?";
	private static final String DEFAULT_REFRESH_TOKEN_REVOKE_STATEMENT = "update oauth_refresh_token set revoked = 1, revoke_date=? where token_id = ?";
	private static final String DEFAULT_ACCESS_TOKEN_SELECT_STATEMENT = "select token_id, client_id, expire_date, reg_date, revoked, revoke_date, refreshed from oauth_access_token where token_id = ?" ;
	private static final String DEFAULT_ACCESS_TOKEN_SELECT_STATEMENT_CLINENTID_CI = "select authentication_id, token_id, token_value, user_name, client_id, refresh_token_value, expire_date, reg_date, revoked, revoke_date, refreshed from oauth_access_token where user_name = ? and client_id=?" ;
	
	@Autowired
	private OAuthRefreshTokenMapper mapper;
	
	private final JdbcTemplate jdbcTemplate;
	private AuthenticationKeyGenerator authenticationKeyGenerator = new DefaultAuthenticationKeyGenerator();

	public TITokenStore(RedisConnectionFactory connectionFactory, DataSource dataSource) {
		super(connectionFactory);
		this.jdbcTemplate = new JdbcTemplate(dataSource);
	}

	@Override
	public void storeAccessToken(OAuth2AccessToken token, OAuth2Authentication authentication) {
		String refreshToken = null;
		if (token.getRefreshToken() != null) {
			refreshToken = token.getRefreshToken().getValue();
		}
		
		if (readAccessToken(token.getValue())!=null) {
			removeAccessToken(token.getValue());
		}
		
		boolean isRefreshToken = false;
		OAuth2Request req = authentication.getOAuth2Request();
		TokenRequest tokenRequest = req.getRefreshTokenRequest();
		if(tokenRequest != null) {
			String grantType = tokenRequest.getGrantType();
			if(grantType != null && grantType.equals("refresh_token")) {
				isRefreshToken = true;
			}
		}

		jdbcTemplate.update(DEFAULT_ACCESS_TOKEN_INSERT_STATEMENT, new Object[] { 
				extractTokenKey(token.getValue()),
				token.getValue(),
				authenticationKeyGenerator.extractKey(authentication),
				authentication.isClientOnly() ? null : authentication.getName(),
				authentication.getOAuth2Request().getClientId(),
				extractTokenKey(refreshToken),
				refreshToken,
				token.getExpiration(),
				isRefreshToken},
				new int[] {
				Types.VARCHAR, Types.VARCHAR, Types.VARCHAR, Types.VARCHAR, Types.VARCHAR, Types.VARCHAR, Types.VARCHAR, Types.TIMESTAMP, Types.TINYINT});
		
		super.storeAccessToken(token, authentication);
	}
	
	

	@Override
	public void storeRefreshToken(OAuth2RefreshToken refreshToken, OAuth2Authentication authentication) {
		Date expireDate = null;
		if(refreshToken instanceof DefaultExpiringOAuth2RefreshToken) {
			expireDate = ((DefaultExpiringOAuth2RefreshToken)refreshToken).getExpiration();
		}
		
		jdbcTemplate.update(DEFAULT_REFRESH_TOKEN_INSERT_STATEMENT, new Object[] { 
					extractTokenKey(refreshToken.getValue()),
					refreshToken.getValue(),
					authentication.isClientOnly() ? null : authentication.getName(),
					authentication.getOAuth2Request().getClientId(),
					expireDate,
				}, 
				new int[] { Types.VARCHAR, Types.VARCHAR, Types.VARCHAR, Types.VARCHAR, Types.TIMESTAMP});
		
		super.storeRefreshToken(refreshToken, authentication);
	}

	

	@Override
	public void removeAccessToken(String tokenValue) {
		jdbcTemplate.update(DEFAULT_ACCESS_TOKEN_REVOKE_STATEMENT, new Date(), extractTokenKey(tokenValue));
		super.removeAccessToken(tokenValue);
	}

	@Override
	public void removeRefreshToken(String tokenValue) {
		jdbcTemplate.update(DEFAULT_REFRESH_TOKEN_REVOKE_STATEMENT, new Date(), extractTokenKey(tokenValue));
		super.removeRefreshToken(tokenValue);
	}
	
	public OAuthRefreshToken getRefreshToken(OAuth2RefreshToken refreshToken) {
		return mapper.findRefreshTokenByTokenId(extractTokenKey(refreshToken.getValue()));
	}
	
	/**
	 * ci + clientId에 해당하는 모든 토큰을 revoke 한다.
	 * @param ci
	 * @param clientId
	 */
	public void revokeTokenAll(String ci, String clientId) {
		List<Map<String, Object>> list = jdbcTemplate.queryForList(DEFAULT_ACCESS_TOKEN_SELECT_STATEMENT_CLINENTID_CI, ci, clientId);
		
		if(list != null && list.size() > 0) {
			for(Map map : list) {
				Object revoked = map.get("revoked");
				int revoke = 0;
				if(revoked instanceof Boolean) {
					revoke = ((boolean)revoked) ? 1 : 0;
				} else if(revoked instanceof Integer) {
					revoke = (int) revoked;
				}
				
				if(revoke == 0) {
					String token = (String)map.get("token_value");
					removeAccessToken(token);
					
					String refreshToken = (String)map.get("refresh_token_value");
					if(refreshToken != null) {
						removeRefreshToken(refreshToken);
					}
				}
			}
		}
	}
	
	/*
	public Map<String, Object> jdbcReadAccessToken(String tokenValue) {
		Map<String, Object> map = null;
		
		List<Map> list = null;
		try {
			list = jdbcTemplate.queryForObject(DEFAULT_ACCESS_TOKEN_SELECT_STATEMENT, new RowMapper<List>() {
				public List mapRow(ResultSet rs, int rowNum) throws SQLException {
					
					ResultSetMetaData metaData = rs.getMetaData();
			        int sizeOfColumn = metaData.getColumnCount();

			        List<Map> list = new ArrayList<Map>();
			        Map<String, Object> map;
			        String column;
			        while (rs.next()) {
			            map = new HashMap<String, Object>();
			            for (int indexOfcolumn = 0; indexOfcolumn < sizeOfColumn; indexOfcolumn++) {
			                column = metaData.getColumnName(indexOfcolumn + 1);
			                map.put(column, rs.getString(column));
			            }
			            list.add(map);
			        }
			        return list;
					
				}
			}, extractTokenKey(tokenValue));
		}
		catch (EmptyResultDataAccessException e) {
			if (log.isInfoEnabled()) {
				log.info("Failed to find access token for token " + tokenValue);
			}
		}

		return map;
	}
	*/

	protected String extractTokenKey(String value) {
		if (value == null) {
			return null;
		}
		MessageDigest digest;
		try {
			digest = MessageDigest.getInstance("MD5");
		}
		catch (NoSuchAlgorithmException e) {
			throw new IllegalStateException("MD5 algorithm not available.  Fatal (should be in the JDK).");
		}

		try {
			byte[] bytes = digest.digest(value.getBytes("UTF-8"));
			return String.format("%032x", new BigInteger(1, bytes));
		}
		catch (UnsupportedEncodingException e) {
			throw new IllegalStateException("UTF-8 encoding not available.  Fatal (should be in the JDK).");
		}
	}
	
}
