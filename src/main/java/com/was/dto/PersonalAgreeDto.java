package com.was.dto;

public class PersonalAgreeDto {

	private String cino      = "";
	private String appname   = "";
	private String agree_gb  = "";
	private String agree_id  = "";
	private String agree_ver = "";
	private String agreedate = "";
	private String canceldate= "";
	private String callapi   = "";
	private String corpcd    = "";
	private String corpname  = "";
	private String agree_yn  = "";
	private String app_key  = "";

	public void setType(String type) {
		this.type = type;
	}

	public void setCorpid(String corpid) {
		this.corpid = corpid;
	}

	private String type  = "";

	public String getType() {
		return type;
	}

	public String getCorpid() {
		return corpid;
	}

	private String corpid  = "";

	public void setApp_key(String app_key) {
		this.app_key = app_key;
	}

	public String getApp_key() {
		return app_key;
	}

	public String getCino() {
		return cino;
	}

	public String getAppname() {
		return appname;
	}

	public String getAgree_gb() {
		return agree_gb;
	}

	public String getAgree_id() {
		return agree_id;
	}

	public String getAgree_ver() {
		return agree_ver;
	}

	public String getAgreedate() {
		return agreedate;
	}

	public String getCanceldate() {
		return canceldate;
	}

	public String getCallapi() {
		return callapi;
	}

	public String getCorpcd() {
		return corpcd;
	}

	public String getCorpname() {
		return corpname;
	}

	public String getAgree_yn() {
		return agree_yn;
	}

	public void setCino(String cino) {
		this.cino = cino;
	}

	public void setAppname(String appname) {
		this.appname = appname;
	}

	public void setAgree_gb(String agree_gb) {
		this.agree_gb = agree_gb;
	}

	public void setAgree_id(String agree_id) {
		this.agree_id = agree_id;
	}

	public void setAgree_ver(String agree_ver) {
		this.agree_ver = agree_ver;
	}

	public void setAgreedate(String agreedate) {
		this.agreedate = agreedate;
	}

	public void setCanceldate(String canceldate) {
		this.canceldate = canceldate;
	}

	public void setCallapi(String callapi) {
		this.callapi = callapi;
	}

	public void setCorpcd(String corpcd) {
		this.corpcd = corpcd;
	}

	public void setCorpname(String corpname) {
		this.corpname = corpname;
	}

	public void setAgree_yn(String agree_yn) {
		this.agree_yn = agree_yn;
	}
}
