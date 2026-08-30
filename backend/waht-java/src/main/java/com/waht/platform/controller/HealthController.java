package com.waht.platform.controller;

import com.waht.platform.common.api.BaseResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 应用级健康检查接口，用于确认 Web 服务已经可以接收请求。
 */
@RestController
@RequestMapping("/api/health")
public class HealthController {

    @GetMapping
    public BaseResponse<Map<String, Object>> health() {
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("status", "UP");
        data.put("service", "waht-java");
        data.put("time", LocalDateTime.now());
        return BaseResponse.success(data);
    }
}
