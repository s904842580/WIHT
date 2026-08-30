package com.waht.platform.common.exception;

/**
 * 业务层可预期异常。
 *
 * <p>Service 在遇到参数冲突、资源不存在或权限问题时抛出该异常，
 * {@link GlobalExceptionHandler} 会将它转换成统一的接口响应。</p>
 */
public class ServiceException extends RuntimeException {

    private final int code;

    public ServiceException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.code = errorCode.getCode();
    }

    public ServiceException(ErrorCode errorCode, String message) {
        super(message);
        this.code = errorCode.getCode();
    }

    public int getCode() {
        return code;
    }
}
