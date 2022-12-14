package com.was.service;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.was.dto.PersonalAgreeDto;
import com.was.mapper.PersonalMapper;
import org.apache.tomcat.util.json.JSONParser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import javax.servlet.http.HttpServletRequest;

@Service
public class PersonalServiceImpl implements PersonalService {

	@Autowired
	private PersonalMapper personalMapper;

	@Override
	public String insertAgree(HttpServletRequest request, String jsonStr) {

		String resultStr = "";
		PersonalAgreeDto personalAgreeDto = new PersonalAgreeDto();
		JsonParser jsonParser = new JsonParser();
		JsonElement element   = jsonParser.parse(jsonStr);
		JsonObject jsonBody   = (JsonObject) element.getAsJsonObject().get("dataBody");

		// 필수 입력값 검증
		if ( doValidationInfo(jsonBody) == false ) {
			// 에러 리턴
		}

		personalAgreeDto.setApp_key(jsonBody.get("clientId").getAsString());
		personalAgreeDto.setAgree_id(jsonBody.get("collAgreeId").getAsString());
		personalAgreeDto.setAgree_ver(jsonBody.get("collAgreeVer").getAsString());
		personalAgreeDto.setCino(jsonBody.get("cino").getAsString());

		PersonalAgreeDto selectData = personalMapper.selectOganizationByCd(personalAgreeDto);

		personalAgreeDto.setType(selectData.getType());

		if (selectData.getType().equals("PROVIDER")) {
			personalAgreeDto.setCorpcd("KB");
		} else {
			personalAgreeDto.setCorpcd(selectData.getCorpcd());
		}


		return resultStr;
	}

	// 필수 입력값 검증
	public boolean doValidationInfo(JsonObject jsonBody) {

		if ( jsonBody.getAsJsonObject("clientId") == null ) {
			return false;
		}

		if ( jsonBody.getAsJsonObject("collAgreeId") == null ) {
			return false;
		}

		if ( jsonBody.getAsJsonObject("collAgreeVer") == null ) {
			return false;
		}

		if ( jsonBody.getAsJsonObject("ciNo") == null ) {
			return false;
		}

		return true;
	}

}
