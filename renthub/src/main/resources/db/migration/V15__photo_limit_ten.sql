-- FR-4a (D-52): a listing may have up to 10 photos (positions 0-9) instead of 5.
ALTER TABLE equipment_photos DROP CONSTRAINT ck_equipment_photos_position;
ALTER TABLE equipment_photos ADD CONSTRAINT ck_equipment_photos_position CHECK (position BETWEEN 0 AND 9);
