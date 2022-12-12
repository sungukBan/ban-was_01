package com.was.dto;

public class PaySendSubVo {

	private String roomId      ; // 카톡방ID
	private String sendDt      ; // 뿌린일자
	private String token       ; // 토큰
	private String subSeq      ; // 서브순번
	private int    recvAmt     ; // 받은금액
	private String recvTm      ; // 받은시간
	private String recvUserId  ; // 받은유저ID
	private String recvYn      ; // 받기여부
	
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
	public String getSubSeq() {
		return subSeq;
	}
	public void setSubSeq(String subSeq) {
		this.subSeq = subSeq;
	}
	public int getRecvAmt() {
		return recvAmt;
	}
	public void setRecvAmt(int recvAmt) {
		this.recvAmt = recvAmt;
	}
	public String getRecvTm() {
		return recvTm;
	}
	public void setRecvTm(String recvTm) {
		this.recvTm = recvTm;
	}
	public String getRecvUserId() {
		return recvUserId;
	}
	public void setRecvUserId(String recvUserId) {
		this.recvUserId = recvUserId;
	}
	public String getRecvYn() {
		return recvYn;
	}
	public void setRecvYn(String recvYn) {
		this.recvYn = recvYn;
	}
}
