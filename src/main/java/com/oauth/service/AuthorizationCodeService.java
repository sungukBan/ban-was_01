package com.oauth.service;

import com.oauth.hanaClient.HanaJdbcClientDetailsService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.oauth2.common.OAuth2AccessToken;
import org.springframework.security.oauth2.provider.*;
import org.springframework.security.oauth2.provider.code.JdbcAuthorizationCodeServices;
import org.springframework.security.oauth2.provider.request.DefaultOAuth2RequestFactory;
import org.springframework.security.oauth2.provider.request.DefaultOAuth2RequestValidator;
import org.springframework.security.oauth2.provider.token.DefaultTokenServices;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ResponseStatusException;

import javax.sql.DataSource;
import java.util.*;

@Service
public class AuthorizationCodeService extends AbstOAuthService{

    private static final Logger log = LoggerFactory.getLogger(AuthorizationCodeService.class);

    @Autowired
    private DefaultTokenServices tokenServices;

    @Autowired
    DataSource dataSource;

    @Autowired
    HanaJdbcClientDetailsService hanaJdbcClientDetailsService;

    private OAuth2RequestValidator oAuth2RequestValidator = new DefaultOAuth2RequestValidator();

    private String credentialsCharset = "UTF-8";

//    /**
//     * generateCode
//     * @param request
//     * @param parameters
//     * @return
//     * @throws Exception
//     */
//    public String generateCode(HttpServletRequest request, Map<String, String> parameters) throws IOException {
//        // Client credentials 확인
//        String header = request.getHeader("Authorization");
//        super.checkClientCredentials(request, header);
//
//        // Authorization Code 발급용 client Id 반환
//        String clientId = super.getClientIdFromAuthorizationHeader(request, header);
//        log.info("clientId: " + clientId) ;
//
//        // CI 가입 여부 체크 후 가입 처리
//        String ci = parameters.get("ci");
//        String uuid = parameters.get("uuid");
//        String affiliateCode = parameters.get("affiliateCode");
//        String redirectUri = parameters.get("redirectUri");
//        String scope = parameters.get("scope");
//
//        return generateCode(clientId, ci, uuid, affiliateCode, redirectUri, scope);
//    }

//    /**
//     *
//     * @param parameters
//     * @return
//     * @throws IOException
//     */
//    public String generateCode(Map<String, String> parameters) throws IOException {
//
//        // Authorization Code 발급용 client Id 반환
//        String clientId = parameters.get("client_id");
//        log.info("clientId: " + clientId) ;
//
//        // CI 가입 여부 체크 후 가입 처리
//        String ci = parameters.get("ci");
//        String uuid = parameters.get("uuid");
//        String affiliateCode = parameters.get("affiliateCode");
//        String redirectUri = parameters.get("redirectUri");
//        String scope = parameters.get("scope");
//
//        return generateCode(clientId, ci, uuid, affiliateCode, redirectUri, scope);
//    }

    /**
     * 토큰을 요청한 정보 저장
     * @param parameters
     */
    public void saveUserInfo(Map<String, String> parameters) throws Exception {
        hanaJdbcClientDetailsService.insertUserTokenHistory(parameters);
    }

    /**
     * getToken
     * @param clientId
     * @param ci
     * @param uuid
     * @param affiliateCode
     * @param redirectUri
     * @param scope
     * @param parameters
     * @return
     */
    public OAuth2AccessToken getUserToken(String clientId, String ci, String uuid, String affiliateCode, String redirectUri, String scope, Map<String, String> parameters) {
        // CI 가입 여부 체크 후 가입 처리
//        checkAndJoinHanaUsers(ci, uuid, affiliateCode);

        OAuth2RequestFactory oAuth2RequestFactory = new DefaultOAuth2RequestFactory(hanaJdbcClientDetailsService);
        ClientDetails clientDetails = hanaJdbcClientDetailsService.loadClientByClientId(clientId);
        TokenRequest tokenRequest = oAuth2RequestFactory.createTokenRequest(parameters, clientDetails);
        OAuth2Request storedOAuth2Request = oAuth2RequestFactory.createOAuth2Request(clientDetails, tokenRequest);

        if (clientId != null && !clientId.equals("")) {
            // Only validate the client details if a client authenticated during this
            // request.
            if (!clientId.equals(tokenRequest.getClientId())) {
                // double check to make sure that the client ID in the token request is the same as that in the
                // authenticated client
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Given client ID does not match authenticated client");
            }
        }
        if (clientDetails != null) {
            try {
                oAuth2RequestValidator.validateScope(tokenRequest, clientDetails);
            } catch (Exception e) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
            }
        }

        if (!StringUtils.hasText(tokenRequest.getGrantType())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Missing grant type2");
        }
        if (!clientDetails.getAuthorizedGrantTypes().contains(tokenRequest.getGrantType())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Unsupported grant type: " + tokenRequest.getGrantType());
        }

        //generate code
        String code = generateCode(clientId, ci, uuid, affiliateCode, redirectUri, scope);
        parameters.put("code", code);

        // AuthorizationRequest
        AuthorizationRequest authorizationRequest = new AuthorizationRequest();

        // - ApprovalParameters
        Map<String, String> approvalPrameters = new HashMap<>();
        approvalPrameters.put("user_oauth_approval", "true");
        approvalPrameters.put("authorize", "Authorize");
        authorizationRequest.setApprovalParameters(approvalPrameters);

        // - ResponseTypes
        Set<String> responseTypes = new TreeSet<>();
        responseTypes.add("token");
        authorizationRequest.setResponseTypes(responseTypes);
        // - Authorities
        List<GrantedAuthority> authorities = new ArrayList<>();
        authorities.add(new SimpleGrantedAuthority("ROLE_USER"));
        authorizationRequest.setAuthorities(authorities);

        // - Approved
        authorizationRequest.setApproved(false);

        // - RedirectUr
//        redirectUri = Optional.ofNullable(redirectUri).orElse("http://localhost:24001/");
        authorizationRequest.setRedirectUri(redirectUri);

        // - ClientId
        authorizationRequest.setClientId(clientId);

        // - Scope
        List<String> scopeList = new ArrayList<>();
        scope = Optional.ofNullable(scope).orElse("default");
        scopeList.add(scope);
        authorizationRequest.setScope(scopeList);

        // - Approved
        authorizationRequest.setApproved(true);

        // User
        String username = String.format("%s|%s|%s",ci,uuid,affiliateCode);
        User user = new User(username, "password", authorities);
        log.info("user: "+user);
        UsernamePasswordAuthenticationToken usernamePasswordAuthenticationToken = new UsernamePasswordAuthenticationToken(user, null, authorities);


        OAuth2Authentication oauth2Authentication = new OAuth2Authentication(storedOAuth2Request, usernamePasswordAuthenticationToken);
//        tokenServices.setClientDetailsService(inMemoryClientDetailsService);

        OAuth2AccessToken oauth2AccessToken = tokenServices.createAccessToken(oauth2Authentication);

        //authorization code 삭제
        removeCode(code);

        return oauth2AccessToken;
    }


    /**
     *
     * @param clientId
     * @param ci
     * @param uuid
     * @param affiliateCode
     * @param redirectUri
     * @param scope
     * @return
     */
    public String generateCode(String clientId, String ci, String uuid, String affiliateCode, String redirectUri, String scope) {
        checkAndJoinHanaUsers(ci, uuid, affiliateCode);

        // Authorization Code 생성
        String username = String.format("%s|%s|%s",ci,uuid,affiliateCode);

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

        // - ResourceIds
//        Set<String> resourceIds = new TreeSet<>();
//        resourceIds.add("apis");
//        authorizationRequest.setResourceIds(resourceIds);

        // - Authorities
        List<GrantedAuthority> authorities = new ArrayList<>();
        authorities.add(new SimpleGrantedAuthority("ROLE_USER"));
        authorizationRequest.setAuthorities(authorities);

        // - Approved
        authorizationRequest.setApproved(false);

        // - RedirectUr
//        redirectUri = Optional.ofNullable(redirectUri).orElse(DEFAULT_REDIRECT_URI);
        authorizationRequest.setRedirectUri(redirectUri);

        // - ClientId
        authorizationRequest.setClientId(clientId);

        // - Scope
        List<String> scopeList = new ArrayList<>();
        scope = Optional.ofNullable(scope).orElse("default");
        scopeList.add(scope);
        authorizationRequest.setScope(scopeList);

        // - Approved
        authorizationRequest.setApproved(true);

        // OAuth2RequestFactory
        OAuth2RequestFactory oAuth2RequestFactory = new DefaultOAuth2RequestFactory(hanaJdbcClientDetailsService);

        OAuth2Request storedOAuth2Request = oAuth2RequestFactory.createOAuth2Request(authorizationRequest);

        // User
        User user = new User(username, "password", authorities);
        log.info("user: "+user);

        UsernamePasswordAuthenticationToken usernamePasswordAuthenticationToken = new UsernamePasswordAuthenticationToken(user, null, authorities);

        OAuth2Authentication combinedAuth = new OAuth2Authentication(storedOAuth2Request, (Authentication) usernamePasswordAuthenticationToken);

        JdbcAuthorizationCodeServices jdbcAuthorizationCodeServices = new JdbcAuthorizationCodeServices(dataSource);
        String code = jdbcAuthorizationCodeServices.createAuthorizationCode(combinedAuth);

        log.info("code: "+code);
        return code;
    }

    /**
     * authorization code 삭제
     * @param code
     */
    private void removeCode(String code) {
        JdbcAuthorizationCodeServices jdbcAuthorizationCodeServices = new JdbcAuthorizationCodeServices(dataSource);
        jdbcAuthorizationCodeServices.remove(code);
    }
}
