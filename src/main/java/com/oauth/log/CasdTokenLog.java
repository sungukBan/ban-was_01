package com.oauth.log;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 카드사 토큰 발급 이력을 카프카에 전달하기 위한 클래스
 * logback-spring.xml에 이 클래스를 바라보는 logger가 추가됨.
 * @author 이호철
 *
 */
public class CasdTokenLog {
	private static final Logger log = LoggerFactory.getLogger(CasdTokenLog.class);

	public static void print(String msg) {
		log.info(msg);
	}
}
