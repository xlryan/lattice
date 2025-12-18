/*
 * Lattice/Lauter Database Schema - Future-Proof Version
 * Architecture: Multi-Tenant, Soft-Delete, Vector-Search Ready
 * * Version: 2.0 (Pro Ready)
 */

-- 1. Environment & Extensions
create schema if not exists lattice;
set search_path to lattice, public;

-- Enable Vector for AI
create extension if not exists vector;

-- ========================================================
-- 2. Base Index (Unified Search)
-- ========================================================
create table if not exists lattice_nodes (
                                             id            uuid primary key,
    -- Multi-Tenancy & Audit Columns
                                             tenant_id     varchar(64) not null default 'default', -- 租户ID (默认单机)
                                             created_by    varchar(64) not null default 'system',  -- 创建人 (支持多用户)
                                             is_deleted    boolean not null default false,         -- 软删除标记

    -- Core Data
                                             domain        text not null check (domain in ('CAREER','WEALTH','BUILD','KNOWLEDGE','INTEL','INBOX')),
                                             title         text not null,
                                             content       text not null,
                                             properties    jsonb not null default '{}',
                                             embedding     vector(1536),
                                             tags          text[] default '{}',

                                             created_at    timestamptz default now(),
                                             updated_at    timestamptz default now()
);

-- Enable RLS (Preparation for future security policies)
alter table lattice_nodes enable row level security;

-- Optimization Indexes
-- 1. Tenant Isolation Index (Crucial for SaaS performance)
create index if not exists idx_nodes_tenant on lattice_nodes (tenant_id, is_deleted);
-- 2. Business Indexes
create index if not exists idx_nodes_domain on lattice_nodes (domain, tags) where is_deleted = false;
create index if not exists idx_nodes_props_gin on lattice_nodes using gin (properties);
create index if not exists idx_nodes_props_cost on lattice_nodes ((properties->>'cost'));
-- 3. Vector Index (Partitioned by vector logic)
create index if not exists idx_nodes_embedding on lattice_nodes using ivfflat (embedding vector_cosine_ops) with (lists = 100);


-- ========================================================
-- 3. Domain Models (Pro Structure)
-- ========================================================

-- 3.1 Career (Resume, Experience)
create table if not exists lattice_career_nodes (
                                                    id              uuid primary key,
                                                    tenant_id       varchar(64) not null default 'default',
                                                    created_by      varchar(64) not null default 'system',
                                                    is_deleted      boolean not null default false,

                                                    type            text not null,
                                                    raw_content     text not null,
                                                    structured_data jsonb not null default '{}',
                                                    embedding       vector(1536) not null,
                                                    tags            text[] default '{}',
                                                    created_at      timestamptz default now()
);
alter table lattice_career_nodes enable row level security;

create index if not exists idx_career_tenant on lattice_career_nodes (tenant_id);
create index if not exists idx_career_nodes_type on lattice_career_nodes (type);
create index if not exists idx_career_nodes_structured on lattice_career_nodes using gin (structured_data);
create index if not exists idx_career_nodes_embedding on lattice_career_nodes using ivfflat (embedding vector_cosine_ops) with (lists = 100);


-- 3.2 Wealth (Finance, Assets)
create table if not exists lattice_wealth_entries (
                                                      id              uuid primary key,
                                                      tenant_id       varchar(64) not null default 'default',
                                                      created_by      varchar(64) not null default 'system',
                                                      is_deleted      boolean not null default false,

                                                      entry_type      text not null,
                                                      source_system   text,
                                                      raw_content     text not null,
                                                      structured_data jsonb not null default '{}',
                                                      amount          numeric(18,2) default 0,
                                                      currency        text default 'CNY',
                                                      occurred_on     date,
                                                      embedding       vector(1536),
                                                      tags            text[] default '{}',
                                                      created_at      timestamptz default now()
);
alter table lattice_wealth_entries enable row level security;

create index if not exists idx_wealth_tenant on lattice_wealth_entries (tenant_id, occurred_on);
create index if not exists idx_wealth_structured on lattice_wealth_entries using gin (structured_data);
create index if not exists idx_wealth_embedding on lattice_wealth_entries using ivfflat (embedding vector_cosine_ops) with (lists = 100);


-- 3.3 Build (DIY, Projects)
create table if not exists lattice_build_artifacts (
                                                       id          uuid primary key,
                                                       tenant_id   varchar(64) not null default 'default',
                                                       created_by  varchar(64) not null default 'system',
                                                       is_deleted  boolean not null default false,

                                                       category    text not null,
                                                       title       text not null,
                                                       description text,
                                                       attributes  jsonb not null default '{}',
                                                       embedding   vector(1536),
                                                       tags        text[] default '{}',
                                                       created_at  timestamptz default now()
);
alter table lattice_build_artifacts enable row level security;

create index if not exists idx_build_tenant on lattice_build_artifacts (tenant_id);
create index if not exists idx_build_embedding on lattice_build_artifacts using ivfflat (embedding vector_cosine_ops) with (lists = 100);


-- 3.4 Knowledge (Notes, RAG)
create table if not exists lattice_knowledge_notes (
                                                       id          uuid primary key,
                                                       tenant_id   varchar(64) not null default 'default',
                                                       created_by  varchar(64) not null default 'system',
                                                       is_deleted  boolean not null default false,

                                                       title       text not null,
                                                       content     text not null,
                                                       metadata    jsonb not null default '{}',
                                                       embedding   vector(1536),
                                                       tags        text[] default '{}',
                                                       created_at  timestamptz default now()
);
alter table lattice_knowledge_notes enable row level security;

create index if not exists idx_knowledge_tenant on lattice_knowledge_notes (tenant_id);
create index if not exists idx_knowledge_embedding on lattice_knowledge_notes using ivfflat (embedding vector_cosine_ops) with (lists = 100);


-- 3.5 Intel (Signals, News)
create table if not exists lattice_intel_signals (
                                                     id          uuid primary key,
                                                     tenant_id   varchar(64) not null default 'default',
                                                     created_by  varchar(64) not null default 'system',
                                                     is_deleted  boolean not null default false,

                                                     source      text not null,
                                                     headline    text not null,
                                                     raw_payload text,
                                                     insight     jsonb not null default '{}',
                                                     importance  text not null,
                                                     embedding   vector(1536),
                                                     tags        text[] default '{}',
                                                     captured_at timestamptz default now()
);
alter table lattice_intel_signals enable row level security;

create index if not exists idx_intel_tenant on lattice_intel_signals (tenant_id);
create index if not exists idx_intel_embedding on lattice_intel_signals using ivfflat (embedding vector_cosine_ops) with (lists = 100);


-- ========================================================
-- 4. Ingestion Staging
-- ========================================================
create table if not exists lattice_inbox_items (
                                                   id               uuid primary key,
                                                   tenant_id        varchar(64) not null default 'default',

                                                   payload          jsonb not null,
                                                   suggested_domain text,
                                                   status           text not null,
                                                   error_log        text, -- 新增：用于记录处理失败的原因
                                                   received_at      timestamptz default now()
);
alter table lattice_inbox_items enable row level security;

create index if not exists idx_inbox_processing on lattice_inbox_items (tenant_id, status);