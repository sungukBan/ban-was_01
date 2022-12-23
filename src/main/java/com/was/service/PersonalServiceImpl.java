package com.was.service;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.was.biz.APIStatus;
import com.was.biz.SetJsonData;
import com.was.constant.Constant;
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
	public String selectAgree(HttpServletRequest request, String jsonStr) {
		String resultStr = jsonStr;
		APIStatus status = new APIStatus();
		PersonalAgreeDto personalAgreeDto = new PersonalAgreeDto();
		JsonParser jsonParser = new JsonParser();

		try {
			JsonElement element   = jsonParser.parse(jsonStr);
			JsonObject jsonBody   = (JsonObject) element.getAsJsonObject().get(Constant.DATA_BODY);

			if ( jsonBody.get("ciNo") == null ) {
				status.setAPIStatus(Constant.RLST_CD_901, Constant.RLST_MSG_901);
				return SetJsonData.setResponseData(resultStr, status);
			}
			personalAgreeDto.setCiNo(jsonBody.get("ciNo").getAsString());

			PersonalAgreeDto selectData;
			try {
				// 등록된 동의내역을 조회한다
				selectData = personalMapper.selectMNGAgree(personalAgreeDto);
			} catch (Exception e) {
				e.printStackTrace();
				status.setAPIStatus(Constant.RLST_CD_998, Constant.RLST_MSG_998);
				return SetJsonData.setResponseData(resultStr, status);
			}

			jsonBody = new JsonObject();
			jsonBody.addProperty("collAgreeYn", selectData.getAgreeYn());
			jsonBody.addProperty("agreeYn", selectData.getAgreeYn());
			jsonBody.addProperty("collAgreeID", selectData.getAgreeId());
			jsonBody.addProperty("collAgreeVer", selectData.getAgreeVer());

			System.out.println("selectData.getAgreeYn() :: " + selectData.getAgreeYn());
			System.out.println("selectData.getAgreeId() :: " + selectData.getAgreeId());
			System.out.println("selectData.getAgreeVer() :: " + selectData.getAgreeVer());
			System.out.println("selectData.getBody() :: " + selectData.getBody());

			if ( "Y".equals(selectData.getAgreeYn()) ) {
				status.setAPIStatus(Constant.RLST_CD_200, Constant.RLST_MSG_000);
				jsonBody.addProperty("collAgreeBody", "");
				jsonBody.addProperty("resCd", "200");
			} else {
				status.setAPIStatus(Constant.RLST_CD_300, Constant.RLST_MSG_300);
				jsonBody.addProperty("collAgreeBody", selectData.getBody());
				jsonBody.addProperty("resCd", "300");
			}
			JsonObject jsonObject = new JsonObject();
			jsonObject.add(Constant.DATA_BODY, jsonBody);
			resultStr = jsonObject.toString();
			System.out.println("resultStr :: " + resultStr);

		} catch (Exception e) {
			e.printStackTrace();
			status.setAPIStatus(Constant.RLST_CD_999, Constant.RLST_MSG_999);
			return SetJsonData.setResponseData(resultStr, status);
		} finally {
			return SetJsonData.setResponseData(resultStr, status);
		}
	}

	@Override
	public String insertAgree(HttpServletRequest request, String jsonStr) {

		String resultStr = jsonStr;
		APIStatus status = new APIStatus();
		PersonalAgreeDto personalAgreeDto = new PersonalAgreeDto();
		JsonParser jsonParser = new JsonParser();

		try {
			JsonElement element   = jsonParser.parse(jsonStr);
			JsonObject jsonBody   = (JsonObject) element.getAsJsonObject().get("dataBody");

			// 필수 입력값 검증
			if ( doValidationInfo(jsonBody, status) == false ) {
				return SetJsonData.setResponseData(resultStr, status);
			}

			personalAgreeDto.setCiNo(jsonBody.get("ciNo").getAsString());
			personalAgreeDto.setAppKey(jsonBody.get("clientId").getAsString());
			personalAgreeDto.setAgreeId(jsonBody.get("collAgreeId").getAsString());
			personalAgreeDto.setAgreeVer(jsonBody.get("collAgreeVer").getAsString());
			personalAgreeDto.setAgreeYn(jsonBody.get("collAgreeYn").getAsString());

			try {
				// 등록된 동의서를 조회한다
				int cnt = personalMapper.selectAgreeOne(personalAgreeDto);

				jsonBody = new JsonObject();
				jsonBody.addProperty("ciNo", personalAgreeDto.getCiNo());

				// 미등록 CI인 경우 동의서 등록한다
				if ( cnt == 0 ) {
					personalMapper.insertAgree(personalAgreeDto);
					status.setAPIStatus(Constant.RLST_CD_200, Constant.RLST_MSG_000);
					jsonBody.addProperty("resCd", "200");
				}
				// 기등록된 CI
				else {
					status.setAPIStatus(Constant.RLST_CD_913, Constant.RLST_MSG_913);
					jsonBody.addProperty("resCd", "300");
				}
			} catch (Exception e) {
				e.printStackTrace();
				status.setAPIStatus(Constant.RLST_CD_998, Constant.RLST_MSG_998);
				return SetJsonData.setResponseData(resultStr, status);
			}

			JsonObject jsonObject = new JsonObject();
			jsonObject.add(Constant.DATA_BODY, jsonBody);
			resultStr = jsonObject.toString();
			System.out.println("resultStr :: " + resultStr);

		} catch (Exception e) {
			e.printStackTrace();
			status.setAPIStatus(Constant.RLST_CD_999, Constant.RLST_MSG_999);
			return SetJsonData.setResponseData(resultStr, status);
		} finally {
			return SetJsonData.setResponseData(resultStr, status);
		}
	}

	// 필수 입력값 검증
	public boolean doValidationInfo(JsonObject jsonBody, APIStatus status) {

		if ( jsonBody.get("ciNo") == null ) {
			status.setAPIStatus(Constant.RLST_CD_901, Constant.RLST_MSG_901);
			return false;
		}

		if ( jsonBody.get("clientId") == null ) {
			status.setAPIStatus(Constant.RLST_CD_902, Constant.RLST_MSG_902);
			return false;
		}

		if ( jsonBody.get("collAgreeId") == null ) {
			status.setAPIStatus(Constant.RLST_CD_903, Constant.RLST_MSG_903);
			return false;
		}

		if ( jsonBody.get("collAgreeVer") == null ) {
			status.setAPIStatus(Constant.RLST_CD_904, Constant.RLST_MSG_904);
			return false;
		}

		return true;
	}

}
