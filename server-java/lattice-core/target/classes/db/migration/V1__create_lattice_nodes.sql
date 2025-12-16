create schema if not exists lattice;
set search_path to lattice;

create extension if not exists vector;

create table if not exists lattice_nodes (
    id            uuid primary key,
    domain        text not null check (domain in ('CAREER','DIY','MUSIC','LIFE','FINANCE')),
    title         text not null,
    content       text not null,
    properties    jsonb not null,
    embedding     vector(1536),
    tags          text[] default '{}',
    created_at    timestamptz default now(),
    updated_at    timestamptz default now()
);

create index if not exists idx_nodes_props_gin on lattice_nodes using gin (properties);
create index if not exists idx_nodes_props_cost on lattice_nodes ((properties->>'cost'));
create index if not exists idx_nodes_props_material on lattice_nodes ((properties #>> '{materials,0,name}'));
create index if not exists idx_nodes_domain on lattice_nodes (domain, tags);

create index if not exists idx_nodes_embedding
    on lattice_nodes using ivfflat (embedding vector_cosine_ops)
    with (lists = 100);
