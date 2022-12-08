package com.oauth.configurer;

import com.oauth.hanaClient.HanaJdbcClientDetailsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.crypto.password.StandardPasswordEncoder;
import org.springframework.security.oauth2.config.annotation.configurers.ClientDetailsServiceConfigurer;
import org.springframework.security.oauth2.config.annotation.web.configuration.AuthorizationServerConfigurerAdapter;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableAuthorizationServer;
import org.springframework.security.oauth2.config.annotation.web.configurers.AuthorizationServerEndpointsConfigurer;
import org.springframework.security.oauth2.config.annotation.web.configurers.AuthorizationServerSecurityConfigurer;
import org.springframework.security.oauth2.provider.ClientDetailsService;
import org.springframework.security.oauth2.provider.approval.TokenApprovalStore;
import org.springframework.security.oauth2.provider.code.AuthorizationCodeServices;
import org.springframework.security.oauth2.provider.endpoint.AuthorizationEndpoint;
import org.springframework.security.oauth2.provider.request.DefaultOAuth2RequestFactory;
import org.springframework.security.oauth2.provider.token.TokenEnhancerChain;
import org.springframework.security.oauth2.provider.token.TokenStore;
import org.springframework.security.oauth2.provider.token.store.JwtAccessTokenConverter;
import org.springframework.security.provisioning.JdbcUserDetailsManager;

import javax.annotation.PostConstruct;
import javax.sql.DataSource;
import java.util.Arrays;

@Configuration
@EnableAuthorizationServer
public class OauthAuthorizationConfig extends AuthorizationServerConfigurerAdapter {
	
	private final String signingKey = "1f95062b-eeec-4a43-b298-6632dbed6319";

    //private final PasswordEncoder passwordEncoder;
	@Autowired
    private DataSource dataSource;
    
    @Autowired
    private AuthenticationManager authenticationManager;
    
    @Value("${oauth.config.tokenExpireSecond.default}")
	protected int tokenExpireSecondDefault;
    
    
    @Value("${oauth.config.refreshTokenExpireSecond}")
	protected int refreshTokenExpireSecond;
	
	
	@Autowired
	RedisConnectionFactory redisConnectionFactory;
	
	@Autowired
	private AuthorizationEndpoint authorizationEndpoint;
	
	@Autowired
	private ClientDetailsService clientDetailsService;
	
	
	
	@PostConstruct
	public void init() {
		//authorizationEndpoint.setUserApprovalPage("forward:/2.0/confirm_access");
		//authorizationEndpoint.setErrorPage("forward:/oauth/custom_error");
		
	}
	

    @Override
    public void configure(AuthorizationServerSecurityConfigurer security) {
        security.tokenKeyAccess("permitAll()")
                .checkTokenAccess("permitAll()") 
                .allowFormAuthenticationForClients();
    }
    
    @Bean
	public JdbcUserDetailsManager jdbcUserDetailsManager() {
		JdbcUserDetailsManager userManager = new JdbcUserDetailsManager();
		userManager.setDataSource(dataSource);
		return userManager;
	}
    
    /*
    @Override
    public void configure(ClientDetailsServiceConfigurer clients) throws Exception {
        clients.jdbc(dataSource).passwordEncoder(passwordEncoder);
    }
    */
    
    @Bean
	public PasswordEncoder passwordEncoder() {
	  return new StandardPasswordEncoder();
	}
    
    @Override
	public void configure(ClientDetailsServiceConfigurer client) throws Exception {
		client.withClientDetails(jdbcClientDetailsService());
	}
    
    @Bean
	public HanaJdbcClientDetailsService jdbcClientDetailsService() {
	    return new HanaJdbcClientDetailsService(dataSource);
	}
 
	 
    @Bean
    public TokenStore tokenStore() {
		//JwtTokenStore tokenStore = new OAuthJwtTokenStore(jwtAccessTokenConverter());
		//tokenStore.setApprovalStore(approvalStore);
		//return tokenStore;
        //return new JwtTokenStore(jwtAccessTokenConverter());
    	
    	return new TITokenStore(redisConnectionFactory, dataSource);
    	//return new RedisTokenStore(redisConnectionFactory);
    }
    
	@Bean
	public JwtAccessTokenConverter jwtAccessTokenConverter() {
		JwtAccessTokenConverter converter = new JwtAccessTokenConverter();
		converter.setAccessTokenConverter(new HanaTokenConverter());
		converter.setSigningKey(signingKey);		
		return converter;
	}

    
    @Override
    public void configure(AuthorizationServerEndpointsConfigurer endpoints) {
        endpoints.accessTokenConverter(jwtAccessTokenConverter());
        endpoints.tokenServices(tokenServices());
        endpoints.tokenStore(tokenStore()).authenticationManager(authenticationManager);
        endpoints.authorizationCodeServices(authorizationCodeServices());
        endpoints.userApprovalHandler(approvalHandler());
        
        
        endpoints.prefix("/2.0");
        endpoints.pathMapping("/oauth/token", "/token");
        endpoints.pathMapping("/oauth/check_token", "/check_token");
//        endpoints.pathMapping("/oauth/authorize", "/authorize");
//        endpoints.pathMapping("/oauth/confirm_access", "/confirm_access");
    }
    
    public TIUserApprovalHandler approvalHandler() {
    	TIUserApprovalHandler handler =  new TIUserApprovalHandler();
        TokenApprovalStore tokenApprovalStore = new TokenApprovalStore();
		tokenApprovalStore.setTokenStore(tokenStore());
        handler.setApprovalStore(tokenApprovalStore);
		handler.setRequestFactory(new DefaultOAuth2RequestFactory(clientDetailsService));
		handler.setClientDetailsService(clientDetailsService);
		return handler;
    }
    
    
    @Bean
    @Primary
    public TITokenService tokenServices() {		
		TokenEnhancerChain tokenEnhancerChain = new TokenEnhancerChain();
        tokenEnhancerChain.setTokenEnhancers(
        		Arrays.asList(new HanaTokenEnhancer(jdbcClientDetailsService()), 
        		jwtAccessTokenConverter()));
        

        TITokenService tokenServices = new TITokenService();
		tokenServices.setTokenStore(tokenStore());
		tokenServices.setSupportRefreshToken(true);
		tokenServices.setTokenEnhancer(tokenEnhancerChain);
		
        tokenServices.setAccessTokenValiditySeconds(tokenExpireSecondDefault);
        tokenServices.setRefreshTokenValiditySeconds(refreshTokenExpireSecond);
        
        return tokenServices;
    }
    
    
    @Bean
	protected AuthorizationCodeServices authorizationCodeServices() {
    	TIAuthorizationCodeServices codeService = new TIAuthorizationCodeServices(dataSource);
		return codeService;
	}
}
