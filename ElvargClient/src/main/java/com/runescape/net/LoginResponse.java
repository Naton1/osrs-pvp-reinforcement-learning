package com.runescape.net;

/**
 * Represents a login response sent from the server.
 *
 * @author Professor Oak
 */
public class LoginResponse {

    private final int responseCode;

    public LoginResponse(int responseCode) {
        this.responseCode = responseCode;
    }

    public int getResponseCode() {
        return responseCode;
    }
}
