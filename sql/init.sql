-- 初始化 grain_log 库，确保 pgvector/uuid 插件与 lattice schema 可用
CREATE EXTENSION IF NOT EXISTS vector;
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- 将业务对象隔离在 lattice schema，方便后续治理
DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.schemata WHERE schema_name = 'lattice'
    ) THEN
        EXECUTE 'CREATE SCHEMA lattice AUTHORIZATION grain_user';
    END IF;
END
$$;

ALTER DATABASE grain_log SET search_path TO lattice, public;
GRANT USAGE ON SCHEMA lattice TO grain_user;
