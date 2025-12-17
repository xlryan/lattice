set search_path to lattice;

create table if not exists lattice_career_nodes (
    id uuid primary key,
    type text not null,
    raw_content text not null,
    structured_data jsonb not null,
    embedding vector(1536) not null,
    tags text[] default '{}',
    created_at timestamptz default now()
);

create index if not exists idx_career_nodes_type on lattice_career_nodes (type);
create index if not exists idx_career_nodes_tags on lattice_career_nodes using gin (tags);
create index if not exists idx_career_nodes_structured on lattice_career_nodes using gin (structured_data);
create index if not exists idx_career_nodes_embedding on lattice_career_nodes using ivfflat (embedding vector_cosine_ops) with (lists = 100);
