package com.waht.platform.common.exception;

public enum ErrorCode {

    SUCCESS(0, "success"),
    BAD_REQUEST(400, "bad request"),
    UNAUTHORIZED(401, "unauthorized"),
    CONFLICT(409, "conflict"),
    NOT_FOUND(404, "not found"),
    BUSINESS_ERROR(1000, "business error"),
    INTERNAL_ERROR(500, "internal server error");

    private final int code;
    private final String message;

    ErrorCode(int code, String message) {
        this.code = code;
        this.message = message;
    }

    public int getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }
}
