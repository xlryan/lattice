set search_path to lattice;

create table if not exists lattice_wealth_entries (
    id uuid primary key,
    entry_type text not null,
    source_system text,
    raw_content text not null,
    structured_data jsonb not null,
    amount numeric(18,2),
    currency text,
    occurred_on date,
    embedding vector(1536),
    tags text[] default '{}',
    created_at timestamptz default now()
);

create table if not exists lattice_build_artifacts (
    id uuid primary key,
    category text not null,
    title text not null,
    description text,
    attributes jsonb not null,
    embedding vector(1536),
    tags text[] default '{}',
    created_at timestamptz default now()
);

create table if not exists lattice_knowledge_notes (
    id uuid primary key,
    title text not null,
    content text not null,
    metadata jsonb not null,
    embedding vector(1536),
    tags text[] default '{}',
    created_at timestamptz default now()
);

create table if not exists lattice_intel_signals (
    id uuid primary key,
    source text not null,
    headline text not null,
    raw_payload text,
    insight jsonb not null,
    importance text not null,
    embedding vector(1536),
    tags text[] default '{}',
    captured_at timestamptz default now()
);

create table if not exists lattice_inbox_items (
    id uuid primary key,
    payload jsonb not null,
    suggested_domain text,
    status text not null,
    received_at timestamptz default now()
);

create index if not exists idx_wealth_structured on lattice_wealth_entries using gin (structured_data);
create index if not exists idx_wealth_embedding on lattice_wealth_entries using ivfflat (embedding vector_cosine_ops) with (lists = 100);
create index if not exists idx_build_embedding on lattice_build_artifacts using ivfflat (embedding vector_cosine_ops) with (lists = 100);
create index if not exists idx_knowledge_embedding on lattice_knowledge_notes using ivfflat (embedding vector_cosine_ops) with (lists = 100);
create index if not exists idx_intel_embedding on lattice_intel_signals using ivfflat (embedding vector_cosine_ops) with (lists = 100);
create index if not exists idx_inbox_status on lattice_inbox_items (status);
