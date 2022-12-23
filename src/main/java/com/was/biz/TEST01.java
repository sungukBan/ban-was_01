package com.was.biz;


import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.was.constant.Constant;

public class TEST01 {

	public static void main(String[] args) {

        test_biz2();
	}
	
    public static void test_biz() {

        String jsonStr = "{\n" +
                " \"dataHeader\" : {\n" +
                " },\n" +
                " \"dataBody\" : {\n" +
                " \"clientId\" : \"l7xxlhn5qRkVmys7G2SIiEicpTMSjAyOPTeM\",\n" +
                " \"ciNo\" :\"test1234512345\" \n" +
                " }\n" +
                "}";

        JsonParser jsonParser = new JsonParser();
        JsonElement element   = jsonParser.parse(jsonStr);
        JsonObject jsonBody   = (JsonObject) element.getAsJsonObject().get(Constant.DATA_BODY);

        System.out.println(jsonBody.toString());
        System.out.println(jsonBody.get("ciNo"));
        System.out.println(jsonBody.get("ciNo").getAsString());
        System.out.println(jsonBody.get("ciNo").toString());

        if ( jsonBody.get("ciNo1") == null ) {
            System.out.println("널");
        } else {
            System.out.println("정상");
        }


        /*

        jsonBody.add();
        jsonBody = element.getAsJsonObject().get("dataBody");
        */

    }

    public static void test_biz2() {
        JsonObject jsonBody = new JsonObject();
        jsonBody.addProperty("collAgreeBody", "");
        jsonBody.addProperty("resCd", "200");
        jsonBody.addProperty("asdsadasd", "qweqweweq");
        // jsonBody.add(Constant.DATA_BODY, jsonBody);

        JsonObject jsonObject = new JsonObject();
        jsonObject.add(Constant.DATA_BODY, jsonBody);

        System.out.println("jsonBody.toString() :: " + jsonObject.toString());
    }

}
