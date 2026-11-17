-- Every non-admin account can both rent and rent out (DECISIONS.md D-30): RENTER and OWNER merge into USER, and every
-- user has an account type (PRIVATE by default, COMPANY with name + ЄДРПОУ); the admin has none.
ALTER TABLE users DROP CONSTRAINT ck_users_owner_type_role;
ALTER TABLE users DROP CONSTRAINT ck_users_role;

UPDATE users SET role = 'USER' WHERE role IN ('RENTER', 'OWNER');
UPDATE users SET owner_type = 'PRIVATE' WHERE role = 'USER' AND owner_type IS NULL;
UPDATE users SET owner_type = NULL, company_name = NULL, edrpou = NULL WHERE role = 'ADMIN';

ALTER TABLE users ADD CONSTRAINT ck_users_role CHECK (role IN ('USER', 'ADMIN'));
ALTER TABLE users ADD CONSTRAINT ck_users_owner_type_role CHECK (
    (role = 'USER' AND owner_type IS NOT NULL) OR (role = 'ADMIN' AND owner_type IS NULL));
