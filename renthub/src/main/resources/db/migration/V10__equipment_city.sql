-- FR-23 E5: the city of a listing, filled from the geocoding result and editable by the owner (DECISIONS.md D-46).
-- Every listing so far is in the start city, Lviv.
ALTER TABLE equipment ADD COLUMN city VARCHAR(100);
UPDATE equipment SET city = 'Львів';
ALTER TABLE equipment ALTER COLUMN city SET NOT NULL;
