package com.oauth.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.Base64;

@Controller
@Validated
public class AbstOAuthController {

    private static final Logger log = LoggerFactory.getLogger(AbstOAuthController.class);

    protected final static String DEFAULT_REDIRECT_URI = "http://127.0.0.1:24001";

    protected final static String REDIRECT_URI = "redirect_uri";
    protected final static String GRANT_TYPE = "grant_type";
    protected final static String SCOPE = "scope";
    protected final static String CI = "ci";
    protected final static String UNIQUE_ID = "unique_id";
    protected final static String USER_UUID = "uuid";
    protected final static String AFFILIATE_CODE = "affiliate_code";

    private String credentialsCharset = "UTF-8";

    /**
     * Decodes the header into a username and password.
     *
     * @throws BadCredentialsException if the Basic header is not present or is not valid
     *                                 Base64
     */
    protected String[] extractAndDecodeHeader(String header, HttpServletRequest request)
            throws IOException {

        byte[] base64Token = header.substring(6).getBytes("UTF-8");
        byte[] decoded;
        try {
            decoded = Base64.getDecoder().decode(base64Token);
        } catch (IllegalArgumentException e) {
            throw new BadCredentialsException(
                    "Failed to decode basic authentication token");
        }

        String token = new String(decoded, getCredentialsCharset(request));
        log.info("token: "+token);
        log.info("decoded: "+decoded);
        int delim = token.indexOf(":");
        log.info("delim: "+delim);
        if (delim == -1) {
            throw new BadCredentialsException("Invalid basic authentication token");
        }
        return new String[]{token.substring(0, delim), token.substring(delim + 1)};
    }

    protected String getCredentialsCharset(HttpServletRequest httpRequest) {
        return this.credentialsCharset;
    }

    /**
     * xss 대응을 위한 replaceAll 부분 추가
     * @param src
     * @return
     */
    protected String xss(String src) {
        String rtn = src;
        if (rtn == null)
            return rtn;

        rtn = rtn.replaceAll("<", "&lt;");
        rtn = rtn.replaceAll(">", "&gt;");
        return rtn;
    }
}
