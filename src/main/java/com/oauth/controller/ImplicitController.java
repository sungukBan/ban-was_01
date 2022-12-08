package com.oauth.controller;

import com.oauth.service.ImplicitService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;

@Controller
@Validated
public class ImplicitController extends AbstOAuthController {

    private static final Logger log = LoggerFactory.getLogger(ImplicitController.class);

    @Autowired
    private ImplicitService implicitService;

//    /**
//     * authorize 요청
//     *
//     * @param request
//     * @param parameters
//     * @return
//     * @throws IOException
//     * xss 대응을 위한 replaceAll 부분 추가
//     */
//    @PostMapping(value = "/oauth/implicitAuthorize")
//    @ResponseBody
//    public ResponseEntity<OAuth2AccessToken> authorize(HttpServletRequest request
//            , @RequestParam Map<String, String> parameters) throws IOException {
//        String header = request.getHeader("Authorization");
//
//        String redirectUri = DEFAULT_REDIRECT_URI;
////        client_id=client&
////        response_type=token&scope=read_profile&state=test 으로 웹브라우저로 요청합니다.
//
//        String scope;
//        String ci;
//        String uuid;
//        String affiliateCode;
//
//        String responseType = "token";
//        String state = "system";
//        if (StringUtils.hasText(parameters.get(SCOPE))) {
//            scope = xss(parameters.get(SCOPE));
//        } else {
//            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "scope must not be null and blank.");
//        }
//        if (StringUtils.hasText(parameters.get(CI))) {
//            ci = xss(parameters.get(CI));
//        } else {
//            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "ci must not be null and blank.");
//        }
//        if (StringUtils.hasText(parameters.get(USER_UUID))) {
//            uuid = xss(parameters.get(USER_UUID));
//        } else {
//            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "uuid must not be null and blank.");
//        }
//        if (StringUtils.hasText(parameters.get(AFFILIATE_CODE))) {
//            affiliateCode = xss(parameters.get(AFFILIATE_CODE));
//        } else {
//            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "affiliate_code must not be null and blank.");
//        }
//
//        // Client credentials 확인
//        String[] tokens = extractAndDecodeHeader(header, request);
//        String clientId = tokens[0];
//        String clientSecret = tokens[1];
//        log.info("clientId: "+clientId);
//        log.info("clientSecret: "+clientSecret);
//        implicitService.checkClientCredentials(clientId, clientSecret);
//
//        OAuth2AccessToken oAuth2AccessToken = implicitService.getUserToken(clientId, ci, uuid, affiliateCode, redirectUri, scope, parameters);
//
//        DefaultOAuth2AccessToken norefresh = new DefaultOAuth2AccessToken(oAuth2AccessToken);
//        norefresh.setRefreshToken(null);
//        return ResponseEntity.ok(norefresh);
//    }


}
