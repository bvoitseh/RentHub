-- FR-4a (D-52): one optional video per listing (MP4 or WebM, up to 50 MB) in the object storage; the database keeps
-- its storage key and content type only.
ALTER TABLE equipment ADD COLUMN video_key VARCHAR(200);
ALTER TABLE equipment ADD COLUMN video_content_type VARCHAR(20);
ALTER TABLE equipment ADD CONSTRAINT uq_equipment_video_key UNIQUE (video_key);
ALTER TABLE equipment ADD CONSTRAINT ck_equipment_video CHECK ((video_key IS NULL AND video_content_type IS NULL)
    OR (video_key IS NOT NULL AND video_content_type IS NOT NULL));
