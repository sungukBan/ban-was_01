package com.was.dto;

public class PaySendVo {

	private String roomId        ; // 카톡방ID
	private String sendDt        ; // 뿌린일자
	private String token         ; // 토큰    
	private int    sendAmt       ; // 뿌린금액
	private int    sendCnt       ; // 뿌린인원
	private String sendTm        ; // 뿌린시간
	private String userId        ; // 뿌린유저
	private int    succesAmt     ; // 완료금액
	private String inq_start_dt  ; // 조회시작일자
	private String inq_end_dt    ; // 조회종료일자
	private String inq_start_tm  ; // 조회시작시간
	private String inq_end_tm    ; // 조회종료시간
	
	public String getRoomId() {
		return roomId;
	}
	public void setRoomId(String roomId) {
		this.roomId = roomId;
	}
	public String getSendDt() {
		return sendDt;
	}
	public void setSendDt(String sendDt) {
		this.sendDt = sendDt;
	}
	public String getToken() {
		return token;
	}
	public void setToken(String token) {
		this.token = token;
	}
	public int getSendAmt() {
		return sendAmt;
	}
	public void setSendAmt(int sendAmt) {
		this.sendAmt = sendAmt;
	}
	public int getSendCnt() {
		return sendCnt;
	}
	public void setSendCnt(int sendCnt) {
		this.sendCnt = sendCnt;
	}
	public String getSendTm() {
		return sendTm;
	}
	public void setSendTm(String sendTm) {
		this.sendTm = sendTm;
	}
	public String getUserId() {
		return userId;
	}
	public void setUserId(String userId) {
		this.userId = userId;
	}
	public int getSuccesAmt() {
		return succesAmt;
	}
	public void setSuccesAmt(int succesAmt) {
		this.succesAmt = succesAmt;
	}
	public String getInq_start_dt() {
		return inq_start_dt;
	}
	public void setInq_start_dt(String inq_start_dt) {
		this.inq_start_dt = inq_start_dt;
	}
	public String getInq_end_dt() {
		return inq_end_dt;
	}
	public void setInq_end_dt(String inq_end_dt) {
		this.inq_end_dt = inq_end_dt;
	}
	public String getInq_start_tm() {
		return inq_start_tm;
	}
	public void setInq_start_tm(String inq_start_tm) {
		this.inq_start_tm = inq_start_tm;
	}
	public String getInq_end_tm() {
		return inq_end_tm;
	}
	public void setInq_end_tm(String inq_end_tm) {
		this.inq_end_tm = inq_end_tm;
	}
}
