package com.oauth.mapper;

import com.oauth.model.OAuthRefreshToken;
import org.apache.ibatis.annotations.Mapper;

import javax.transaction.Transactional;

@Mapper
@Transactional
public interface OAuthRefreshTokenMapper {

	/**
	 * 토큰 조회
	 * @param tokenId
	 * @return
	 */
    OAuthRefreshToken findRefreshTokenByTokenId(String tokenId);

    /**
     * 토큰 저장
     * @param refreshToken
     * @return
     */
    int saveRefreshToken(OAuthRefreshToken refreshToken);
    
    /**
     * 토큰 삭제
     * @param refreshToken
     * @return
     */
    int deleteRefreshToken(OAuthRefreshToken refreshToken);
}
