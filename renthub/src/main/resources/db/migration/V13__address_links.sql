-- Users and listings point to a settlement (FR-1, FR-4, D-48); listings get an optional postal code. Existing rows
-- are all in or around Lviv (start city): users get «м. Львів» (OSM node 26150791); listings get Lviv or the suburb
-- named at the start of their address, and lose that prefix because the address now holds the street and house only. city stays as a copy of the
-- settlement name for the public profile, catalogue cards and statistics E5.
ALTER TABLE users ADD COLUMN settlement_id BIGINT;
ALTER TABLE users ADD CONSTRAINT fk_users_settlement FOREIGN KEY (settlement_id) REFERENCES settlements (id);
UPDATE users SET settlement_id = (SELECT id FROM settlements WHERE osm_id = 26150791), city = 'Львів' WHERE role = 'USER';
ALTER TABLE users ADD CONSTRAINT ck_users_settlement CHECK (role = 'ADMIN' OR settlement_id IS NOT NULL);

ALTER TABLE equipment ADD COLUMN settlement_id BIGINT;
ALTER TABLE equipment ADD COLUMN postal_code VARCHAR(5);
-- Demo listings around Lviv name their town first («Винники, вул. …»): they get that settlement.
UPDATE equipment SET settlement_id = (SELECT id FROM settlements WHERE osm_id = 313247858), city = 'Винники',
    address = SUBSTRING(address FROM 10) WHERE address LIKE 'Винники, %';
UPDATE equipment SET settlement_id = (SELECT id FROM settlements WHERE osm_id = 337541358), city = 'Брюховичі',
    address = SUBSTRING(address FROM 12) WHERE address LIKE 'Брюховичі, %';
UPDATE equipment SET settlement_id = (SELECT id FROM settlements WHERE osm_id = 337547925), city = 'Пустомити',
    address = SUBSTRING(address FROM 12) WHERE address LIKE 'Пустомити, %';
UPDATE equipment SET settlement_id = (SELECT id FROM settlements WHERE osm_id = 261709042), city = 'Городок',
    address = SUBSTRING(address FROM 10) WHERE address LIKE 'Городок, %';
UPDATE equipment SET settlement_id = (SELECT id FROM settlements WHERE osm_id = 260954473), city = 'Жовква',
    address = SUBSTRING(address FROM 9) WHERE address LIKE 'Жовква, %';
UPDATE equipment SET settlement_id = (SELECT id FROM settlements WHERE osm_id = 26150791), city = 'Львів',
    address = CASE WHEN address LIKE 'Львів, %' THEN SUBSTRING(address FROM 8) ELSE address END
WHERE settlement_id IS NULL;
ALTER TABLE equipment ALTER COLUMN settlement_id SET NOT NULL;
ALTER TABLE equipment ADD CONSTRAINT fk_equipment_settlement FOREIGN KEY (settlement_id) REFERENCES settlements (id);
CREATE INDEX ix_equipment_settlement ON equipment (settlement_id);
