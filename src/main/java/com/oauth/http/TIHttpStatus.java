package com.oauth.http;

import org.springframework.lang.Nullable;

/** 
 * HTTP Response Code 정의
 * @author hclee
 *
 */
public enum TIHttpStatus {
	
	OK(200, "OK"),
	
	INVALID_REQUEST_CODE(302, "invalid_request"),
	UNAUTHORIZED_CLIENT_CODE(302, "unauthorized_client"),
	ACCESS_DENIED(302, "access_denied"),
	UNSUPPORTED_RESPONSE_TYPE(302, "unsupported_response_type"),
	SERVER_ERROR(302, "server_error"),
	TEMPORARILY_UNAVAILABLE(302, "temporarily_unavailable"),
	
	
	INVALID_REQUEST_ACCESS(400, "invalid_request"),
	INVALID_CLIENT(400, "invalid client"),
	INVALID_GRANT(400, "invalid_grant"),
	UNAUTHORIZED_CLIENT_ACCESS(400, "unauthorized_client"),
	UNSUPPORTED_GRANT_TYPE(400, "unsupported_grant_type"),
	INVALID_SCOPE(400, "invalid_scope"),
	UNSUPPORTED_TOKEN_TYPE(400, "unsupported_token_type"),
	INVALID_TOKEN(400, "invalid_token"),
	
	ERROR(503, "unknown error");
	
	
	private final int value;

	private final String reasonPhraseCode;


	TIHttpStatus(int value, String reasonPhraseCode) {
		this.value = value;
		this.reasonPhraseCode = reasonPhraseCode;
	}


	/**
	 * Return the integer value of this status code.
	 */
	public int value() {
		return this.value;
	}

	/**
	 * Return the reason phrase of this status code.
	 */
	public String getReasonPhraseCode() {
		return this.reasonPhraseCode;
	}
 
	
	@Nullable
	public static TIHttpStatus resolve(int statusCode) {
		for (TIHttpStatus status : values()) {
			if (status.value == statusCode) {
				return status;
			}
		}
		return null;
	}

}