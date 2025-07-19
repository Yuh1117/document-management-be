CREATE EXTENSION IF NOT EXISTS vector;

CREATE TABLE document_search_index (
    id SERIAL PRIMARY KEY,
    keywords VARCHAR(255),
    created_at TIMESTAMP WITH TIME ZONE,
    updated_at TIMESTAMP WITH TIME ZONE
);

-- Columns
ALTER TABLE document_search_index
    ADD COLUMN IF NOT EXISTS content_vector vector(384),
    ADD COLUMN IF NOT EXISTS keywords_tsv tsvector;

-- Indexes
CREATE INDEX IF NOT EXISTS idx_content_vector_vector
    ON document_search_index
    USING ivfflat (content_vector vector_cosine_ops);

CREATE INDEX IF NOT EXISTS idx_keywords_tsv
    ON document_search_index
    USING GIN (keywords_tsv);

-- Function
CREATE OR REPLACE FUNCTION update_keywords_tsv() RETURNS trigger AS $$
BEGIN
  NEW.keywords_tsv := to_tsvector('simple', COALESCE(NEW.keywords, ''));
  RETURN NEW;
END
$$ LANGUAGE plpgsql;

-- Trigger
DROP TRIGGER IF EXISTS trg_keywords_tsv ON document_search_index;

CREATE TRIGGER trg_keywords_tsv
BEFORE INSERT OR UPDATE ON document_search_index
FOR EACH ROW EXECUTE FUNCTION update_keywords_tsv();
