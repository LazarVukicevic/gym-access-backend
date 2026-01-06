CREATE TABLE IF NOT EXISTS entry_logs (
    id UUID PRIMARY KEY,

    member_id UUID NOT NULL,
    occurred_at TIMESTAMPTZ NOT NULL,

    result VARCHAR(20) NOT NULL,
    reason VARCHAR(100),
    gate_id VARCHAR(50),

    CONSTRAINT fk_entry_logs_member
            FOREIGN KEY (member_id)
            REFERENCES members(id)
            ON DELETE RESTRICT
);
