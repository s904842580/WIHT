from functools import lru_cache

from pydantic import Field
from pydantic_settings import BaseSettings, SettingsConfigDict


class Settings(BaseSettings):
    """Agent 服务配置；所有敏感字段都从环境变量读取。"""

    model_config = SettingsConfigDict(env_file=None, extra="ignore", populate_by_name=True)

    agent_host: str = Field(default="127.0.0.1", validation_alias="WAHT_AGENT_HOST")
    agent_port: int = Field(default=8000, validation_alias="WAHT_AGENT_PORT")
    database_url: str = Field(
        default="sqlite+pysqlite:///./data/waht-agent.db",
        validation_alias="WAHT_AGENT_DB_URL",
    )
    core_base_url: str = Field(default="http://127.0.0.1:8080", validation_alias="WAHT_CORE_BASE_URL")
    service_token: str = Field(
        default="waht-local-agent-service-token-change-me",
        validation_alias="WAHT_AGENT_SERVICE_TOKEN",
    )
    request_timeout_seconds: float = Field(
        default=20.0,
        gt=0,
        le=120,
        validation_alias="WAHT_AGENT_REQUEST_TIMEOUT_SECONDS",
    )
    ai_model: str = Field(default="", validation_alias="WAHT_AI_MODEL")
    openai_api_key: str = Field(default="", validation_alias="OPENAI_API_KEY")
    max_agent_turns: int = Field(default=6, ge=1, le=12, validation_alias="WAHT_AGENT_MAX_TURNS")


@lru_cache(maxsize=1)
def get_settings() -> Settings:
    return Settings()
