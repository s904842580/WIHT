from contextlib import contextmanager
from pathlib import Path
from typing import Iterator

from sqlalchemy import Engine, create_engine
from sqlalchemy.orm import Session, sessionmaker


class Database:
    """维护 Agent 自有数据库连接；不连接 Java 的 waht 业务表。"""

    def __init__(self, database_url: str) -> None:
        if database_url.startswith("sqlite"):
            self._prepare_sqlite_directory(database_url)
        connect_args = {"check_same_thread": False} if database_url.startswith("sqlite") else {}
        self.engine: Engine = create_engine(database_url, pool_pre_ping=True, connect_args=connect_args)
        self._session_factory = sessionmaker(bind=self.engine, expire_on_commit=False)

    def create_schema(self) -> None:
        from waht_agent.persistence.models import Base

        Base.metadata.create_all(self.engine)

    @contextmanager
    def session(self) -> Iterator[Session]:
        session = self._session_factory()
        try:
            yield session
            session.commit()
        except Exception:
            session.rollback()
            raise
        finally:
            session.close()

    def dispose(self) -> None:
        self.engine.dispose()

    @staticmethod
    def _prepare_sqlite_directory(database_url: str) -> None:
        prefix = "sqlite+pysqlite:///"
        if not database_url.startswith(prefix):
            return
        path_value = database_url.removeprefix(prefix)
        if path_value == ":memory:":
            return
        Path(path_value).parent.mkdir(parents=True, exist_ok=True)
