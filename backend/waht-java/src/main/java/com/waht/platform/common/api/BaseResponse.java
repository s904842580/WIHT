package com.waht.platform.common.api;

import com.waht.platform.common.exception.ErrorCode;

import java.time.LocalDateTime;

/**
 * 所有 REST 接口的统一响应包装。
 *
 * <p>控制器只返回业务数据，成功码、提示信息和响应时间由本类统一维护，
 * 这样前端可以使用同一套解析与错误处理逻辑。</p>
 */
public class BaseResponse<T> {

    private final int code;
    private final String message;
    private final T data;
    private final LocalDateTime timestamp;

    private BaseResponse(int code, String message, T data) {
        this.code = code;
        this.message = message;
        this.data = data;
        this.timestamp = LocalDateTime.now();
    }

    public static BaseResponse<Void> success() {
        return new BaseResponse<>(ErrorCode.SUCCESS.getCode(), ErrorCode.SUCCESS.getMessage(), null);
    }

    public static <T> BaseResponse<T> success(T data) {
        return new BaseResponse<>(ErrorCode.SUCCESS.getCode(), ErrorCode.SUCCESS.getMessage(), data);
    }

    public static <T> BaseResponse<T> fail(ErrorCode errorCode) {
        return new BaseResponse<>(errorCode.getCode(), errorCode.getMessage(), null);
    }

    public static <T> BaseResponse<T> fail(int code, String message) {
        return new BaseResponse<>(code, message, null);
    }

    public int getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }

    public T getData() {
        return data;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }
}
