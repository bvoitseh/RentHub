-- Chat presence (D-58): whether a user shows when they are online and when they were last seen. Presence itself is
-- kept in memory only; this column stores just the user's choice. Everyone shows it until they turn it off.
ALTER TABLE users ADD COLUMN show_online_status BOOLEAN NOT NULL DEFAULT TRUE;
