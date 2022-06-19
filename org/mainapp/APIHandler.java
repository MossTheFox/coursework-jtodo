package org.mainapp;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpTimeoutException;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

public class APIHandler {
    /**
     * 初始化用户登录 OAuth 凭据 (OAuth 逻辑在服务端会直接完成)
     * 该请求是同步的
     */
    static InitOAuthResponse initOAuth() {
        var request = HttpRequest.newBuilder()
                .uri(URI.create(GlobalConst.OAuthURL))
                .GET()
                .timeout(Duration.ofSeconds(5))
                .build();
        var client = HttpClient.newHttpClient();
        try {
            // 同步请求
            var response = client.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() != 200 || response.body() == null) {
                return new InitOAuthResponse("err", "服务器错误");
            }

            JSONObject json = new JSONObject(response.body());

            if (!json.getString("code").equals("ok")) {
                return new InitOAuthResponse("err", json.getString("message"));
            }

            return new InitOAuthResponse(json.getString("code"), json.getJSONObject("data"));

        } catch (Exception e) {
            System.out.println("initOAuth - 发生错误");
            e.printStackTrace();
            return new InitOAuthResponse("err", e.getMessage());
        }
    }

    /**
     * 登录授权、并拿取 Token 和用户信息
     * 该请求也是同步的
     */
    static AuthenticationPassedResponse finishAuthentication(String qqAuthObjectID) {
        var body = new JSONObject();
        body.append("qqAuth", qqAuthObjectID);

        var request = HttpRequest.newBuilder()
                .uri(URI.create(GlobalConst.finishAuthenticationURL))
                .header("Content-Type", "application/json;charset=utf-8")
                .POST(HttpRequest.BodyPublishers.ofString(body.toString()))
                .timeout(Duration.ofSeconds(5))
                .build();

        var client = HttpClient.newHttpClient();
        try {
            // 同步请求
            var response = client.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() != 200 || response.body() == null) {
                return new AuthenticationPassedResponse("err", "服务器响应异常");
            }

            // System.out.println(response.body());
            JSONObject json = new JSONObject(response.body());

            if (!json.getString("code").equals("ok")) {
                return new AuthenticationPassedResponse("err", json.getString("message"));
            }

            return new AuthenticationPassedResponse(json.getString("code"), json.getJSONObject("data"));

        } catch (Exception e) {
            System.out.println("finishAuthentication - 发生错误");
            e.printStackTrace();
            return new AuthenticationPassedResponse("err", e.getMessage());
        }
    }

    /**
     * 拿取初始信息
     * 同步
     *
     * @param token 用户的 Token，即完成验证后得到的 JWT Token
     */
    static GetFullDataResponse getFullData(String token) {
        var request = HttpRequest.newBuilder()
                .uri(URI.create(GlobalConst.initialGetDataUrl))
                .header("Authorization", "Bearer " + token)
                .GET()
                .timeout(Duration.ofSeconds(5))
                .build();
        var client = HttpClient.newHttpClient();
        try {
            // 同步请求
            var response = client.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() != 200 || response.body() == null) {
                return new GetFullDataResponse("err", "服务器响应异常");
            }
            System.out.println(response.body());
            var json = new JSONObject(response.body());
            if (!json.getString("code").equals("ok")) {
                return new GetFullDataResponse("err", json.getString("message"));
            }
            return new GetFullDataResponse(json.getString("code"), json.getJSONObject("data"));
        } catch (Exception e) {
            System.out.println("getFullData - 发生错误");
            e.printStackTrace();
            return new GetFullDataResponse("err", e.getMessage());
        }
    }

    /**
     * 同步修改
     * 这个是异步的，由主控类调用，回调函数也属于主控类
     */
    static CompletableFuture<Object> fireSyncData(String bearerToken, String requestBodyJSONString, Consumer<String> callback, Consumer<String> errorCallback) {
        var request = HttpRequest.newBuilder()
                .uri(URI.create(GlobalConst.syncUrl))
                .header("Content-Type", "application/json;charset=utf-8")
                .header("Authorization", "Bearer " + bearerToken)
                .method("PATCH", HttpRequest.BodyPublishers.ofString(requestBodyJSONString))
                .timeout(Duration.ofSeconds(5))
                .build();
        var client = HttpClient.newHttpClient();
        try {
            var promise = client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                    .thenApply((response) -> {
                        if (response.statusCode() != 200 || response.body() == null) {
                            errorCallback.accept("服务器响应异常");
                            return null;
                        }
                        var json = new JSONObject(response.body());
                        if (!json.getString("code").equals("ok")) {
                            errorCallback.accept(json.getString("message"));
                            return null;
                        }
                        callback.accept(json.getString("message"));
                        return null;
                    });
            return promise;
        } catch (Exception e) {
            System.out.println("syncData - 发生错误");
            e.printStackTrace();
            errorCallback.accept(e.getMessage());
        }
        return new CompletableFuture<>();
    }
}


