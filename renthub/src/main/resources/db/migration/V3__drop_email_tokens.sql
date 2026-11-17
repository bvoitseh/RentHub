-- E-mail link tokens moved to Redis with TTLs (DECISIONS.md D-27); the table and its index are no longer used.
DROP INDEX ix_email_tokens_user_type;
DROP TABLE email_tokens;
