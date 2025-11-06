-- -- V2__chat_message_sync_and_clientid.sql
-- ALTER TABLE messages
--   ADD COLUMN client_message_id UUID,
--   ADD COLUMN is_synced BOOLEAN DEFAULT FALSE;

-- ALTER TABLE messages
--   ADD COLUMN is_deleted BOOLEAN DEFAULT FALSE;

-- CREATE UNIQUE INDEX IF NOT EXISTS ux_messages_conversation_clientid
--   ON messages(conversation_id, client_message_id);

-- CREATE INDEX IF NOT EXISTS idx_messages_conversation_created_at
--   ON messages(conversation_id, created_at);
