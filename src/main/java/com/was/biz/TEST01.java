package com.was.biz;


import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class TEST01 {

	public static void main(String[] args) {

        test_biz();
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

        System.out.println(element.getAsJsonObject().get("dataBody"));

        JsonObject jsonBody = (JsonObject) element.getAsJsonObject().get("dataBody");

        System.out.println(jsonBody.toString());

        if ( jsonBody.getAsJsonObject("cino") == null ) {
            // 에러발생
        }


        /*

        jsonBody.add();
        jsonBody = element.getAsJsonObject().get("dataBody");
        */

    }

}
