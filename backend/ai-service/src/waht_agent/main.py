from collections.abc import AsyncIterator
from contextlib import asynccontextmanager

from fastapi import FastAPI, Request
from fastapi.exceptions import RequestValidationError
from fastapi.responses import JSONResponse

from waht_agent.agent.runner import AgentRunner, OpenAIAgentRunner
from waht_agent.api.routes import router
from waht_agent.clients.waht_core import HttpWahtCoreClient, WahtCoreClient
from waht_agent.core.config import Settings, get_settings
from waht_agent.core.errors import AppError
from waht_agent.core.responses import failure, success
from waht_agent.persistence.database import Database
from waht_agent.persistence.repository import AgentRepository
from waht_agent.service import AgentService


def create_app(
    settings: Settings | None = None,
    runner: AgentRunner | None = None,
    core_client: WahtCoreClient | None = None,
) -> FastAPI:
    resolved_settings = settings or get_settings()
    database = Database(resolved_settings.database_url)
    resolved_core_client = core_client or HttpWahtCoreClient(
        resolved_settings.core_base_url,
        resolved_settings.service_token,
        resolved_settings.request_timeout_seconds,
    )
    resolved_runner = runner or OpenAIAgentRunner(resolved_settings, resolved_core_client)
    repository = AgentRepository(database)
    service = AgentService(resolved_settings, repository, resolved_runner, resolved_core_client)

    @asynccontextmanager
    async def lifespan(_: FastAPI) -> AsyncIterator[None]:
        database.create_schema()
        try:
            yield
        finally:
            await resolved_core_client.close()
            database.dispose()

    app = FastAPI(title="WAHT AI Agent Service", version="0.1.0", lifespan=lifespan)
    app.state.settings = resolved_settings
    app.state.agent_service = service
    app.include_router(router)

    @app.get("/health")
    def health() -> object:
        return success({"status": "UP", "modelConfigured": bool(resolved_settings.ai_model)})

    @app.exception_handler(AppError)
    async def handle_app_error(_: Request, exc: AppError) -> JSONResponse:
        payload = failure(exc.code, exc.message)
        return JSONResponse(status_code=exc.http_status, content=payload.model_dump(by_alias=True, mode="json"))

    @app.exception_handler(RequestValidationError)
    async def handle_validation_error(_: Request, exc: RequestValidationError) -> JSONResponse:
        payload = failure(400, "请求参数不合法")
        return JSONResponse(status_code=400, content=payload.model_dump(by_alias=True, mode="json"))

    @app.exception_handler(Exception)
    async def handle_unexpected_error(_: Request, exc: Exception) -> JSONResponse:
        payload = failure(500, "internal server error")
        return JSONResponse(status_code=500, content=payload.model_dump(by_alias=True, mode="json"))

    return app


app = create_app()
