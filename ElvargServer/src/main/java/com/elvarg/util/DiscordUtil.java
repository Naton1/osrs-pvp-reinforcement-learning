package com.elvarg.util;

import com.google.gson.Gson;
import okhttp3.*;

import java.io.IOException;

/**
 * Helper class for performing various Discord API functions.
 *
 * @author shogun <shogunrsps@gmail.com>
 */
public class DiscordUtil {

    private static class DiscordConstants {
        private static final String CLIENT_ID = "1010001099815669811";
        private static final String CLIENT_SECRET = "";
        private static final String TOKEN_ENDPOINT = "https://discord.com/api/oauth2/token";
        private static final String IDENTITY_ENDPOINT = "https://discord.com/api/v10/users/@me";
    }

    static OkHttpClient httpClient;

    public static class DiscordInfo {
        public String username, password;
    }

    private static class AccessTokenResponse {
        String access_token;
    }

    private static class UserResponse {
        String id;
        String username;
        String discriminator;
    }

    public static AccessTokenResponse getAccessToken(String code) throws IOException {
        RequestBody formBody = new FormBody.Builder()
                .add("client_id", DiscordConstants.CLIENT_ID)
                .add("client_secret", DiscordConstants.CLIENT_SECRET)
                .add("grant_type", "authorization_code")
                .add("code", code)
                .add("redirect_uri", "http://localhost:8080")
                .build();

        Request req = new Request.Builder()
                .url(DiscordConstants.TOKEN_ENDPOINT)
                .addHeader("Content-Type", "application/x-www-form-urlencoded")
                .post(formBody)
                .build();

        Response response = httpClient.newCall(req).execute();
        AccessTokenResponse resp = new Gson().fromJson(response.body().string(), AccessTokenResponse.class);

        return resp;
    }

    public static UserResponse getUserInfo(String token) throws IOException {
        Request req = new Request.Builder()
                .url(DiscordConstants.IDENTITY_ENDPOINT)
                .addHeader("Authorization", "Bearer " + token)
                .get()
                .build();

        Response response = httpClient.newCall(req).execute();
        UserResponse resp = new Gson().fromJson(response.body().string(), UserResponse.class);

        return resp;
    }

    public static DiscordInfo getDiscordInfoWithCode(String code) throws IOException {
        AccessTokenResponse token = getAccessToken(code);
        UserResponse userInfo = getUserInfo(token.access_token);

        DiscordInfo ret = new DiscordInfo();
        ret.username = userInfo.username + "_" + userInfo.discriminator;
        ret.password = userInfo.id;

        return ret;
    }

    static {
        httpClient = new OkHttpClient();
    }
}
