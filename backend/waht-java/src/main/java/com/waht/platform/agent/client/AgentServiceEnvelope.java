package com.waht.platform.agent.client;

/**
 * Python Agent 服务响应的内部反序列化模型。
 */
record AgentServiceEnvelope<T>(int code, String message, T data) {
}
