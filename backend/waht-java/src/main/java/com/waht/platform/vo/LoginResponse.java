package com.waht.platform.vo;

public class LoginResponse {

    private String tokenType;
    private String token;
    private long expiresIn;
    private UserInfoResponse user;

    public LoginResponse(String tokenType, String token, long expiresIn, UserInfoResponse user) {
        this.tokenType = tokenType;
        this.token = token;
        this.expiresIn = expiresIn;
        this.user = user;
    }

    public String getTokenType() {
        return tokenType;
    }

    public void setTokenType(String tokenType) {
        this.tokenType = tokenType;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public long getExpiresIn() {
        return expiresIn;
    }

    public void setExpiresIn(long expiresIn) {
        this.expiresIn = expiresIn;
    }

    public UserInfoResponse getUser() {
        return user;
    }

    public void setUser(UserInfoResponse user) {
        this.user = user;
    }
}
