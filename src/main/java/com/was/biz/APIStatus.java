package com.was.biz;

public class APIStatus {

	private String RLST_CD  ; // 응답코드
	private String RLST_MSG ; // 응답메시지
	private String SUCCESS_CD; // 상태코드
	
	public void setAPIStatus(String RLST_CD, String RLST_MSG) {
		this.RLST_CD  = RLST_CD;
		this.RLST_MSG = RLST_MSG;
	}
	public String getRLST_CD() {
		return RLST_CD;
	}
	public String getRLST_MSG() {
		return RLST_MSG;
	}

	
}
