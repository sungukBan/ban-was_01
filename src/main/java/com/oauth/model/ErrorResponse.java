package com.oauth.model;

import com.oauth.http.TIHttpStatus;


public class ErrorResponse {
	private String error;
	private String error_description;
	
	public ErrorResponse(TIHttpStatus status) {
		this.error = status.getReasonPhraseCode();
		this.error_description = status.getReasonPhraseCode();
	}
	
	public String getError() {
		return error;
	}

	public void setError(String error) {
		this.error = error;
	}

	public String getError_description() {
		return error_description;
	}

	public void setError_description(String error_description) {
		this.error_description = error_description;
	}
}
