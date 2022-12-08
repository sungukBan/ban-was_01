package com.oauth.configurer;

import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.security.oauth2.provider.OAuth2Request;

public class TIOAuth2Authentication extends OAuth2Authentication {
    /**
     * Construct an OAuth 2 authentication. Since some grant types don't require user authentication, the user
     * authentication may be null.
     *
     * @param storedRequest      The authorization request (must not be null).
     * @param userAuthentication The user authentication (possibly null).
     */


    private int expireSecond ;
    public TIOAuth2Authentication(OAuth2Request storedRequest, Authentication userAuthentication) {
        super(storedRequest, userAuthentication);
    }
}
