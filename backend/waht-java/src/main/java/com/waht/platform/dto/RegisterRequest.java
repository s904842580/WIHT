package com.waht.platform.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * 注册接口请求参数，限制用户名字符可以避免 URL、日志和后续权限系统中的歧义。
 */
public class RegisterRequest {

    @NotBlank(message = "不能为空")
    @Size(min = 3, max = 50, message = "长度必须在 3 到 50 之间")
    @Pattern(regexp = "^[A-Za-z0-9_]+$", message = "只能包含字母、数字和下划线")
    private String username;

    @NotBlank(message = "不能为空")
    @Size(min = 6, max = 50, message = "长度必须在 6 到 50 之间")
    private String password;

    @Size(max = 50, message = "长度不能超过 50")
    private String nickname;

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }
}
