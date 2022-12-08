package com.oauth.model;

import java.time.LocalDateTime;


public class HanaUsers {

    String ci;
	String uuid;
    String affiliateCode;
    LocalDateTime regDate;
    LocalDateTime modDate;
    
    public String getCi() {
		return ci;
	}
	public void setCi(String ci) {
		this.ci = ci;
	}
	public String getUuid() {
		return uuid;
	}
	public void setUuid(String uuid) {
		this.uuid = uuid;
	}
	public String getAffiliateCode() {
		return affiliateCode;
	}
	public void setAffiliateCode(String affiliateCode) {
		this.affiliateCode = affiliateCode;
	}
	public LocalDateTime getRegDate() {
		return regDate;
	}
	public void setRegDate(LocalDateTime regDate) {
		this.regDate = regDate;
	}
	public LocalDateTime getModDate() {
		return modDate;
	}
	public void setModDate(LocalDateTime modDate) {
		this.modDate = modDate;
	}
}
