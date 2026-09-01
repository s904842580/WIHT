class AppError(Exception):
    """可预期服务错误，交给 FastAPI 统一转换为 BaseResponse。"""

    def __init__(self, code: int, message: str, http_status: int | None = None) -> None:
        super().__init__(message)
        self.code = code
        self.message = message
        self.http_status = http_status or code


class NotFoundError(AppError):
    def __init__(self, message: str) -> None:
        super().__init__(404, message, 404)


class ConflictError(AppError):
    def __init__(self, message: str) -> None:
        super().__init__(409, message, 409)


class AgentUnavailableError(AppError):
    def __init__(self, message: str = "AI 学习助手暂时不可用") -> None:
        super().__init__(503, message, 503)
