package com.waht.platform.common.exception;

/**
 * 系统统一错误码，数值与常见 HTTP 状态保持一致，便于接口调用方判断。
 */
public enum ErrorCode {

    SUCCESS(0, "success"),
    BAD_REQUEST(400, "bad request"),
    UNAUTHORIZED(401, "unauthorized"),
    FORBIDDEN(403, "forbidden"),
    CONFLICT(409, "conflict"),
    NOT_FOUND(404, "not found"),
    SERVICE_UNAVAILABLE(503, "service unavailable"),
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
