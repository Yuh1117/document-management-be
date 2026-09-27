ALTER TABLE documents DROP CONSTRAINT documents_storage_type_check;

ALTER TABLE documents ADD CONSTRAINT documents_storage_type_check
    CHECK (storage_type::text = ANY (ARRAY['LOCAL','AWS_S3','CLOUDFLARE_R2','GCS']::text[]));
