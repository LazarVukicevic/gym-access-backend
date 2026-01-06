CREATE INDEX IF NOT EXISTS idx_entry_logs_occurred_at ON entry_logs (occurred_at DESC);
CREATE INDEX IF NOT EXISTS idx_entry_logs_member_id ON entry_logs (member_id);
CREATE INDEX IF NOT EXISTS idx_entry_logs_result ON entry_logs (result);