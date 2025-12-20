set search_path to lattice, public;

-- Drop existing indexes
DROP INDEX IF EXISTS idx_nodes_embedding;
DROP INDEX IF EXISTS idx_career_nodes_embedding;
DROP INDEX IF EXISTS idx_wealth_embedding;
DROP INDEX IF EXISTS idx_knowledge_embedding;
DROP INDEX IF EXISTS idx_intel_embedding;

-- Alter column dimensions
ALTER TABLE lattice_nodes ALTER COLUMN embedding TYPE vector(384);
ALTER TABLE lattice_career_nodes ALTER COLUMN embedding TYPE vector(384);
ALTER TABLE lattice_wealth_entries ALTER COLUMN embedding TYPE vector(384);
ALTER TABLE lattice_knowledge_notes ALTER COLUMN embedding TYPE vector(384);
ALTER TABLE lattice_intel_signals ALTER COLUMN embedding TYPE vector(384);

-- Recreate indexes
CREATE INDEX idx_nodes_embedding ON lattice_nodes USING ivfflat (embedding vector_cosine_ops) WITH (lists = 100);
CREATE INDEX idx_career_nodes_embedding ON lattice_career_nodes USING ivfflat (embedding vector_cosine_ops) WITH (lists = 100);
CREATE INDEX idx_wealth_embedding ON lattice_wealth_entries USING ivfflat (embedding vector_cosine_ops) WITH (lists = 100);
CREATE INDEX idx_knowledge_embedding ON lattice_knowledge_notes USING ivfflat (embedding vector_cosine_ops) WITH (lists = 100);
CREATE INDEX idx_intel_embedding ON lattice_intel_signals USING ivfflat (embedding vector_cosine_ops) WITH (lists = 100);
