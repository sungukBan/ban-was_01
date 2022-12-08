package com.oauth.controller;

import com.oauth.hanaClient.HanaJdbcClientDetailsService;
import com.oauth.service.AuthorizationCodeService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.common.OAuth2AccessToken;
import org.springframework.stereotype.Controller;
import org.springframework.util.StringUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Controller
@Validated
public class AuthorizationCodeController extends AbstOAuthController {

    private static final Logger log = LoggerFactory.getLogger(AuthorizationCodeController.class);
    @Autowired
    HanaJdbcClientDetailsService jdbcClientDetailsService;
    @Value("${oauth.config.reuseB2CToken}")
    private boolean reuseB2CToken;

    @Autowired
    private AuthorizationCodeService authorizationCodeService;

//    /**
//     * Authorization Code Redirect 요청
//     * <p>
//     * e.g. http://localhost:24001/oauth/authorization_code?client_id=client&redirect_uri=http://localhost:24001/&scope=default&username=testuser
//     *
//     * @param clientId
//     * @param redirectUri
//     * @param scope
//     * @return
//     */
//    @Deprecated
//    @RequestMapping(value = "/oauth/authorization_code_redirect")
//    public ModelAndView authorizationCodeRedirect(@RequestParam(value = "client_id") String clientId
//            , @Valid @NotBlank @RequestParam(value = "username") String username
//            , @RequestParam(value = "redirect_uri", required = false) String redirectUri
//            , @RequestParam(value = "scope", required = false) String scope
//            , ModelMap model) {
//        Map<String, String> parameters = new HashMap<String, String>();
//        parameters.put("client_id", clientId);
//        parameters.put("username", username);
//        parameters.put("redirect_uri", redirectUri);
//        parameters.put("scope", scope);
//
//        String code = authorizationCodeService.generateCode(parameters);
//
//        RedirectView redirectView = new RedirectView();
//        redirectView.setUrl(redirectUri);
//
//        model.addAttribute("code", code);
//        return new ModelAndView(redirectView, model);
//    }

    /**
     * Authorization Code 요청
     *
     * @param request
     * @param parameters
     * @return
     * @throws IOException
     * xss 대응을 위한 replaceAll 부분 추가
     */
    @PostMapping(value = "/oauth/authorization_code")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> authorizationCode(HttpServletRequest request
            , @RequestParam Map<String, String> parameters) throws IOException {
        String header = request.getHeader("Authorization");
        String headerForCode = request.getHeader("Authorization-For-Code");

        String ci ;
        String uuid ;
        String affiliateCode ;
        String redirectUri = DEFAULT_REDIRECT_URI;
        String scope ;
//        if (StringUtils.hasText(parameters.get(REDIRECT_URI))) {
//            redirectUri = xss(parameters.get(REDIRECT_URI));
//        } else {
//            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "redirect_uri must not be null and blank.");
//        }
        if (StringUtils.hasText(parameters.get(SCOPE))) {
            scope = xss(parameters.get(SCOPE));
        } else {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "scope must not be null and blank.");
        }
        if (StringUtils.hasText(parameters.get(CI))) {
            ci = xss(parameters.get(CI));
        } else {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "ci must not be null and blank.");
        }
        if (StringUtils.hasText(parameters.get(USER_UUID))) {
            uuid = xss(parameters.get(USER_UUID));
        } else {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "uuid must not be null and blank.");
        }
        if (StringUtils.hasText(parameters.get(AFFILIATE_CODE))) {
            affiliateCode = xss(parameters.get(AFFILIATE_CODE));
        } else {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "affiliate_code must not be null and blank.");
        }

        //client_id, client_secret
        log.info("==========request=======" + request) ;
        log.info("==========header=======" + header) ;
        if (!StringUtils.hasText(header)) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Authorization header must not be null and blank.");
        }

        String[] tokens = extractAndDecodeHeader(header, request);
        String clientId = tokens[0];
        String clientSecret = tokens[1];
        log.info("clientId: "+clientId);
        log.info("clientSecret: "+clientSecret);

//        authorizationCodeService.checkClientCredentials(clientId, clientSecret);
        String code = authorizationCodeService.generateCode(clientId, ci, uuid, affiliateCode, redirectUri, scope);
        Map<String, Object> map = new HashMap<>();
        map.put("code", code);

        return ResponseEntity.ok(map);
    }

    /**
     * Authorization Code 요청
     *
     * @param request
     * @param parameters
     * @return
     * @throws IOException
     * xss 대응을 위한 replaceAll 부분 추가
     */
    @PostMapping(value = "/oauth/codeAuthorize")
    @ResponseBody
    public ResponseEntity<OAuth2AccessToken> authorize(HttpServletRequest request
            , @RequestParam Map<String, String> parameters) throws IOException {
        String header = request.getHeader("Authorization");
        String headerForCode = request.getHeader("Authorization-For-Code");

        String ci ;
        String uuid ="" ;
        String affiliateCode ="" ;
        String redirectUri = DEFAULT_REDIRECT_URI;
        String scope ;
//        if (StringUtils.hasText(parameters.get(REDIRECT_URI))) {
//            redirectUri = xss(parameters.get(REDIRECT_URI));
//        } else {
//            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "redirect_uri must not be null and blank.");
//        }

        if (StringUtils.hasText(parameters.get(GRANT_TYPE))) {
            String grantType = xss(parameters.get(GRANT_TYPE)).trim();
            if (!grantType.equals("authorization_code")) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "invalid grant type");
            }
        } else {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "grant type must not be null and blank.");
        }

        if (StringUtils.hasText(parameters.get(SCOPE))) {
            scope = xss(parameters.get(SCOPE));
        } else {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "scope must not be null and blank.");
        }
        if (StringUtils.hasText(parameters.get(UNIQUE_ID))) {
            ci = xss(parameters.get(UNIQUE_ID));
        } else {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "UNIQUE_ID must not be null and blank.");
        }
        if (StringUtils.hasText(parameters.get(USER_UUID))) {
            uuid = xss(parameters.get(USER_UUID));
        }
        if (StringUtils.hasText(parameters.get(AFFILIATE_CODE))) {
            affiliateCode = xss(parameters.get(AFFILIATE_CODE));
        }

        //client_id, client_secret
        log.info("==========request=======" + request) ;
        log.info("==========header=======" + header) ;
        if (!StringUtils.hasText(header)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Authorization header must not be null and blank.");
        }

        String[] tokens = extractAndDecodeHeader(header, request);
        String clientId = tokens[0];
        String clientSecret = tokens[1];
        log.info("clientId: "+clientId);
        log.info("clientSecret: "+clientSecret);

        if (reuseB2CToken == false) {
            String sso_token = parameters.containsKey("sso_token") ? parameters.get("sso_token") : "";

            affiliateCode = String.format("%s|%s", affiliateCode, sso_token);
        }
        authorizationCodeService.checkClientCredentials(clientId, clientSecret);
        OAuth2AccessToken oAuth2AccessToken = authorizationCodeService.getUserToken(clientId, ci, uuid, affiliateCode, redirectUri, scope, parameters);

        //save db...
        try {
            //이력저장
            log.info("refresh_token" + oAuth2AccessToken.getRefreshToken().getValue());
            parameters.put("oauth_token", oAuth2AccessToken.getValue());
            authorizationCodeService.saveUserInfo(parameters);
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        }
        return ResponseEntity.ok(oAuth2AccessToken);
    }

    /**
     * client_id, client_secret 검증
     * @param request
     * @return
     * @throws IOException
     */
    @RequestMapping(value = "/oauth/api/checkClient", method = RequestMethod.POST)
    public ResponseEntity<Map<String, Object>> checkClient(HttpServletRequest request) throws IOException {
        String header = request.getHeader("Authorization");
        if (!StringUtils.hasText(header)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Authorization header must not be null and blank.");
        }

        String[] tokens = extractAndDecodeHeader(header, request);
        String clientId = tokens[0];
        String clientSecret = tokens[1];
        log.debug("clientId: "+clientId);
        log.debug("clientSecret: "+clientSecret);

        authorizationCodeService.checkClientCredentials(clientId, clientSecret);

        Map<String, Object> map = new HashMap<>();
        map.put("result", "OK");

        return ResponseEntity.ok(map);
    }
}
