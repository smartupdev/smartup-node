package global.smartup.node.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import global.smartup.node.eth.EthUtil;
import global.smartup.node.util.MapBuilder;
import okhttp3.*;
import org.junit.Test;

import java.io.IOException;
import java.util.Map;

public class OkHttpUtil {

    private static OkHttpClient client = new OkHttpClient();

    public static final String BaseUrl = "http://127.0.0.1:8086";

    public static final String UserAddress = "0xB44940Be0eeA81a3D0dA22CC15208AF4744BeA8E";

    public static final String UserPrivateKey = "988a93ab9c8e83dba7093cd4bc7b7c2aa075a7421c2313154a329100395c5c9d";

    public static String Token = "";


    public static String post(String api, Map<String, String> param) {
        System.out.println("url: " + BaseUrl + api);
        System.out.println("param: ");
        StringBuffer content = new StringBuffer("");
        if (param != null) {
            param.entrySet().stream().forEach(e -> {
                System.out.println("  " + e.getKey() + "=" + e.getValue());
                content.append(e.getKey() + "=" + e.getValue() + "&");
            });
        }

        RequestBody body = RequestBody.create(MediaType.parse("application/x-www-form-urlencoded"), content.toString());
        Request request = new Request.Builder().url(BaseUrl + api).post(body).header("token", Token).build();
        try {
            Response response = client.newCall(request).execute();
            String respStr = response.body().string();
            System.out.println("response: " + respStr);
            return respStr;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static String postWithLogin(String api, Map<String, String> param) {
        login();
        return post(api, param);
    }

    public static String postJson(String api, Map<String, Object> param) {
        System.out.println("url: " + BaseUrl + api);
        System.out.println("param: ");
        if (param != null) {
            param.entrySet().stream().forEach(e -> {
                System.out.println("  " + e.getKey() + "=" + e.getValue());
            });
        }
        System.out.println("json: " + JSON.toJSONString(param));

        RequestBody body = RequestBody.create(MediaType.parse("application/json"), JSON.toJSONString(param));
        Request request = new Request.Builder().url(BaseUrl + api).post(body).header("token", Token).build();
        try {
            Response response = client.newCall(request).execute();
            String respStr = response.body().string();
            System.out.println("response: " + respStr);
            return respStr;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static String postJsonWithLogin(String api, Map<String, Object> param) {
        login();
        return postJson(api, param);
    }

    private static void login() {
        String loginData = post("/api/login", MapBuilder.<String, String>create().put("address", UserAddress).build());
        JSONObject jo = JSON.parseObject(loginData);
        String code = jo.getString("obj");
        String sign = EthUtil.sign(UserPrivateKey, code, true);
        String authData = post("/api/auth", MapBuilder.<String, String>create().put("address", UserAddress).put("signature", sign).build());
        JSONObject authObj = JSON.parseObject(authData);
        Token = authObj.getJSONObject("obj").getString("token");
    }

    @Test
    public void test() {
        // postJson("/", MapBuilder.<String, Object>create().put("aa", "ss").put("zz", 12.2).build());
        // login();
    }

}
