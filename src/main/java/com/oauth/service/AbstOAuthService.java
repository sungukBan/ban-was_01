package com.oauth.service;

import com.oauth.hanaClient.HanaJdbcClientDetailsService;
import com.oauth.mapper.HanaUsersMapper;
import com.oauth.model.HanaUsers;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.provider.ClientDetails;
import org.springframework.security.oauth2.provider.NoSuchClientException;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import javax.sql.DataSource;
import java.time.LocalDateTime;

@Service
public class AbstOAuthService {

    private static final Logger log = LoggerFactory.getLogger(AbstOAuthService.class);

    @Autowired
    DataSource dataSource;

    @Autowired
    HanaJdbcClientDetailsService hanaJdbcClientDetailsService;

    private String credentialsCharset = "UTF-8";

    @Autowired
    private HanaUsersMapper hanaUsersMapper;

    @Autowired
    PasswordEncoder passwordEncoder;

    /**
     * CI 가입 여부 체크 후 가입 처리
     *
     * @param ci
     * @param uuid
     */
    public void checkAndJoinHanaUsers(String ci, String uuid, String affiliateCode) {
        HanaUsers hanaUsersTemp = new HanaUsers();
        hanaUsersTemp.setCi(ci);
        hanaUsersTemp.setUuid(uuid);
        hanaUsersTemp.setAffiliateCode(affiliateCode);

        HanaUsers hanaUser = hanaUsersMapper.selectHanaUsers(hanaUsersTemp);

        if (hanaUser == null) {
            HanaUsers newHanaUsers = new HanaUsers();
            newHanaUsers.setCi(ci);
            newHanaUsers.setUuid(uuid);
            newHanaUsers.setAffiliateCode(affiliateCode);
            newHanaUsers.setRegDate(LocalDateTime.now());
            newHanaUsers.setModDate(LocalDateTime.now());

            hanaUsersMapper.insertHanaUsers(newHanaUsers);
        }
    }

    /**
     * Client credentials 확인
     * @param clientId
     * @param clientSecret
     * @return
     */
    public String checkClientCredentials(String clientId, String clientSecret) {

        HanaJdbcClientDetailsService hanaJdbcClientDetailsService = new HanaJdbcClientDetailsService(dataSource);
        try {
            ClientDetails clientDetails = hanaJdbcClientDetailsService.loadClientByClientId(clientId);

            if (clientDetails == null) {
                throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "clientId is not exist.");
            }

            boolean isSecretValid = passwordEncoder.matches(clientSecret, clientDetails.getClientSecret());

            if (!isSecretValid) {
                throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "clientSecret is not matched.");
            }
        } catch (NoSuchClientException nsc) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, nsc.getMessage());
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        }
        return clientId;
    }
//
//    public String getClientIdFromAuthorizationHeader(HttpServletRequest request, String header) throws IOException {
//        log.info("getClientIdFromAuthorizationHeader");
//        if (!StringUtils.hasText(header)) {
//            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Authorization-For-Code header must not be null and blank.");
//        }
//
//        String[] tokens = extractAndDecodeHeader(header, request);
//
//        return tokens[0];
//    }


}
