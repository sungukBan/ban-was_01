package com.was.biz;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.was.constant.Constant;

public class SetJsonData {

	public static String setResponseData2(String jsonStr, String rlstCd, String rlstMsg) {

		JsonObject resultJson = new JsonObject();

		try {
			String SUCCESS_CD = "0";
			JsonParser jsonParser = new JsonParser();
			JsonElement element   = jsonParser.parse(jsonStr);
			JsonObject jsonHeader = new JsonObject();
			JsonObject jsonBody   = (JsonObject) element.getAsJsonObject().get(Constant.DATA_BODY);
			if ( "200".equals(rlstCd) ) {
				SUCCESS_CD = "1";
			}

			jsonHeader.addProperty("successCode", SUCCESS_CD);
			jsonHeader.addProperty("resultCode", rlstCd);
			jsonHeader.addProperty("resultMessage", rlstMsg);

			resultJson.add("dataHeader", jsonHeader);
			resultJson.add("dataBody", jsonBody);

		} catch(Exception e) {
			e.printStackTrace();
		}

		System.out.println("resultJson.toString() :: " + resultJson.toString());
		return resultJson.toString();
	}

	public static String setResponseData(String jsonStr, APIStatus status) {

		JsonObject resultJson = new JsonObject();

		try {
			String SUCCESS_CD = "0";
			JsonParser jsonParser = new JsonParser();
			JsonElement element   = jsonParser.parse(jsonStr);
			JsonObject jsonHeader = new JsonObject();
			JsonObject jsonBody   = (JsonObject) element.getAsJsonObject().get(Constant.DATA_BODY);
			if ( status.getRLST_CD().equals("200") ) {
				SUCCESS_CD = "1";
			}

			jsonHeader.addProperty("successCode", SUCCESS_CD);
			jsonHeader.addProperty("resultCode", status.getRLST_CD());
			jsonHeader.addProperty("resultMessage", status.getRLST_MSG());

			resultJson.add("dataHeader", jsonHeader);
			resultJson.add("dataBody", jsonBody);

		} catch(Exception e) {
			e.printStackTrace();
		}

		System.out.println("resultJson.toString() :: " + resultJson.toString());
		return resultJson.toString();
	}

	
}
