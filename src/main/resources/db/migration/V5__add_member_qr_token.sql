ALTER TABLE members
    ADD COLUMN IF NOT EXISTS qr_token_hash VARCHAR(64),
    ADD COLUMN IF NOT EXISTS qr_token_created_at TIMESTAMPTZ;

CREATE UNIQUE INDEX IF NOT EXISTS uq_members_qr_token_hash
    ON members (qr_token_hash)
    WHERE qr_token_hash IS NOT NULL;