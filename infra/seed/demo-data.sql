-- RentHub demo data: 6 owners and 150 listings around Lviv with placeholder photos (CLAUDE.md 2.4).
-- NOT a Flyway migration: run it by hand, once, against the local H2 database (see README, "Demo data").
-- Safe to re-run: every row is inserted only if it does not exist yet. All demo accounts use the password
-- Password123 and e-mails at example.com (never receive mail). The photo files are uploaded to the bucket by the
-- docker-compose job s3-init (infra/seed/photos -> renthub-photos/seed/).

-- Owners: verified users; four private persons and two companies (FR-1a).
INSERT INTO users (name, email, email_verified, phone, password_hash, role, city, geo_consent, blocked, owner_type, company_name, edrpou, created_at, settlement_id)
SELECT 'Олена Коваль', 'seed.olena@example.com', TRUE, '+380671110001', '$2a$10$e4ibsmwuaUjO6h6V8CTFF.5hCEb.IP44KPTqrnyWG9SfReKi2X.GG', 'USER', 'Львів', FALSE, FALSE, 'PRIVATE', NULL, NULL, CURRENT_TIMESTAMP - INTERVAL '90' DAY, (SELECT id FROM settlements WHERE osm_id = 26150791)
WHERE NOT EXISTS (SELECT 1 FROM users WHERE email = 'seed.olena@example.com');
INSERT INTO users (name, email, email_verified, phone, password_hash, role, city, geo_consent, blocked, owner_type, company_name, edrpou, created_at, settlement_id)
SELECT 'Андрій Шевчук', 'seed.andrii@example.com', TRUE, '+380671110002', '$2a$10$e4ibsmwuaUjO6h6V8CTFF.5hCEb.IP44KPTqrnyWG9SfReKi2X.GG', 'USER', 'Львів', FALSE, FALSE, 'PRIVATE', NULL, NULL, CURRENT_TIMESTAMP - INTERVAL '90' DAY, (SELECT id FROM settlements WHERE osm_id = 26150791)
WHERE NOT EXISTS (SELECT 1 FROM users WHERE email = 'seed.andrii@example.com');
INSERT INTO users (name, email, email_verified, phone, password_hash, role, city, geo_consent, blocked, owner_type, company_name, edrpou, created_at, settlement_id)
SELECT 'Марта Бойко', 'seed.marta@example.com', TRUE, '+380671110003', '$2a$10$e4ibsmwuaUjO6h6V8CTFF.5hCEb.IP44KPTqrnyWG9SfReKi2X.GG', 'USER', 'Львів', FALSE, FALSE, 'PRIVATE', NULL, NULL, CURRENT_TIMESTAMP - INTERVAL '90' DAY, (SELECT id FROM settlements WHERE osm_id = 26150791)
WHERE NOT EXISTS (SELECT 1 FROM users WHERE email = 'seed.marta@example.com');
INSERT INTO users (name, email, email_verified, phone, password_hash, role, city, geo_consent, blocked, owner_type, company_name, edrpou, created_at, settlement_id)
SELECT 'Тарас Гнатюк', 'seed.taras@example.com', TRUE, '+380671110004', '$2a$10$e4ibsmwuaUjO6h6V8CTFF.5hCEb.IP44KPTqrnyWG9SfReKi2X.GG', 'USER', 'Львів', FALSE, FALSE, 'PRIVATE', NULL, NULL, CURRENT_TIMESTAMP - INTERVAL '90' DAY, (SELECT id FROM settlements WHERE osm_id = 26150791)
WHERE NOT EXISTS (SELECT 1 FROM users WHERE email = 'seed.taras@example.com');
INSERT INTO users (name, email, email_verified, phone, password_hash, role, city, geo_consent, blocked, owner_type, company_name, edrpou, created_at, settlement_id)
SELECT 'Ігор Мельник', 'seed.budprokat@example.com', TRUE, '+380671110005', '$2a$10$e4ibsmwuaUjO6h6V8CTFF.5hCEb.IP44KPTqrnyWG9SfReKi2X.GG', 'USER', 'Львів', FALSE, FALSE, 'COMPANY', 'ТОВ «Будпрокат»', '31234567', CURRENT_TIMESTAMP - INTERVAL '90' DAY, (SELECT id FROM settlements WHERE osm_id = 26150791)
WHERE NOT EXISTS (SELECT 1 FROM users WHERE email = 'seed.budprokat@example.com');
INSERT INTO users (name, email, email_verified, phone, password_hash, role, city, geo_consent, blocked, owner_type, company_name, edrpou, created_at, settlement_id)
SELECT 'Світлана Лис', 'seed.fotozona@example.com', TRUE, '+380671110006', '$2a$10$e4ibsmwuaUjO6h6V8CTFF.5hCEb.IP44KPTqrnyWG9SfReKi2X.GG', 'USER', 'Львів', FALSE, FALSE, 'COMPANY', 'ТОВ «Фотозона Львів»', '41234568', CURRENT_TIMESTAMP - INTERVAL '90' DAY, (SELECT id FROM settlements WHERE osm_id = 26150791)
WHERE NOT EXISTS (SELECT 1 FROM users WHERE email = 'seed.fotozona@example.com');

-- Listings: one cover photo each (every fifth listing has two). A listing is skipped when its photo exists.

-- 001. Перфоратор Bosch GBH 2-26
INSERT INTO equipment (owner_id, category_id, title, description, item_condition, price_per_day, quantity, min_days, max_days, pickup_available, delivery_available, delivery_note, address, city, lat, lon, status, created_at, updated_at, settlement_id)
SELECT u.id, c.id, 'Перфоратор Bosch GBH 2-26', 'Підходить для домашніх і професійних робіт. Видаю з кейсом і комплектом аксесуарів.', 'EXCELLENT', 190.00, 1, 2, 7, FALSE, TRUE, 'Доставка по Львову та області, ціну узгодимо', 'вул. Наукова, 92', 'Львів', 49.847343, 24.013822, 'ACTIVE', CURRENT_TIMESTAMP - INTERVAL '13' DAY, CURRENT_TIMESTAMP - INTERVAL '13' DAY, (SELECT id FROM settlements WHERE osm_id = 26150791)
FROM users u, categories c WHERE u.email = 'seed.taras@example.com' AND c.name = 'Електроінструмент'
AND NOT EXISTS (SELECT 1 FROM equipment_photos WHERE storage_key = 'seed/equipment-001.png');
INSERT INTO equipment_photos (equipment_id, storage_key, position, created_at)
SELECT MAX(e.id), 'seed/equipment-001.png', 0, CURRENT_TIMESTAMP FROM equipment e JOIN users u ON u.id = e.owner_id
WHERE e.title = 'Перфоратор Bosch GBH 2-26' AND u.email = 'seed.taras@example.com'
AND NOT EXISTS (SELECT 1 FROM equipment_photos WHERE storage_key = 'seed/equipment-001.png')
HAVING MAX(e.id) IS NOT NULL;

-- 002. Бетонозмішувач 180 л
INSERT INTO equipment (owner_id, category_id, title, description, item_condition, price_per_day, quantity, min_days, max_days, pickup_available, delivery_available, delivery_note, address, city, lat, lon, status, created_at, updated_at, settlement_id)
SELECT u.id, c.id, 'Бетонозмішувач 180 л', 'Акуратне використання, без пошкоджень. Перед видачею чищу й перевіряю.', 'GOOD', 330.00, 1, 1, 14, TRUE, FALSE, NULL, 'вул. Володимира Великого, 39', 'Львів', 49.871405, 23.981643, 'ACTIVE', CURRENT_TIMESTAMP - INTERVAL '23' DAY, CURRENT_TIMESTAMP - INTERVAL '23' DAY, (SELECT id FROM settlements WHERE osm_id = 26150791)
FROM users u, categories c WHERE u.email = 'seed.budprokat@example.com' AND c.name = 'Будівельне обладнання'
AND NOT EXISTS (SELECT 1 FROM equipment_photos WHERE storage_key = 'seed/equipment-002.png');
INSERT INTO equipment_photos (equipment_id, storage_key, position, created_at)
SELECT MAX(e.id), 'seed/equipment-002.png', 0, CURRENT_TIMESTAMP FROM equipment e JOIN users u ON u.id = e.owner_id
WHERE e.title = 'Бетонозмішувач 180 л' AND u.email = 'seed.budprokat@example.com'
AND NOT EXISTS (SELECT 1 FROM equipment_photos WHERE storage_key = 'seed/equipment-002.png')
HAVING MAX(e.id) IS NOT NULL;

-- 003. Драбина алюмінієва 3×9 щаблів
INSERT INTO equipment (owner_id, category_id, title, description, item_condition, price_per_day, quantity, min_days, max_days, pickup_available, delivery_available, delivery_note, address, city, lat, lon, status, created_at, updated_at, settlement_id)
SELECT u.id, c.id, 'Драбина алюмінієва 3×9 щаблів', 'Підходить для домашніх і професійних робіт. Видаю з кейсом і комплектом аксесуарів.', 'GOOD', 310.00, 1, 2, NULL, TRUE, TRUE, 'Доставка по Львову та області, ціну узгодимо', 'вул. Княгині Ольги, 31', 'Львів', 49.827874, 24.068343, 'ACTIVE', CURRENT_TIMESTAMP - INTERVAL '53' DAY, CURRENT_TIMESTAMP - INTERVAL '53' DAY, (SELECT id FROM settlements WHERE osm_id = 26150791)
FROM users u, categories c WHERE u.email = 'seed.marta@example.com' AND c.name = 'Риштування та драбини'
AND NOT EXISTS (SELECT 1 FROM equipment_photos WHERE storage_key = 'seed/equipment-003.png');
INSERT INTO equipment_photos (equipment_id, storage_key, position, created_at)
SELECT MAX(e.id), 'seed/equipment-003.png', 0, CURRENT_TIMESTAMP FROM equipment e JOIN users u ON u.id = e.owner_id
WHERE e.title = 'Драбина алюмінієва 3×9 щаблів' AND u.email = 'seed.marta@example.com'
AND NOT EXISTS (SELECT 1 FROM equipment_photos WHERE storage_key = 'seed/equipment-003.png')
HAVING MAX(e.id) IS NOT NULL;

-- 004. Газонокосарка Husqvarna LC 140
INSERT INTO equipment (owner_id, category_id, title, description, item_condition, price_per_day, quantity, min_days, max_days, pickup_available, delivery_available, delivery_note, address, city, lat, lon, status, created_at, updated_at, settlement_id)
SELECT u.id, c.id, 'Газонокосарка Husqvarna LC 140', 'У хорошому стані, регулярно обслуговується. Можу підказати, як найкраще використати.', 'GOOD', 570.00, 3, 2, NULL, TRUE, FALSE, NULL, 'вул. Виговського, 1', 'Львів', 49.883471, 23.997884, 'ACTIVE', CURRENT_TIMESTAMP - INTERVAL '6' DAY, CURRENT_TIMESTAMP - INTERVAL '6' DAY, (SELECT id FROM settlements WHERE osm_id = 26150791)
FROM users u, categories c WHERE u.email = 'seed.taras@example.com' AND c.name = 'Садова техніка'
AND NOT EXISTS (SELECT 1 FROM equipment_photos WHERE storage_key = 'seed/equipment-004.png');
INSERT INTO equipment_photos (equipment_id, storage_key, position, created_at)
SELECT MAX(e.id), 'seed/equipment-004.png', 0, CURRENT_TIMESTAMP FROM equipment e JOIN users u ON u.id = e.owner_id
WHERE e.title = 'Газонокосарка Husqvarna LC 140' AND u.email = 'seed.taras@example.com'
AND NOT EXISTS (SELECT 1 FROM equipment_photos WHERE storage_key = 'seed/equipment-004.png')
HAVING MAX(e.id) IS NOT NULL;

-- 005. Снігоприбирач бензиновий Honda HSS 655
INSERT INTO equipment (owner_id, category_id, title, description, item_condition, price_per_day, quantity, min_days, max_days, pickup_available, delivery_available, delivery_note, address, city, lat, lon, status, created_at, updated_at, settlement_id)
SELECT u.id, c.id, 'Снігоприбирач бензиновий Honda HSS 655', 'Справний, перевірений перед кожною орендою. Видаю з інструкцією та всім потрібним для роботи.', 'EXCELLENT', 750.00, 1, 1, NULL, TRUE, FALSE, NULL, 'вул. Бандери, 37', 'Львів', 49.862360, 23.968492, 'ACTIVE', CURRENT_TIMESTAMP - INTERVAL '16' DAY, CURRENT_TIMESTAMP - INTERVAL '16' DAY, (SELECT id FROM settlements WHERE osm_id = 26150791)
FROM users u, categories c WHERE u.email = 'seed.andrii@example.com' AND c.name = 'Снігоприбиральна техніка'
AND NOT EXISTS (SELECT 1 FROM equipment_photos WHERE storage_key = 'seed/equipment-005.png');
INSERT INTO equipment_photos (equipment_id, storage_key, position, created_at)
SELECT MAX(e.id), 'seed/equipment-005.png', 0, CURRENT_TIMESTAMP FROM equipment e JOIN users u ON u.id = e.owner_id
WHERE e.title = 'Снігоприбирач бензиновий Honda HSS 655' AND u.email = 'seed.andrii@example.com'
AND NOT EXISTS (SELECT 1 FROM equipment_photos WHERE storage_key = 'seed/equipment-005.png')
HAVING MAX(e.id) IS NOT NULL;
INSERT INTO equipment_photos (equipment_id, storage_key, position, created_at)
SELECT MAX(e.id), 'seed/equipment-005-2.png', 1, CURRENT_TIMESTAMP FROM equipment e JOIN users u ON u.id = e.owner_id
WHERE e.title = 'Снігоприбирач бензиновий Honda HSS 655' AND u.email = 'seed.andrii@example.com'
AND NOT EXISTS (SELECT 1 FROM equipment_photos WHERE storage_key = 'seed/equipment-005-2.png')
HAVING MAX(e.id) IS NOT NULL;

-- 006. Мийка високого тиску Kärcher K5
INSERT INTO equipment (owner_id, category_id, title, description, item_condition, price_per_day, quantity, min_days, max_days, pickup_available, delivery_available, delivery_note, address, city, lat, lon, status, created_at, updated_at, settlement_id)
SELECT u.id, c.id, 'Мийка високого тиску Kärcher K5', 'Справний, перевірений перед кожною орендою. Видаю з інструкцією та всім потрібним для роботи.', 'NEW', 750.00, 1, 1, 14, TRUE, FALSE, NULL, 'просп. Свободи, 8', 'Львів', 49.810451, 23.961604, 'ACTIVE', CURRENT_TIMESTAMP - INTERVAL '8' DAY, CURRENT_TIMESTAMP - INTERVAL '8' DAY, (SELECT id FROM settlements WHERE osm_id = 26150791)
FROM users u, categories c WHERE u.email = 'seed.olena@example.com' AND c.name = 'Клінінгове обладнання'
AND NOT EXISTS (SELECT 1 FROM equipment_photos WHERE storage_key = 'seed/equipment-006.png');
INSERT INTO equipment_photos (equipment_id, storage_key, position, created_at)
SELECT MAX(e.id), 'seed/equipment-006.png', 0, CURRENT_TIMESTAMP FROM equipment e JOIN users u ON u.id = e.owner_id
WHERE e.title = 'Мийка високого тиску Kärcher K5' AND u.email = 'seed.olena@example.com'
AND NOT EXISTS (SELECT 1 FROM equipment_photos WHERE storage_key = 'seed/equipment-006.png')
HAVING MAX(e.id) IS NOT NULL;

-- 007. Фотоапарат Sony A7 III
INSERT INTO equipment (owner_id, category_id, title, description, item_condition, price_per_day, quantity, min_days, max_days, pickup_available, delivery_available, delivery_note, address, city, lat, lon, status, created_at, updated_at, settlement_id)
SELECT u.id, c.id, 'Фотоапарат Sony A7 III', 'Акуратне використання, без пошкоджень. Перед видачею чищу й перевіряю.', 'EXCELLENT', 950.00, 5, 2, 14, FALSE, TRUE, NULL, 'вул. Городоцька, 62', 'Львів', 49.795451, 23.982952, 'ACTIVE', CURRENT_TIMESTAMP - INTERVAL '35' DAY, CURRENT_TIMESTAMP - INTERVAL '35' DAY, (SELECT id FROM settlements WHERE osm_id = 26150791)
FROM users u, categories c WHERE u.email = 'seed.marta@example.com' AND c.name = 'Фото- та відеотехніка'
AND NOT EXISTS (SELECT 1 FROM equipment_photos WHERE storage_key = 'seed/equipment-007.png');
INSERT INTO equipment_photos (equipment_id, storage_key, position, created_at)
SELECT MAX(e.id), 'seed/equipment-007.png', 0, CURRENT_TIMESTAMP FROM equipment e JOIN users u ON u.id = e.owner_id
WHERE e.title = 'Фотоапарат Sony A7 III' AND u.email = 'seed.marta@example.com'
AND NOT EXISTS (SELECT 1 FROM equipment_photos WHERE storage_key = 'seed/equipment-007.png')
HAVING MAX(e.id) IS NOT NULL;

-- 008. Колонка JBL PartyBox 310
INSERT INTO equipment (owner_id, category_id, title, description, item_condition, price_per_day, quantity, min_days, max_days, pickup_available, delivery_available, delivery_note, address, city, lat, lon, status, created_at, updated_at, settlement_id)
SELECT u.id, c.id, 'Колонка JBL PartyBox 310', 'Справний, перевірений перед кожною орендою. Видаю з інструкцією та всім потрібним для роботи.', 'FAIR', 790.00, 1, 1, 30, TRUE, TRUE, 'Доставка по Львову та області, ціну узгодимо', 'вул. Центральна, 56', 'Винники', 49.816680, 24.120750, 'ACTIVE', CURRENT_TIMESTAMP - INTERVAL '10' DAY, CURRENT_TIMESTAMP - INTERVAL '10' DAY, (SELECT id FROM settlements WHERE osm_id = 313247858)
FROM users u, categories c WHERE u.email = 'seed.fotozona@example.com' AND c.name = 'Світло та звук'
AND NOT EXISTS (SELECT 1 FROM equipment_photos WHERE storage_key = 'seed/equipment-008.png');
INSERT INTO equipment_photos (equipment_id, storage_key, position, created_at)
SELECT MAX(e.id), 'seed/equipment-008.png', 0, CURRENT_TIMESTAMP FROM equipment e JOIN users u ON u.id = e.owner_id
WHERE e.title = 'Колонка JBL PartyBox 310' AND u.email = 'seed.fotozona@example.com'
AND NOT EXISTS (SELECT 1 FROM equipment_photos WHERE storage_key = 'seed/equipment-008.png')
HAVING MAX(e.id) IS NOT NULL;

-- 009. Намет 4-місний Tramp
INSERT INTO equipment (owner_id, category_id, title, description, item_condition, price_per_day, quantity, min_days, max_days, pickup_available, delivery_available, delivery_note, address, city, lat, lon, status, created_at, updated_at, settlement_id)
SELECT u.id, c.id, 'Намет 4-місний Tramp', 'Підходить для домашніх і професійних робіт. Видаю з кейсом і комплектом аксесуарів.', 'EXCELLENT', 210.00, 1, 3, 30, TRUE, FALSE, NULL, 'вул. Володимира Великого, 45', 'Львів', 49.839059, 24.018526, 'ACTIVE', CURRENT_TIMESTAMP - INTERVAL '50' DAY, CURRENT_TIMESTAMP - INTERVAL '50' DAY, (SELECT id FROM settlements WHERE osm_id = 26150791)
FROM users u, categories c WHERE u.email = 'seed.marta@example.com' AND c.name = 'Туристичне спорядження'
AND NOT EXISTS (SELECT 1 FROM equipment_photos WHERE storage_key = 'seed/equipment-009.png');
INSERT INTO equipment_photos (equipment_id, storage_key, position, created_at)
SELECT MAX(e.id), 'seed/equipment-009.png', 0, CURRENT_TIMESTAMP FROM equipment e JOIN users u ON u.id = e.owner_id
WHERE e.title = 'Намет 4-місний Tramp' AND u.email = 'seed.marta@example.com'
AND NOT EXISTS (SELECT 1 FROM equipment_photos WHERE storage_key = 'seed/equipment-009.png')
HAVING MAX(e.id) IS NOT NULL;

-- 010. Мініекскаватор 1,5 т
INSERT INTO equipment (owner_id, category_id, title, description, item_condition, price_per_day, quantity, min_days, max_days, pickup_available, delivery_available, delivery_note, address, city, lat, lon, status, created_at, updated_at, settlement_id)
SELECT u.id, c.id, 'Мініекскаватор 1,5 т', 'У хорошому стані, регулярно обслуговується. Можу підказати, як найкраще використати.', 'GOOD', 3450.00, 1, 1, NULL, FALSE, TRUE, NULL, 'вул. Володимира Великого, 45', 'Львів', 49.816107, 24.057828, 'ACTIVE', CURRENT_TIMESTAMP - INTERVAL '13' DAY, CURRENT_TIMESTAMP - INTERVAL '13' DAY, (SELECT id FROM settlements WHERE osm_id = 26150791)
FROM users u, categories c WHERE u.email = 'seed.budprokat@example.com' AND c.name = 'Спецтехніка'
AND NOT EXISTS (SELECT 1 FROM equipment_photos WHERE storage_key = 'seed/equipment-010.png');
INSERT INTO equipment_photos (equipment_id, storage_key, position, created_at)
SELECT MAX(e.id), 'seed/equipment-010.png', 0, CURRENT_TIMESTAMP FROM equipment e JOIN users u ON u.id = e.owner_id
WHERE e.title = 'Мініекскаватор 1,5 т' AND u.email = 'seed.budprokat@example.com'
AND NOT EXISTS (SELECT 1 FROM equipment_photos WHERE storage_key = 'seed/equipment-010.png')
HAVING MAX(e.id) IS NOT NULL;
INSERT INTO equipment_photos (equipment_id, storage_key, position, created_at)
SELECT MAX(e.id), 'seed/equipment-010-2.png', 1, CURRENT_TIMESTAMP FROM equipment e JOIN users u ON u.id = e.owner_id
WHERE e.title = 'Мініекскаватор 1,5 т' AND u.email = 'seed.budprokat@example.com'
AND NOT EXISTS (SELECT 1 FROM equipment_photos WHERE storage_key = 'seed/equipment-010-2.png')
HAVING MAX(e.id) IS NOT NULL;

-- 011. Шуруповерт Makita DF333
INSERT INTO equipment (owner_id, category_id, title, description, item_condition, price_per_day, quantity, min_days, max_days, pickup_available, delivery_available, delivery_note, address, city, lat, lon, status, created_at, updated_at, settlement_id)
SELECT u.id, c.id, 'Шуруповерт Makita DF333', 'Акуратне використання, без пошкоджень. Перед видачею чищу й перевіряю.', 'GOOD', 400.00, 3, 1, 7, TRUE, TRUE, 'Доставка по Львову, вартість за домовленістю', 'вул. Городоцька, 91', 'Львів', 49.811204, 24.045190, 'ACTIVE', CURRENT_TIMESTAMP - INTERVAL '35' DAY, CURRENT_TIMESTAMP - INTERVAL '35' DAY, (SELECT id FROM settlements WHERE osm_id = 26150791)
FROM users u, categories c WHERE u.email = 'seed.marta@example.com' AND c.name = 'Електроінструмент'
AND NOT EXISTS (SELECT 1 FROM equipment_photos WHERE storage_key = 'seed/equipment-011.png');
INSERT INTO equipment_photos (equipment_id, storage_key, position, created_at)
SELECT MAX(e.id), 'seed/equipment-011.png', 0, CURRENT_TIMESTAMP FROM equipment e JOIN users u ON u.id = e.owner_id
WHERE e.title = 'Шуруповерт Makita DF333' AND u.email = 'seed.marta@example.com'
AND NOT EXISTS (SELECT 1 FROM equipment_photos WHERE storage_key = 'seed/equipment-011.png')
HAVING MAX(e.id) IS NOT NULL;

-- 012. Віброплита 90 кг
INSERT INTO equipment (owner_id, category_id, title, description, item_condition, price_per_day, quantity, min_days, max_days, pickup_available, delivery_available, delivery_note, address, city, lat, lon, status, created_at, updated_at, settlement_id)
SELECT u.id, c.id, 'Віброплита 90 кг', 'Підходить для домашніх і професійних робіт. Видаю з кейсом і комплектом аксесуарів.', 'EXCELLENT', 820.00, 3, 1, NULL, TRUE, FALSE, NULL, 'просп. Червоної Калини, 88', 'Львів', 49.861960, 24.028247, 'ARCHIVED', CURRENT_TIMESTAMP - INTERVAL '19' DAY, CURRENT_TIMESTAMP - INTERVAL '19' DAY, (SELECT id FROM settlements WHERE osm_id = 26150791)
FROM users u, categories c WHERE u.email = 'seed.budprokat@example.com' AND c.name = 'Будівельне обладнання'
AND NOT EXISTS (SELECT 1 FROM equipment_photos WHERE storage_key = 'seed/equipment-012.png');
INSERT INTO equipment_photos (equipment_id, storage_key, position, created_at)
SELECT MAX(e.id), 'seed/equipment-012.png', 0, CURRENT_TIMESTAMP FROM equipment e JOIN users u ON u.id = e.owner_id
WHERE e.title = 'Віброплита 90 кг' AND u.email = 'seed.budprokat@example.com'
AND NOT EXISTS (SELECT 1 FROM equipment_photos WHERE storage_key = 'seed/equipment-012.png')
HAVING MAX(e.id) IS NOT NULL;

-- 013. Риштування пересувне 4 м
INSERT INTO equipment (owner_id, category_id, title, description, item_condition, price_per_day, quantity, min_days, max_days, pickup_available, delivery_available, delivery_note, address, city, lat, lon, status, created_at, updated_at, settlement_id)
SELECT u.id, c.id, 'Риштування пересувне 4 м', 'Справний, перевірений перед кожною орендою. Видаю з інструкцією та всім потрібним для роботи.', 'FAIR', 600.00, 2, 3, 7, TRUE, FALSE, NULL, 'вул. Пасічна, 85', 'Львів', 49.815712, 24.040373, 'ACTIVE', CURRENT_TIMESTAMP - INTERVAL '11' DAY, CURRENT_TIMESTAMP - INTERVAL '11' DAY, (SELECT id FROM settlements WHERE osm_id = 26150791)
FROM users u, categories c WHERE u.email = 'seed.taras@example.com' AND c.name = 'Риштування та драбини'
AND NOT EXISTS (SELECT 1 FROM equipment_photos WHERE storage_key = 'seed/equipment-013.png');
INSERT INTO equipment_photos (equipment_id, storage_key, position, created_at)
SELECT MAX(e.id), 'seed/equipment-013.png', 0, CURRENT_TIMESTAMP FROM equipment e JOIN users u ON u.id = e.owner_id
WHERE e.title = 'Риштування пересувне 4 м' AND u.email = 'seed.taras@example.com'
AND NOT EXISTS (SELECT 1 FROM equipment_photos WHERE storage_key = 'seed/equipment-013.png')
HAVING MAX(e.id) IS NOT NULL;

-- 014. Тример бензиновий Stihl FS 55
INSERT INTO equipment (owner_id, category_id, title, description, item_condition, price_per_day, quantity, min_days, max_days, pickup_available, delivery_available, delivery_note, address, city, lat, lon, status, created_at, updated_at, settlement_id)
SELECT u.id, c.id, 'Тример бензиновий Stihl FS 55', 'Підходить для домашніх і професійних робіт. Видаю з кейсом і комплектом аксесуарів.', 'GOOD', 450.00, 1, 1, 7, TRUE, FALSE, NULL, 'вул. Княгині Ольги, 15', 'Львів', 49.880837, 24.068219, 'ACTIVE', CURRENT_TIMESTAMP - INTERVAL '27' DAY, CURRENT_TIMESTAMP - INTERVAL '27' DAY, (SELECT id FROM settlements WHERE osm_id = 26150791)
FROM users u, categories c WHERE u.email = 'seed.andrii@example.com' AND c.name = 'Садова техніка'
AND NOT EXISTS (SELECT 1 FROM equipment_photos WHERE storage_key = 'seed/equipment-014.png');
INSERT INTO equipment_photos (equipment_id, storage_key, position, created_at)
SELECT MAX(e.id), 'seed/equipment-014.png', 0, CURRENT_TIMESTAMP FROM equipment e JOIN users u ON u.id = e.owner_id
WHERE e.title = 'Тример бензиновий Stihl FS 55' AND u.email = 'seed.andrii@example.com'
AND NOT EXISTS (SELECT 1 FROM equipment_photos WHERE storage_key = 'seed/equipment-014.png')
HAVING MAX(e.id) IS NOT NULL;

-- 015. Снігоприбирач електричний Einhell
INSERT INTO equipment (owner_id, category_id, title, description, item_condition, price_per_day, quantity, min_days, max_days, pickup_available, delivery_available, delivery_note, address, city, lat, lon, status, created_at, updated_at, settlement_id)
SELECT u.id, c.id, 'Снігоприбирач електричний Einhell', 'Підходить для домашніх і професійних робіт. Видаю з кейсом і комплектом аксесуарів.', 'EXCELLENT', 370.00, 1, 1, 30, TRUE, FALSE, NULL, 'просп. Червоної Калини, 118', 'Львів', 49.837865, 24.007251, 'ACTIVE', CURRENT_TIMESTAMP - INTERVAL '38' DAY, CURRENT_TIMESTAMP - INTERVAL '38' DAY, (SELECT id FROM settlements WHERE osm_id = 26150791)
FROM users u, categories c WHERE u.email = 'seed.olena@example.com' AND c.name = 'Снігоприбиральна техніка'
AND NOT EXISTS (SELECT 1 FROM equipment_photos WHERE storage_key = 'seed/equipment-015.png');
INSERT INTO equipment_photos (equipment_id, storage_key, position, created_at)
SELECT MAX(e.id), 'seed/equipment-015.png', 0, CURRENT_TIMESTAMP FROM equipment e JOIN users u ON u.id = e.owner_id
WHERE e.title = 'Снігоприбирач електричний Einhell' AND u.email = 'seed.olena@example.com'
AND NOT EXISTS (SELECT 1 FROM equipment_photos WHERE storage_key = 'seed/equipment-015.png')
HAVING MAX(e.id) IS NOT NULL;
INSERT INTO equipment_photos (equipment_id, storage_key, position, created_at)
SELECT MAX(e.id), 'seed/equipment-015-2.png', 1, CURRENT_TIMESTAMP FROM equipment e JOIN users u ON u.id = e.owner_id
WHERE e.title = 'Снігоприбирач електричний Einhell' AND u.email = 'seed.olena@example.com'
AND NOT EXISTS (SELECT 1 FROM equipment_photos WHERE storage_key = 'seed/equipment-015-2.png')
HAVING MAX(e.id) IS NOT NULL;

-- 016. Миючий пилосос Kärcher Puzzi 10/1
INSERT INTO equipment (owner_id, category_id, title, description, item_condition, price_per_day, quantity, min_days, max_days, pickup_available, delivery_available, delivery_note, address, city, lat, lon, status, created_at, updated_at, settlement_id)
SELECT u.id, c.id, 'Миючий пилосос Kärcher Puzzi 10/1', 'Справний, перевірений перед кожною орендою. Видаю з інструкцією та всім потрібним для роботи.', 'GOOD', 320.00, 1, 1, 7, TRUE, FALSE, NULL, 'вул. Замарстинівська, 34', 'Львів', 49.855653, 24.018318, 'ACTIVE', CURRENT_TIMESTAMP - INTERVAL '47' DAY, CURRENT_TIMESTAMP - INTERVAL '47' DAY, (SELECT id FROM settlements WHERE osm_id = 26150791)
FROM users u, categories c WHERE u.email = 'seed.marta@example.com' AND c.name = 'Клінінгове обладнання'
AND NOT EXISTS (SELECT 1 FROM equipment_photos WHERE storage_key = 'seed/equipment-016.png');
INSERT INTO equipment_photos (equipment_id, storage_key, position, created_at)
SELECT MAX(e.id), 'seed/equipment-016.png', 0, CURRENT_TIMESTAMP FROM equipment e JOIN users u ON u.id = e.owner_id
WHERE e.title = 'Миючий пилосос Kärcher Puzzi 10/1' AND u.email = 'seed.marta@example.com'
AND NOT EXISTS (SELECT 1 FROM equipment_photos WHERE storage_key = 'seed/equipment-016.png')
HAVING MAX(e.id) IS NOT NULL;

-- 017. Фотоапарат Canon EOS R6
INSERT INTO equipment (owner_id, category_id, title, description, item_condition, price_per_day, quantity, min_days, max_days, pickup_available, delivery_available, delivery_note, address, city, lat, lon, status, created_at, updated_at, settlement_id)
SELECT u.id, c.id, 'Фотоапарат Canon EOS R6', 'У хорошому стані, регулярно обслуговується. Можу підказати, як найкраще використати.', 'EXCELLENT', 760.00, 1, 1, 14, TRUE, TRUE, 'Доставка по Львову та області, ціну узгодимо', 'вул. Кульпарківська, 63', 'Львів', 49.881349, 24.022540, 'ACTIVE', CURRENT_TIMESTAMP - INTERVAL '12' DAY, CURRENT_TIMESTAMP - INTERVAL '12' DAY, (SELECT id FROM settlements WHERE osm_id = 26150791)
FROM users u, categories c WHERE u.email = 'seed.fotozona@example.com' AND c.name = 'Фото- та відеотехніка'
AND NOT EXISTS (SELECT 1 FROM equipment_photos WHERE storage_key = 'seed/equipment-017.png');
INSERT INTO equipment_photos (equipment_id, storage_key, position, created_at)
SELECT MAX(e.id), 'seed/equipment-017.png', 0, CURRENT_TIMESTAMP FROM equipment e JOIN users u ON u.id = e.owner_id
WHERE e.title = 'Фотоапарат Canon EOS R6' AND u.email = 'seed.fotozona@example.com'
AND NOT EXISTS (SELECT 1 FROM equipment_photos WHERE storage_key = 'seed/equipment-017.png')
HAVING MAX(e.id) IS NOT NULL;

-- 018. Комплект студійного світла 3×LED
INSERT INTO equipment (owner_id, category_id, title, description, item_condition, price_per_day, quantity, min_days, max_days, pickup_available, delivery_available, delivery_note, address, city, lat, lon, status, created_at, updated_at, settlement_id)
SELECT u.id, c.id, 'Комплект студійного світла 3×LED', 'Підходить для домашніх і професійних робіт. Видаю з кейсом і комплектом аксесуарів.', 'NEW', 750.00, 2, 2, 30, TRUE, FALSE, NULL, 'вул. Стрийська, 5', 'Львів', 49.854199, 24.095347, 'ACTIVE', CURRENT_TIMESTAMP - INTERVAL '41' DAY, CURRENT_TIMESTAMP - INTERVAL '41' DAY, (SELECT id FROM settlements WHERE osm_id = 26150791)
FROM users u, categories c WHERE u.email = 'seed.fotozona@example.com' AND c.name = 'Світло та звук'
AND NOT EXISTS (SELECT 1 FROM equipment_photos WHERE storage_key = 'seed/equipment-018.png');
INSERT INTO equipment_photos (equipment_id, storage_key, position, created_at)
SELECT MAX(e.id), 'seed/equipment-018.png', 0, CURRENT_TIMESTAMP FROM equipment e JOIN users u ON u.id = e.owner_id
WHERE e.title = 'Комплект студійного світла 3×LED' AND u.email = 'seed.fotozona@example.com'
AND NOT EXISTS (SELECT 1 FROM equipment_photos WHERE storage_key = 'seed/equipment-018.png')
HAVING MAX(e.id) IS NOT NULL;

-- 019. Каяк надувний двомісний
INSERT INTO equipment (owner_id, category_id, title, description, item_condition, price_per_day, quantity, min_days, max_days, pickup_available, delivery_available, delivery_note, address, city, lat, lon, status, created_at, updated_at, settlement_id)
SELECT u.id, c.id, 'Каяк надувний двомісний', 'Акуратне використання, без пошкоджень. Перед видачею чищу й перевіряю.', 'GOOD', 270.00, 1, 1, 14, TRUE, FALSE, NULL, 'вул. Замарстинівська, 110', 'Львів', 49.857843, 23.995909, 'ACTIVE', CURRENT_TIMESTAMP - INTERVAL '23' DAY, CURRENT_TIMESTAMP - INTERVAL '23' DAY, (SELECT id FROM settlements WHERE osm_id = 26150791)
FROM users u, categories c WHERE u.email = 'seed.marta@example.com' AND c.name = 'Туристичне спорядження'
AND NOT EXISTS (SELECT 1 FROM equipment_photos WHERE storage_key = 'seed/equipment-019.png');
INSERT INTO equipment_photos (equipment_id, storage_key, position, created_at)
SELECT MAX(e.id), 'seed/equipment-019.png', 0, CURRENT_TIMESTAMP FROM equipment e JOIN users u ON u.id = e.owner_id
WHERE e.title = 'Каяк надувний двомісний' AND u.email = 'seed.marta@example.com'
AND NOT EXISTS (SELECT 1 FROM equipment_photos WHERE storage_key = 'seed/equipment-019.png')
HAVING MAX(e.id) IS NOT NULL;

-- 020. Ножичний підйомник 8 м
INSERT INTO equipment (owner_id, category_id, title, description, item_condition, price_per_day, quantity, min_days, max_days, pickup_available, delivery_available, delivery_note, address, city, lat, lon, status, created_at, updated_at, settlement_id)
SELECT u.id, c.id, 'Ножичний підйомник 8 м', 'Акуратне використання, без пошкоджень. Перед видачею чищу й перевіряю.', 'NEW', 3690.00, 1, 2, 14, TRUE, TRUE, NULL, 'вул. Виговського, 65', 'Львів', 49.845449, 24.058788, 'ACTIVE', CURRENT_TIMESTAMP - INTERVAL '13' DAY, CURRENT_TIMESTAMP - INTERVAL '13' DAY, (SELECT id FROM settlements WHERE osm_id = 26150791)
FROM users u, categories c WHERE u.email = 'seed.budprokat@example.com' AND c.name = 'Спецтехніка'
AND NOT EXISTS (SELECT 1 FROM equipment_photos WHERE storage_key = 'seed/equipment-020.png');
INSERT INTO equipment_photos (equipment_id, storage_key, position, created_at)
SELECT MAX(e.id), 'seed/equipment-020.png', 0, CURRENT_TIMESTAMP FROM equipment e JOIN users u ON u.id = e.owner_id
WHERE e.title = 'Ножичний підйомник 8 м' AND u.email = 'seed.budprokat@example.com'
AND NOT EXISTS (SELECT 1 FROM equipment_photos WHERE storage_key = 'seed/equipment-020.png')
HAVING MAX(e.id) IS NOT NULL;
INSERT INTO equipment_photos (equipment_id, storage_key, position, created_at)
SELECT MAX(e.id), 'seed/equipment-020-2.png', 1, CURRENT_TIMESTAMP FROM equipment e JOIN users u ON u.id = e.owner_id
WHERE e.title = 'Ножичний підйомник 8 м' AND u.email = 'seed.budprokat@example.com'
AND NOT EXISTS (SELECT 1 FROM equipment_photos WHERE storage_key = 'seed/equipment-020-2.png')
HAVING MAX(e.id) IS NOT NULL;

-- 021. Кутова шліфмашина DeWalt DWE4157
INSERT INTO equipment (owner_id, category_id, title, description, item_condition, price_per_day, quantity, min_days, max_days, pickup_available, delivery_available, delivery_note, address, city, lat, lon, status, created_at, updated_at, settlement_id)
SELECT u.id, c.id, 'Кутова шліфмашина DeWalt DWE4157', 'Підходить для домашніх і професійних робіт. Видаю з кейсом і комплектом аксесуарів.', 'GOOD', 250.00, 1, 1, 30, TRUE, FALSE, NULL, 'вул. Варшавська, 109', 'Львів', 49.819889, 23.983579, 'ACTIVE', CURRENT_TIMESTAMP - INTERVAL '20' DAY, CURRENT_TIMESTAMP - INTERVAL '20' DAY, (SELECT id FROM settlements WHERE osm_id = 26150791)
FROM users u, categories c WHERE u.email = 'seed.andrii@example.com' AND c.name = 'Електроінструмент'
AND NOT EXISTS (SELECT 1 FROM equipment_photos WHERE storage_key = 'seed/equipment-021.png');
INSERT INTO equipment_photos (equipment_id, storage_key, position, created_at)
SELECT MAX(e.id), 'seed/equipment-021.png', 0, CURRENT_TIMESTAMP FROM equipment e JOIN users u ON u.id = e.owner_id
WHERE e.title = 'Кутова шліфмашина DeWalt DWE4157' AND u.email = 'seed.andrii@example.com'
AND NOT EXISTS (SELECT 1 FROM equipment_photos WHERE storage_key = 'seed/equipment-021.png')
HAVING MAX(e.id) IS NOT NULL;

-- 022. Відбійний молоток Makita HM1307
INSERT INTO equipment (owner_id, category_id, title, description, item_condition, price_per_day, quantity, min_days, max_days, pickup_available, delivery_available, delivery_note, address, city, lat, lon, status, created_at, updated_at, settlement_id)
SELECT u.id, c.id, 'Відбійний молоток Makita HM1307', 'Надійний і простий у використанні. Повернути прошу в чистому вигляді.', 'FAIR', 500.00, 3, 1, 7, TRUE, FALSE, NULL, 'просп. Червоної Калини, 51', 'Львів', 49.816451, 24.040919, 'ACTIVE', CURRENT_TIMESTAMP - INTERVAL '15' DAY, CURRENT_TIMESTAMP - INTERVAL '15' DAY, (SELECT id FROM settlements WHERE osm_id = 26150791)
FROM users u, categories c WHERE u.email = 'seed.budprokat@example.com' AND c.name = 'Будівельне обладнання'
AND NOT EXISTS (SELECT 1 FROM equipment_photos WHERE storage_key = 'seed/equipment-022.png');
INSERT INTO equipment_photos (equipment_id, storage_key, position, created_at)
SELECT MAX(e.id), 'seed/equipment-022.png', 0, CURRENT_TIMESTAMP FROM equipment e JOIN users u ON u.id = e.owner_id
WHERE e.title = 'Відбійний молоток Makita HM1307' AND u.email = 'seed.budprokat@example.com'
AND NOT EXISTS (SELECT 1 FROM equipment_photos WHERE storage_key = 'seed/equipment-022.png')
HAVING MAX(e.id) IS NOT NULL;

-- 023. Драбина-трансформер 4×4
INSERT INTO equipment (owner_id, category_id, title, description, item_condition, price_per_day, quantity, min_days, max_days, pickup_available, delivery_available, delivery_note, address, city, lat, lon, status, created_at, updated_at, settlement_id)
SELECT u.id, c.id, 'Драбина-трансформер 4×4', 'Підходить для домашніх і професійних робіт. Видаю з кейсом і комплектом аксесуарів.', 'EXCELLENT', 500.00, 1, 1, 7, TRUE, FALSE, NULL, 'вул. Центральна, 60', 'Брюховичі', 49.913110, 23.953852, 'ACTIVE', CURRENT_TIMESTAMP - INTERVAL '28' DAY, CURRENT_TIMESTAMP - INTERVAL '28' DAY, (SELECT id FROM settlements WHERE osm_id = 337541358)
FROM users u, categories c WHERE u.email = 'seed.taras@example.com' AND c.name = 'Риштування та драбини'
AND NOT EXISTS (SELECT 1 FROM equipment_photos WHERE storage_key = 'seed/equipment-023.png');
INSERT INTO equipment_photos (equipment_id, storage_key, position, created_at)
SELECT MAX(e.id), 'seed/equipment-023.png', 0, CURRENT_TIMESTAMP FROM equipment e JOIN users u ON u.id = e.owner_id
WHERE e.title = 'Драбина-трансформер 4×4' AND u.email = 'seed.taras@example.com'
AND NOT EXISTS (SELECT 1 FROM equipment_photos WHERE storage_key = 'seed/equipment-023.png')
HAVING MAX(e.id) IS NOT NULL;

-- 024. Культиватор 6,5 к.с.
INSERT INTO equipment (owner_id, category_id, title, description, item_condition, price_per_day, quantity, min_days, max_days, pickup_available, delivery_available, delivery_note, address, city, lat, lon, status, created_at, updated_at, settlement_id)
SELECT u.id, c.id, 'Культиватор 6,5 к.с.', 'Надійний і простий у використанні. Повернути прошу в чистому вигляді.', 'EXCELLENT', 410.00, 1, 3, 7, TRUE, FALSE, NULL, 'вул. Бандери, 52', 'Львів', 49.806729, 24.019708, 'ACTIVE', CURRENT_TIMESTAMP - INTERVAL '50' DAY, CURRENT_TIMESTAMP - INTERVAL '50' DAY, (SELECT id FROM settlements WHERE osm_id = 26150791)
FROM users u, categories c WHERE u.email = 'seed.marta@example.com' AND c.name = 'Садова техніка'
AND NOT EXISTS (SELECT 1 FROM equipment_photos WHERE storage_key = 'seed/equipment-024.png');
INSERT INTO equipment_photos (equipment_id, storage_key, position, created_at)
SELECT MAX(e.id), 'seed/equipment-024.png', 0, CURRENT_TIMESTAMP FROM equipment e JOIN users u ON u.id = e.owner_id
WHERE e.title = 'Культиватор 6,5 к.с.' AND u.email = 'seed.marta@example.com'
AND NOT EXISTS (SELECT 1 FROM equipment_photos WHERE storage_key = 'seed/equipment-024.png')
HAVING MAX(e.id) IS NOT NULL;

-- 025. Снігоприбирач акумуляторний Greenworks
INSERT INTO equipment (owner_id, category_id, title, description, item_condition, price_per_day, quantity, min_days, max_days, pickup_available, delivery_available, delivery_note, address, city, lat, lon, status, created_at, updated_at, settlement_id)
SELECT u.id, c.id, 'Снігоприбирач акумуляторний Greenworks', 'Надійний і простий у використанні. Повернути прошу в чистому вигляді.', 'FAIR', 460.00, 5, 1, 14, TRUE, TRUE, 'Доставка по Львову, вартість за домовленістю', 'вул. Хоткевича, 38', 'Львів', 49.803449, 24.040260, 'ACTIVE', CURRENT_TIMESTAMP - INTERVAL '9' DAY, CURRENT_TIMESTAMP - INTERVAL '9' DAY, (SELECT id FROM settlements WHERE osm_id = 26150791)
FROM users u, categories c WHERE u.email = 'seed.taras@example.com' AND c.name = 'Снігоприбиральна техніка'
AND NOT EXISTS (SELECT 1 FROM equipment_photos WHERE storage_key = 'seed/equipment-025.png');
INSERT INTO equipment_photos (equipment_id, storage_key, position, created_at)
SELECT MAX(e.id), 'seed/equipment-025.png', 0, CURRENT_TIMESTAMP FROM equipment e JOIN users u ON u.id = e.owner_id
WHERE e.title = 'Снігоприбирач акумуляторний Greenworks' AND u.email = 'seed.taras@example.com'
AND NOT EXISTS (SELECT 1 FROM equipment_photos WHERE storage_key = 'seed/equipment-025.png')
HAVING MAX(e.id) IS NOT NULL;
INSERT INTO equipment_photos (equipment_id, storage_key, position, created_at)
SELECT MAX(e.id), 'seed/equipment-025-2.png', 1, CURRENT_TIMESTAMP FROM equipment e JOIN users u ON u.id = e.owner_id
WHERE e.title = 'Снігоприбирач акумуляторний Greenworks' AND u.email = 'seed.taras@example.com'
AND NOT EXISTS (SELECT 1 FROM equipment_photos WHERE storage_key = 'seed/equipment-025-2.png')
HAVING MAX(e.id) IS NOT NULL;

-- 026. Будівельний пилосос Makita VC2512L
INSERT INTO equipment (owner_id, category_id, title, description, item_condition, price_per_day, quantity, min_days, max_days, pickup_available, delivery_available, delivery_note, address, city, lat, lon, status, created_at, updated_at, settlement_id)
SELECT u.id, c.id, 'Будівельний пилосос Makita VC2512L', 'У хорошому стані, регулярно обслуговується. Можу підказати, як найкраще використати.', 'NEW', 310.00, 1, 1, 7, TRUE, TRUE, 'Доставка по Львову, вартість за домовленістю', 'вул. Княгині Ольги, 60', 'Львів', 49.861931, 24.049091, 'ACTIVE', CURRENT_TIMESTAMP - INTERVAL '41' DAY, CURRENT_TIMESTAMP - INTERVAL '41' DAY, (SELECT id FROM settlements WHERE osm_id = 26150791)
FROM users u, categories c WHERE u.email = 'seed.olena@example.com' AND c.name = 'Клінінгове обладнання'
AND NOT EXISTS (SELECT 1 FROM equipment_photos WHERE storage_key = 'seed/equipment-026.png');
INSERT INTO equipment_photos (equipment_id, storage_key, position, created_at)
SELECT MAX(e.id), 'seed/equipment-026.png', 0, CURRENT_TIMESTAMP FROM equipment e JOIN users u ON u.id = e.owner_id
WHERE e.title = 'Будівельний пилосос Makita VC2512L' AND u.email = 'seed.olena@example.com'
AND NOT EXISTS (SELECT 1 FROM equipment_photos WHERE storage_key = 'seed/equipment-026.png')
HAVING MAX(e.id) IS NOT NULL;

-- 027. Об’єктив Sony 70-200 f/2.8
INSERT INTO equipment (owner_id, category_id, title, description, item_condition, price_per_day, quantity, min_days, max_days, pickup_available, delivery_available, delivery_note, address, city, lat, lon, status, created_at, updated_at, settlement_id)
SELECT u.id, c.id, 'Об’єктив Sony 70-200 f/2.8', 'У хорошому стані, регулярно обслуговується. Можу підказати, як найкраще використати.', 'EXCELLENT', 900.00, 3, 1, 14, TRUE, FALSE, NULL, 'вул. Стрийська, 117', 'Львів', 49.815625, 23.994104, 'ACTIVE', CURRENT_TIMESTAMP - INTERVAL '48' DAY, CURRENT_TIMESTAMP - INTERVAL '48' DAY, (SELECT id FROM settlements WHERE osm_id = 26150791)
FROM users u, categories c WHERE u.email = 'seed.fotozona@example.com' AND c.name = 'Фото- та відеотехніка'
AND NOT EXISTS (SELECT 1 FROM equipment_photos WHERE storage_key = 'seed/equipment-027.png');
INSERT INTO equipment_photos (equipment_id, storage_key, position, created_at)
SELECT MAX(e.id), 'seed/equipment-027.png', 0, CURRENT_TIMESTAMP FROM equipment e JOIN users u ON u.id = e.owner_id
WHERE e.title = 'Об’єктив Sony 70-200 f/2.8' AND u.email = 'seed.fotozona@example.com'
AND NOT EXISTS (SELECT 1 FROM equipment_photos WHERE storage_key = 'seed/equipment-027.png')
HAVING MAX(e.id) IS NOT NULL;

-- 028. Радіосистема Shure BLX24
INSERT INTO equipment (owner_id, category_id, title, description, item_condition, price_per_day, quantity, min_days, max_days, pickup_available, delivery_available, delivery_note, address, city, lat, lon, status, created_at, updated_at, settlement_id)
SELECT u.id, c.id, 'Радіосистема Shure BLX24', 'Акуратне використання, без пошкоджень. Перед видачею чищу й перевіряю.', 'NEW', 410.00, 1, 3, 7, FALSE, TRUE, 'Доставка по Львову, вартість за домовленістю', 'вул. Личаківська, 112', 'Львів', 49.796678, 24.032486, 'ACTIVE', CURRENT_TIMESTAMP - INTERVAL '31' DAY, CURRENT_TIMESTAMP - INTERVAL '31' DAY, (SELECT id FROM settlements WHERE osm_id = 26150791)
FROM users u, categories c WHERE u.email = 'seed.olena@example.com' AND c.name = 'Світло та звук'
AND NOT EXISTS (SELECT 1 FROM equipment_photos WHERE storage_key = 'seed/equipment-028.png');
INSERT INTO equipment_photos (equipment_id, storage_key, position, created_at)
SELECT MAX(e.id), 'seed/equipment-028.png', 0, CURRENT_TIMESTAMP FROM equipment e JOIN users u ON u.id = e.owner_id
WHERE e.title = 'Радіосистема Shure BLX24' AND u.email = 'seed.olena@example.com'
AND NOT EXISTS (SELECT 1 FROM equipment_photos WHERE storage_key = 'seed/equipment-028.png')
HAVING MAX(e.id) IS NOT NULL;

-- 029. Спальник −10 °C
INSERT INTO equipment (owner_id, category_id, title, description, item_condition, price_per_day, quantity, min_days, max_days, pickup_available, delivery_available, delivery_note, address, city, lat, lon, status, created_at, updated_at, settlement_id)
SELECT u.id, c.id, 'Спальник −10 °C', 'У хорошому стані, регулярно обслуговується. Можу підказати, як найкраще використати.', 'EXCELLENT', 190.00, 2, 2, 30, TRUE, TRUE, NULL, 'вул. Наукова, 104', 'Львів', 49.867070, 24.052574, 'ACTIVE', CURRENT_TIMESTAMP - INTERVAL '39' DAY, CURRENT_TIMESTAMP - INTERVAL '39' DAY, (SELECT id FROM settlements WHERE osm_id = 26150791)
FROM users u, categories c WHERE u.email = 'seed.andrii@example.com' AND c.name = 'Туристичне спорядження'
AND NOT EXISTS (SELECT 1 FROM equipment_photos WHERE storage_key = 'seed/equipment-029.png');
INSERT INTO equipment_photos (equipment_id, storage_key, position, created_at)
SELECT MAX(e.id), 'seed/equipment-029.png', 0, CURRENT_TIMESTAMP FROM equipment e JOIN users u ON u.id = e.owner_id
WHERE e.title = 'Спальник −10 °C' AND u.email = 'seed.andrii@example.com'
AND NOT EXISTS (SELECT 1 FROM equipment_photos WHERE storage_key = 'seed/equipment-029.png')
HAVING MAX(e.id) IS NOT NULL;

-- 030. Мотопомпа для брудної води
INSERT INTO equipment (owner_id, category_id, title, description, item_condition, price_per_day, quantity, min_days, max_days, pickup_available, delivery_available, delivery_note, address, city, lat, lon, status, created_at, updated_at, settlement_id)
SELECT u.id, c.id, 'Мотопомпа для брудної води', 'У хорошому стані, регулярно обслуговується. Можу підказати, як найкраще використати.', 'EXCELLENT', 2700.00, 1, 1, 30, TRUE, FALSE, NULL, 'вул. Стрийська, 44', 'Львів', 49.798991, 24.054614, 'ACTIVE', CURRENT_TIMESTAMP - INTERVAL '60' DAY, CURRENT_TIMESTAMP - INTERVAL '60' DAY, (SELECT id FROM settlements WHERE osm_id = 26150791)
FROM users u, categories c WHERE u.email = 'seed.budprokat@example.com' AND c.name = 'Спецтехніка'
AND NOT EXISTS (SELECT 1 FROM equipment_photos WHERE storage_key = 'seed/equipment-030.png');
INSERT INTO equipment_photos (equipment_id, storage_key, position, created_at)
SELECT MAX(e.id), 'seed/equipment-030.png', 0, CURRENT_TIMESTAMP FROM equipment e JOIN users u ON u.id = e.owner_id
WHERE e.title = 'Мотопомпа для брудної води' AND u.email = 'seed.budprokat@example.com'
AND NOT EXISTS (SELECT 1 FROM equipment_photos WHERE storage_key = 'seed/equipment-030.png')
HAVING MAX(e.id) IS NOT NULL;
INSERT INTO equipment_photos (equipment_id, storage_key, position, created_at)
SELECT MAX(e.id), 'seed/equipment-030-2.png', 1, CURRENT_TIMESTAMP FROM equipment e JOIN users u ON u.id = e.owner_id
WHERE e.title = 'Мотопомпа для брудної води' AND u.email = 'seed.budprokat@example.com'
AND NOT EXISTS (SELECT 1 FROM equipment_photos WHERE storage_key = 'seed/equipment-030-2.png')
HAVING MAX(e.id) IS NOT NULL;

-- 031. Електролобзик Bosch PST 700
INSERT INTO equipment (owner_id, category_id, title, description, item_condition, price_per_day, quantity, min_days, max_days, pickup_available, delivery_available, delivery_note, address, city, lat, lon, status, created_at, updated_at, settlement_id)
SELECT u.id, c.id, 'Електролобзик Bosch PST 700', 'Акуратне використання, без пошкоджень. Перед видачею чищу й перевіряю.', 'FAIR', 190.00, 5, 1, 14, TRUE, FALSE, NULL, 'вул. Наукова, 48', 'Львів', 49.798970, 24.047625, 'ACTIVE', CURRENT_TIMESTAMP - INTERVAL '27' DAY, CURRENT_TIMESTAMP - INTERVAL '27' DAY, (SELECT id FROM settlements WHERE osm_id = 26150791)
FROM users u, categories c WHERE u.email = 'seed.taras@example.com' AND c.name = 'Електроінструмент'
AND NOT EXISTS (SELECT 1 FROM equipment_photos WHERE storage_key = 'seed/equipment-031.png');
INSERT INTO equipment_photos (equipment_id, storage_key, position, created_at)
SELECT MAX(e.id), 'seed/equipment-031.png', 0, CURRENT_TIMESTAMP FROM equipment e JOIN users u ON u.id = e.owner_id
WHERE e.title = 'Електролобзик Bosch PST 700' AND u.email = 'seed.taras@example.com'
AND NOT EXISTS (SELECT 1 FROM equipment_photos WHERE storage_key = 'seed/equipment-031.png')
HAVING MAX(e.id) IS NOT NULL;

-- 032. Плиткоріз електричний 800 мм
INSERT INTO equipment (owner_id, category_id, title, description, item_condition, price_per_day, quantity, min_days, max_days, pickup_available, delivery_available, delivery_note, address, city, lat, lon, status, created_at, updated_at, settlement_id)
SELECT u.id, c.id, 'Плиткоріз електричний 800 мм', 'У хорошому стані, регулярно обслуговується. Можу підказати, як найкраще використати.', 'EXCELLENT', 340.00, 1, 1, 30, TRUE, FALSE, NULL, 'вул. Хоткевича, 105', 'Львів', 49.846040, 24.065118, 'ACTIVE', CURRENT_TIMESTAMP - INTERVAL '47' DAY, CURRENT_TIMESTAMP - INTERVAL '47' DAY, (SELECT id FROM settlements WHERE osm_id = 26150791)
FROM users u, categories c WHERE u.email = 'seed.budprokat@example.com' AND c.name = 'Будівельне обладнання'
AND NOT EXISTS (SELECT 1 FROM equipment_photos WHERE storage_key = 'seed/equipment-032.png');
INSERT INTO equipment_photos (equipment_id, storage_key, position, created_at)
SELECT MAX(e.id), 'seed/equipment-032.png', 0, CURRENT_TIMESTAMP FROM equipment e JOIN users u ON u.id = e.owner_id
WHERE e.title = 'Плиткоріз електричний 800 мм' AND u.email = 'seed.budprokat@example.com'
AND NOT EXISTS (SELECT 1 FROM equipment_photos WHERE storage_key = 'seed/equipment-032.png')
HAVING MAX(e.id) IS NOT NULL;

-- 033. Помост малярний 1,5 м
INSERT INTO equipment (owner_id, category_id, title, description, item_condition, price_per_day, quantity, min_days, max_days, pickup_available, delivery_available, delivery_note, address, city, lat, lon, status, created_at, updated_at, settlement_id)
SELECT u.id, c.id, 'Помост малярний 1,5 м', 'Справний, перевірений перед кожною орендою. Видаю з інструкцією та всім потрібним для роботи.', 'FAIR', 530.00, 2, 2, 14, TRUE, TRUE, NULL, 'вул. Городоцька, 13', 'Львів', 49.805824, 23.998610, 'ACTIVE', CURRENT_TIMESTAMP - INTERVAL '1' DAY, CURRENT_TIMESTAMP - INTERVAL '1' DAY, (SELECT id FROM settlements WHERE osm_id = 26150791)
FROM users u, categories c WHERE u.email = 'seed.olena@example.com' AND c.name = 'Риштування та драбини'
AND NOT EXISTS (SELECT 1 FROM equipment_photos WHERE storage_key = 'seed/equipment-033.png');
INSERT INTO equipment_photos (equipment_id, storage_key, position, created_at)
SELECT MAX(e.id), 'seed/equipment-033.png', 0, CURRENT_TIMESTAMP FROM equipment e JOIN users u ON u.id = e.owner_id
WHERE e.title = 'Помост малярний 1,5 м' AND u.email = 'seed.olena@example.com'
AND NOT EXISTS (SELECT 1 FROM equipment_photos WHERE storage_key = 'seed/equipment-033.png')
HAVING MAX(e.id) IS NOT NULL;

-- 034. Кущоріз Bosch AHS 50
INSERT INTO equipment (owner_id, category_id, title, description, item_condition, price_per_day, quantity, min_days, max_days, pickup_available, delivery_available, delivery_note, address, city, lat, lon, status, created_at, updated_at, settlement_id)
SELECT u.id, c.id, 'Кущоріз Bosch AHS 50', 'Надійний і простий у використанні. Повернути прошу в чистому вигляді.', 'GOOD', 390.00, 1, 3, 7, TRUE, TRUE, 'Привезу в межах міста за 150 ₴', 'вул. Хоткевича, 8', 'Львів', 49.837304, 24.092958, 'ACTIVE', CURRENT_TIMESTAMP - INTERVAL '16' DAY, CURRENT_TIMESTAMP - INTERVAL '16' DAY, (SELECT id FROM settlements WHERE osm_id = 26150791)
FROM users u, categories c WHERE u.email = 'seed.marta@example.com' AND c.name = 'Садова техніка'
AND NOT EXISTS (SELECT 1 FROM equipment_photos WHERE storage_key = 'seed/equipment-034.png');
INSERT INTO equipment_photos (equipment_id, storage_key, position, created_at)
SELECT MAX(e.id), 'seed/equipment-034.png', 0, CURRENT_TIMESTAMP FROM equipment e JOIN users u ON u.id = e.owner_id
WHERE e.title = 'Кущоріз Bosch AHS 50' AND u.email = 'seed.marta@example.com'
AND NOT EXISTS (SELECT 1 FROM equipment_photos WHERE storage_key = 'seed/equipment-034.png')
HAVING MAX(e.id) IS NOT NULL;

-- 035. Снігоприбирач бензиновий Honda HSS 655
INSERT INTO equipment (owner_id, category_id, title, description, item_condition, price_per_day, quantity, min_days, max_days, pickup_available, delivery_available, delivery_note, address, city, lat, lon, status, created_at, updated_at, settlement_id)
SELECT u.id, c.id, 'Снігоприбирач бензиновий Honda HSS 655', 'Справний, перевірений перед кожною орендою. Видаю з інструкцією та всім потрібним для роботи.', 'EXCELLENT', 510.00, 1, 1, 7, TRUE, FALSE, NULL, 'вул. Сихівська, 7', 'Львів', 49.834307, 23.967438, 'ACTIVE', CURRENT_TIMESTAMP - INTERVAL '41' DAY, CURRENT_TIMESTAMP - INTERVAL '41' DAY, (SELECT id FROM settlements WHERE osm_id = 26150791)
FROM users u, categories c WHERE u.email = 'seed.taras@example.com' AND c.name = 'Снігоприбиральна техніка'
AND NOT EXISTS (SELECT 1 FROM equipment_photos WHERE storage_key = 'seed/equipment-035.png');
INSERT INTO equipment_photos (equipment_id, storage_key, position, created_at)
SELECT MAX(e.id), 'seed/equipment-035.png', 0, CURRENT_TIMESTAMP FROM equipment e JOIN users u ON u.id = e.owner_id
WHERE e.title = 'Снігоприбирач бензиновий Honda HSS 655' AND u.email = 'seed.taras@example.com'
AND NOT EXISTS (SELECT 1 FROM equipment_photos WHERE storage_key = 'seed/equipment-035.png')
HAVING MAX(e.id) IS NOT NULL;
INSERT INTO equipment_photos (equipment_id, storage_key, position, created_at)
SELECT MAX(e.id), 'seed/equipment-035-2.png', 1, CURRENT_TIMESTAMP FROM equipment e JOIN users u ON u.id = e.owner_id
WHERE e.title = 'Снігоприбирач бензиновий Honda HSS 655' AND u.email = 'seed.taras@example.com'
AND NOT EXISTS (SELECT 1 FROM equipment_photos WHERE storage_key = 'seed/equipment-035-2.png')
HAVING MAX(e.id) IS NOT NULL;

-- 036. Парогенератор Kärcher SC 4
INSERT INTO equipment (owner_id, category_id, title, description, item_condition, price_per_day, quantity, min_days, max_days, pickup_available, delivery_available, delivery_note, address, city, lat, lon, status, created_at, updated_at, settlement_id)
SELECT u.id, c.id, 'Парогенератор Kärcher SC 4', 'Акуратне використання, без пошкоджень. Перед видачею чищу й перевіряю.', 'EXCELLENT', 600.00, 1, 1, NULL, TRUE, FALSE, NULL, 'вул. Бандери, 49', 'Львів', 49.826992, 24.056333, 'ACTIVE', CURRENT_TIMESTAMP - INTERVAL '26' DAY, CURRENT_TIMESTAMP - INTERVAL '26' DAY, (SELECT id FROM settlements WHERE osm_id = 26150791)
FROM users u, categories c WHERE u.email = 'seed.olena@example.com' AND c.name = 'Клінінгове обладнання'
AND NOT EXISTS (SELECT 1 FROM equipment_photos WHERE storage_key = 'seed/equipment-036.png');
INSERT INTO equipment_photos (equipment_id, storage_key, position, created_at)
SELECT MAX(e.id), 'seed/equipment-036.png', 0, CURRENT_TIMESTAMP FROM equipment e JOIN users u ON u.id = e.owner_id
WHERE e.title = 'Парогенератор Kärcher SC 4' AND u.email = 'seed.olena@example.com'
AND NOT EXISTS (SELECT 1 FROM equipment_photos WHERE storage_key = 'seed/equipment-036.png')
HAVING MAX(e.id) IS NOT NULL;

-- 037. Дрон DJI Mini 4 Pro
INSERT INTO equipment (owner_id, category_id, title, description, item_condition, price_per_day, quantity, min_days, max_days, pickup_available, delivery_available, delivery_note, address, city, lat, lon, status, created_at, updated_at, settlement_id)
SELECT u.id, c.id, 'Дрон DJI Mini 4 Pro', 'Надійний і простий у використанні. Повернути прошу в чистому вигляді.', 'GOOD', 1210.00, 1, 1, NULL, TRUE, FALSE, NULL, 'просп. Червоної Калини, 110', 'Львів', 49.799988, 24.094835, 'ACTIVE', CURRENT_TIMESTAMP - INTERVAL '35' DAY, CURRENT_TIMESTAMP - INTERVAL '35' DAY, (SELECT id FROM settlements WHERE osm_id = 26150791)
FROM users u, categories c WHERE u.email = 'seed.olena@example.com' AND c.name = 'Фото- та відеотехніка'
AND NOT EXISTS (SELECT 1 FROM equipment_photos WHERE storage_key = 'seed/equipment-037.png');
INSERT INTO equipment_photos (equipment_id, storage_key, position, created_at)
SELECT MAX(e.id), 'seed/equipment-037.png', 0, CURRENT_TIMESTAMP FROM equipment e JOIN users u ON u.id = e.owner_id
WHERE e.title = 'Дрон DJI Mini 4 Pro' AND u.email = 'seed.olena@example.com'
AND NOT EXISTS (SELECT 1 FROM equipment_photos WHERE storage_key = 'seed/equipment-037.png')
HAVING MAX(e.id) IS NOT NULL;

-- 038. Проєктор Epson EB-X49
INSERT INTO equipment (owner_id, category_id, title, description, item_condition, price_per_day, quantity, min_days, max_days, pickup_available, delivery_available, delivery_note, address, city, lat, lon, status, created_at, updated_at, settlement_id)
SELECT u.id, c.id, 'Проєктор Epson EB-X49', 'У хорошому стані, регулярно обслуговується. Можу підказати, як найкраще використати.', 'EXCELLENT', 830.00, 1, 1, 7, TRUE, FALSE, NULL, 'вул. Центральна, 51', 'Пустомити', 49.711110, 23.912050, 'ACTIVE', CURRENT_TIMESTAMP - INTERVAL '39' DAY, CURRENT_TIMESTAMP - INTERVAL '39' DAY, (SELECT id FROM settlements WHERE osm_id = 337547925)
FROM users u, categories c WHERE u.email = 'seed.fotozona@example.com' AND c.name = 'Світло та звук'
AND NOT EXISTS (SELECT 1 FROM equipment_photos WHERE storage_key = 'seed/equipment-038.png');
INSERT INTO equipment_photos (equipment_id, storage_key, position, created_at)
SELECT MAX(e.id), 'seed/equipment-038.png', 0, CURRENT_TIMESTAMP FROM equipment e JOIN users u ON u.id = e.owner_id
WHERE e.title = 'Проєктор Epson EB-X49' AND u.email = 'seed.fotozona@example.com'
AND NOT EXISTS (SELECT 1 FROM equipment_photos WHERE storage_key = 'seed/equipment-038.png')
HAVING MAX(e.id) IS NOT NULL;

-- 039. SUP-дошка 320 см
INSERT INTO equipment (owner_id, category_id, title, description, item_condition, price_per_day, quantity, min_days, max_days, pickup_available, delivery_available, delivery_note, address, city, lat, lon, status, created_at, updated_at, settlement_id)
SELECT u.id, c.id, 'SUP-дошка 320 см', 'Підходить для домашніх і професійних робіт. Видаю з кейсом і комплектом аксесуарів.', 'GOOD', 220.00, 1, 1, 7, TRUE, TRUE, 'Доставка по Львову, вартість за домовленістю', 'вул. Сихівська, 16', 'Львів', 49.862464, 24.024695, 'ACTIVE', CURRENT_TIMESTAMP - INTERVAL '23' DAY, CURRENT_TIMESTAMP - INTERVAL '23' DAY, (SELECT id FROM settlements WHERE osm_id = 26150791)
FROM users u, categories c WHERE u.email = 'seed.andrii@example.com' AND c.name = 'Туристичне спорядження'
AND NOT EXISTS (SELECT 1 FROM equipment_photos WHERE storage_key = 'seed/equipment-039.png');
INSERT INTO equipment_photos (equipment_id, storage_key, position, created_at)
SELECT MAX(e.id), 'seed/equipment-039.png', 0, CURRENT_TIMESTAMP FROM equipment e JOIN users u ON u.id = e.owner_id
WHERE e.title = 'SUP-дошка 320 см' AND u.email = 'seed.andrii@example.com'
AND NOT EXISTS (SELECT 1 FROM equipment_photos WHERE storage_key = 'seed/equipment-039.png')
HAVING MAX(e.id) IS NOT NULL;

-- 040. Генератор дизельний 10 кВт
INSERT INTO equipment (owner_id, category_id, title, description, item_condition, price_per_day, quantity, min_days, max_days, pickup_available, delivery_available, delivery_note, address, city, lat, lon, status, created_at, updated_at, settlement_id)
SELECT u.id, c.id, 'Генератор дизельний 10 кВт', 'У хорошому стані, регулярно обслуговується. Можу підказати, як найкраще використати.', 'EXCELLENT', 2730.00, 1, 1, 14, TRUE, TRUE, 'Доставка по Львову, вартість за домовленістю', 'вул. Кульпарківська, 21', 'Львів', 49.857792, 24.083855, 'ACTIVE', CURRENT_TIMESTAMP - INTERVAL '41' DAY, CURRENT_TIMESTAMP - INTERVAL '41' DAY, (SELECT id FROM settlements WHERE osm_id = 26150791)
FROM users u, categories c WHERE u.email = 'seed.budprokat@example.com' AND c.name = 'Спецтехніка'
AND NOT EXISTS (SELECT 1 FROM equipment_photos WHERE storage_key = 'seed/equipment-040.png');
INSERT INTO equipment_photos (equipment_id, storage_key, position, created_at)
SELECT MAX(e.id), 'seed/equipment-040.png', 0, CURRENT_TIMESTAMP FROM equipment e JOIN users u ON u.id = e.owner_id
WHERE e.title = 'Генератор дизельний 10 кВт' AND u.email = 'seed.budprokat@example.com'
AND NOT EXISTS (SELECT 1 FROM equipment_photos WHERE storage_key = 'seed/equipment-040.png')
HAVING MAX(e.id) IS NOT NULL;
INSERT INTO equipment_photos (equipment_id, storage_key, position, created_at)
SELECT MAX(e.id), 'seed/equipment-040-2.png', 1, CURRENT_TIMESTAMP FROM equipment e JOIN users u ON u.id = e.owner_id
WHERE e.title = 'Генератор дизельний 10 кВт' AND u.email = 'seed.budprokat@example.com'
AND NOT EXISTS (SELECT 1 FROM equipment_photos WHERE storage_key = 'seed/equipment-040-2.png')
HAVING MAX(e.id) IS NOT NULL;

-- 041. Дриль ударна Metabo SBE 650
INSERT INTO equipment (owner_id, category_id, title, description, item_condition, price_per_day, quantity, min_days, max_days, pickup_available, delivery_available, delivery_note, address, city, lat, lon, status, created_at, updated_at, settlement_id)
SELECT u.id, c.id, 'Дриль ударна Metabo SBE 650', 'У хорошому стані, регулярно обслуговується. Можу підказати, як найкраще використати.', 'NEW', 260.00, 1, 1, 30, TRUE, TRUE, NULL, 'вул. Мазепи, 4', 'Львів', 49.800404, 24.043118, 'ACTIVE', CURRENT_TIMESTAMP - INTERVAL '23' DAY, CURRENT_TIMESTAMP - INTERVAL '23' DAY, (SELECT id FROM settlements WHERE osm_id = 26150791)
FROM users u, categories c WHERE u.email = 'seed.marta@example.com' AND c.name = 'Електроінструмент'
AND NOT EXISTS (SELECT 1 FROM equipment_photos WHERE storage_key = 'seed/equipment-041.png');
INSERT INTO equipment_photos (equipment_id, storage_key, position, created_at)
SELECT MAX(e.id), 'seed/equipment-041.png', 0, CURRENT_TIMESTAMP FROM equipment e JOIN users u ON u.id = e.owner_id
WHERE e.title = 'Дриль ударна Metabo SBE 650' AND u.email = 'seed.marta@example.com'
AND NOT EXISTS (SELECT 1 FROM equipment_photos WHERE storage_key = 'seed/equipment-041.png')
HAVING MAX(e.id) IS NOT NULL;

-- 042. Лазерний нівелір Bosch GLL 3-80
INSERT INTO equipment (owner_id, category_id, title, description, item_condition, price_per_day, quantity, min_days, max_days, pickup_available, delivery_available, delivery_note, address, city, lat, lon, status, created_at, updated_at, settlement_id)
SELECT u.id, c.id, 'Лазерний нівелір Bosch GLL 3-80', 'Підходить для домашніх і професійних робіт. Видаю з кейсом і комплектом аксесуарів.', 'GOOD', 770.00, 1, 1, 7, TRUE, FALSE, NULL, 'вул. Шевченка, 41', 'Львів', 49.866985, 24.024311, 'ACTIVE', CURRENT_TIMESTAMP - INTERVAL '44' DAY, CURRENT_TIMESTAMP - INTERVAL '44' DAY, (SELECT id FROM settlements WHERE osm_id = 26150791)
FROM users u, categories c WHERE u.email = 'seed.budprokat@example.com' AND c.name = 'Будівельне обладнання'
AND NOT EXISTS (SELECT 1 FROM equipment_photos WHERE storage_key = 'seed/equipment-042.png');
INSERT INTO equipment_photos (equipment_id, storage_key, position, created_at)
SELECT MAX(e.id), 'seed/equipment-042.png', 0, CURRENT_TIMESTAMP FROM equipment e JOIN users u ON u.id = e.owner_id
WHERE e.title = 'Лазерний нівелір Bosch GLL 3-80' AND u.email = 'seed.budprokat@example.com'
AND NOT EXISTS (SELECT 1 FROM equipment_photos WHERE storage_key = 'seed/equipment-042.png')
HAVING MAX(e.id) IS NOT NULL;

-- 043. Вишка-тура 6 м
INSERT INTO equipment (owner_id, category_id, title, description, item_condition, price_per_day, quantity, min_days, max_days, pickup_available, delivery_available, delivery_note, address, city, lat, lon, status, created_at, updated_at, settlement_id)
SELECT u.id, c.id, 'Вишка-тура 6 м', 'Акуратне використання, без пошкоджень. Перед видачею чищу й перевіряю.', 'GOOD', 170.00, 3, 3, 7, TRUE, TRUE, 'Доставка по Львову та області, ціну узгодимо', 'вул. Княгині Ольги, 112', 'Львів', 49.860198, 24.014597, 'ACTIVE', CURRENT_TIMESTAMP - INTERVAL '27' DAY, CURRENT_TIMESTAMP - INTERVAL '27' DAY, (SELECT id FROM settlements WHERE osm_id = 26150791)
FROM users u, categories c WHERE u.email = 'seed.marta@example.com' AND c.name = 'Риштування та драбини'
AND NOT EXISTS (SELECT 1 FROM equipment_photos WHERE storage_key = 'seed/equipment-043.png');
INSERT INTO equipment_photos (equipment_id, storage_key, position, created_at)
SELECT MAX(e.id), 'seed/equipment-043.png', 0, CURRENT_TIMESTAMP FROM equipment e JOIN users u ON u.id = e.owner_id
WHERE e.title = 'Вишка-тура 6 м' AND u.email = 'seed.marta@example.com'
AND NOT EXISTS (SELECT 1 FROM equipment_photos WHERE storage_key = 'seed/equipment-043.png')
HAVING MAX(e.id) IS NOT NULL;

-- 044. Повітродувка Makita UB1103
INSERT INTO equipment (owner_id, category_id, title, description, item_condition, price_per_day, quantity, min_days, max_days, pickup_available, delivery_available, delivery_note, address, city, lat, lon, status, created_at, updated_at, settlement_id)
SELECT u.id, c.id, 'Повітродувка Makita UB1103', 'Надійний і простий у використанні. Повернути прошу в чистому вигляді.', 'GOOD', 560.00, 3, 3, 14, TRUE, FALSE, NULL, 'вул. Кульпарківська, 35', 'Львів', 49.845320, 23.997189, 'ACTIVE', CURRENT_TIMESTAMP - INTERVAL '20' DAY, CURRENT_TIMESTAMP - INTERVAL '20' DAY, (SELECT id FROM settlements WHERE osm_id = 26150791)
FROM users u, categories c WHERE u.email = 'seed.marta@example.com' AND c.name = 'Садова техніка'
AND NOT EXISTS (SELECT 1 FROM equipment_photos WHERE storage_key = 'seed/equipment-044.png');
INSERT INTO equipment_photos (equipment_id, storage_key, position, created_at)
SELECT MAX(e.id), 'seed/equipment-044.png', 0, CURRENT_TIMESTAMP FROM equipment e JOIN users u ON u.id = e.owner_id
WHERE e.title = 'Повітродувка Makita UB1103' AND u.email = 'seed.marta@example.com'
AND NOT EXISTS (SELECT 1 FROM equipment_photos WHERE storage_key = 'seed/equipment-044.png')
HAVING MAX(e.id) IS NOT NULL;

-- 045. Снігоприбирач електричний Einhell
INSERT INTO equipment (owner_id, category_id, title, description, item_condition, price_per_day, quantity, min_days, max_days, pickup_available, delivery_available, delivery_note, address, city, lat, lon, status, created_at, updated_at, settlement_id)
SELECT u.id, c.id, 'Снігоприбирач електричний Einhell', 'Надійний і простий у використанні. Повернути прошу в чистому вигляді.', 'EXCELLENT', 620.00, 1, 2, 30, TRUE, TRUE, 'Доставка по Львову та області, ціну узгодимо', 'вул. Варшавська, 56', 'Львів', 49.844653, 23.963693, 'ACTIVE', CURRENT_TIMESTAMP - INTERVAL '32' DAY, CURRENT_TIMESTAMP - INTERVAL '32' DAY, (SELECT id FROM settlements WHERE osm_id = 26150791)
FROM users u, categories c WHERE u.email = 'seed.taras@example.com' AND c.name = 'Снігоприбиральна техніка'
AND NOT EXISTS (SELECT 1 FROM equipment_photos WHERE storage_key = 'seed/equipment-045.png');
INSERT INTO equipment_photos (equipment_id, storage_key, position, created_at)
SELECT MAX(e.id), 'seed/equipment-045.png', 0, CURRENT_TIMESTAMP FROM equipment e JOIN users u ON u.id = e.owner_id
WHERE e.title = 'Снігоприбирач електричний Einhell' AND u.email = 'seed.taras@example.com'
AND NOT EXISTS (SELECT 1 FROM equipment_photos WHERE storage_key = 'seed/equipment-045.png')
HAVING MAX(e.id) IS NOT NULL;
INSERT INTO equipment_photos (equipment_id, storage_key, position, created_at)
SELECT MAX(e.id), 'seed/equipment-045-2.png', 1, CURRENT_TIMESTAMP FROM equipment e JOIN users u ON u.id = e.owner_id
WHERE e.title = 'Снігоприбирач електричний Einhell' AND u.email = 'seed.taras@example.com'
AND NOT EXISTS (SELECT 1 FROM equipment_photos WHERE storage_key = 'seed/equipment-045-2.png')
HAVING MAX(e.id) IS NOT NULL;

-- 046. Поломийна машина Kärcher BR 30/4
INSERT INTO equipment (owner_id, category_id, title, description, item_condition, price_per_day, quantity, min_days, max_days, pickup_available, delivery_available, delivery_note, address, city, lat, lon, status, created_at, updated_at, settlement_id)
SELECT u.id, c.id, 'Поломийна машина Kärcher BR 30/4', 'Надійний і простий у використанні. Повернути прошу в чистому вигляді.', 'GOOD', 290.00, 1, 1, 7, TRUE, FALSE, NULL, 'вул. Наукова, 29', 'Львів', 49.879647, 24.046235, 'ACTIVE', CURRENT_TIMESTAMP - INTERVAL '38' DAY, CURRENT_TIMESTAMP - INTERVAL '38' DAY, (SELECT id FROM settlements WHERE osm_id = 26150791)
FROM users u, categories c WHERE u.email = 'seed.marta@example.com' AND c.name = 'Клінінгове обладнання'
AND NOT EXISTS (SELECT 1 FROM equipment_photos WHERE storage_key = 'seed/equipment-046.png');
INSERT INTO equipment_photos (equipment_id, storage_key, position, created_at)
SELECT MAX(e.id), 'seed/equipment-046.png', 0, CURRENT_TIMESTAMP FROM equipment e JOIN users u ON u.id = e.owner_id
WHERE e.title = 'Поломийна машина Kärcher BR 30/4' AND u.email = 'seed.marta@example.com'
AND NOT EXISTS (SELECT 1 FROM equipment_photos WHERE storage_key = 'seed/equipment-046.png')
HAVING MAX(e.id) IS NOT NULL;

-- 047. Стабілізатор DJI RS 3
INSERT INTO equipment (owner_id, category_id, title, description, item_condition, price_per_day, quantity, min_days, max_days, pickup_available, delivery_available, delivery_note, address, city, lat, lon, status, created_at, updated_at, settlement_id)
SELECT u.id, c.id, 'Стабілізатор DJI RS 3', 'Акуратне використання, без пошкоджень. Перед видачею чищу й перевіряю.', 'EXCELLENT', 310.00, 1, 1, 14, TRUE, FALSE, NULL, 'вул. Мазепи, 8', 'Львів', 49.839102, 24.041045, 'ACTIVE', CURRENT_TIMESTAMP - INTERVAL '6' DAY, CURRENT_TIMESTAMP - INTERVAL '6' DAY, (SELECT id FROM settlements WHERE osm_id = 26150791)
FROM users u, categories c WHERE u.email = 'seed.fotozona@example.com' AND c.name = 'Фото- та відеотехніка'
AND NOT EXISTS (SELECT 1 FROM equipment_photos WHERE storage_key = 'seed/equipment-047.png');
INSERT INTO equipment_photos (equipment_id, storage_key, position, created_at)
SELECT MAX(e.id), 'seed/equipment-047.png', 0, CURRENT_TIMESTAMP FROM equipment e JOIN users u ON u.id = e.owner_id
WHERE e.title = 'Стабілізатор DJI RS 3' AND u.email = 'seed.fotozona@example.com'
AND NOT EXISTS (SELECT 1 FROM equipment_photos WHERE storage_key = 'seed/equipment-047.png')
HAVING MAX(e.id) IS NOT NULL;

-- 048. Мікшерний пульт Yamaha MG10XU
INSERT INTO equipment (owner_id, category_id, title, description, item_condition, price_per_day, quantity, min_days, max_days, pickup_available, delivery_available, delivery_note, address, city, lat, lon, status, created_at, updated_at, settlement_id)
SELECT u.id, c.id, 'Мікшерний пульт Yamaha MG10XU', 'У хорошому стані, регулярно обслуговується. Можу підказати, як найкраще використати.', 'EXCELLENT', 900.00, 1, 1, 7, TRUE, FALSE, NULL, 'вул. Бандери, 51', 'Львів', 49.877765, 24.013714, 'ACTIVE', CURRENT_TIMESTAMP - INTERVAL '50' DAY, CURRENT_TIMESTAMP - INTERVAL '50' DAY, (SELECT id FROM settlements WHERE osm_id = 26150791)
FROM users u, categories c WHERE u.email = 'seed.fotozona@example.com' AND c.name = 'Світло та звук'
AND NOT EXISTS (SELECT 1 FROM equipment_photos WHERE storage_key = 'seed/equipment-048.png');
INSERT INTO equipment_photos (equipment_id, storage_key, position, created_at)
SELECT MAX(e.id), 'seed/equipment-048.png', 0, CURRENT_TIMESTAMP FROM equipment e JOIN users u ON u.id = e.owner_id
WHERE e.title = 'Мікшерний пульт Yamaha MG10XU' AND u.email = 'seed.fotozona@example.com'
AND NOT EXISTS (SELECT 1 FROM equipment_photos WHERE storage_key = 'seed/equipment-048.png')
HAVING MAX(e.id) IS NOT NULL;

-- 049. Рюкзак 70 л Deuter
INSERT INTO equipment (owner_id, category_id, title, description, item_condition, price_per_day, quantity, min_days, max_days, pickup_available, delivery_available, delivery_note, address, city, lat, lon, status, created_at, updated_at, settlement_id)
SELECT u.id, c.id, 'Рюкзак 70 л Deuter', 'Акуратне використання, без пошкоджень. Перед видачею чищу й перевіряю.', 'GOOD', 500.00, 3, 2, 7, TRUE, FALSE, NULL, 'вул. Замарстинівська, 99', 'Львів', 49.840507, 24.095880, 'ACTIVE', CURRENT_TIMESTAMP - INTERVAL '3' DAY, CURRENT_TIMESTAMP - INTERVAL '3' DAY, (SELECT id FROM settlements WHERE osm_id = 26150791)
FROM users u, categories c WHERE u.email = 'seed.olena@example.com' AND c.name = 'Туристичне спорядження'
AND NOT EXISTS (SELECT 1 FROM equipment_photos WHERE storage_key = 'seed/equipment-049.png');
INSERT INTO equipment_photos (equipment_id, storage_key, position, created_at)
SELECT MAX(e.id), 'seed/equipment-049.png', 0, CURRENT_TIMESTAMP FROM equipment e JOIN users u ON u.id = e.owner_id
WHERE e.title = 'Рюкзак 70 л Deuter' AND u.email = 'seed.olena@example.com'
AND NOT EXISTS (SELECT 1 FROM equipment_photos WHERE storage_key = 'seed/equipment-049.png')
HAVING MAX(e.id) IS NOT NULL;

-- 050. Причіп легковий 750 кг
INSERT INTO equipment (owner_id, category_id, title, description, item_condition, price_per_day, quantity, min_days, max_days, pickup_available, delivery_available, delivery_note, address, city, lat, lon, status, created_at, updated_at, settlement_id)
SELECT u.id, c.id, 'Причіп легковий 750 кг', 'Надійний і простий у використанні. Повернути прошу в чистому вигляді.', 'GOOD', 3240.00, 1, 3, 7, TRUE, FALSE, NULL, 'вул. Липинського, 40', 'Львів', 49.823989, 24.010786, 'ACTIVE', CURRENT_TIMESTAMP - INTERVAL '3' DAY, CURRENT_TIMESTAMP - INTERVAL '3' DAY, (SELECT id FROM settlements WHERE osm_id = 26150791)
FROM users u, categories c WHERE u.email = 'seed.budprokat@example.com' AND c.name = 'Спецтехніка'
AND NOT EXISTS (SELECT 1 FROM equipment_photos WHERE storage_key = 'seed/equipment-050.png');
INSERT INTO equipment_photos (equipment_id, storage_key, position, created_at)
SELECT MAX(e.id), 'seed/equipment-050.png', 0, CURRENT_TIMESTAMP FROM equipment e JOIN users u ON u.id = e.owner_id
WHERE e.title = 'Причіп легковий 750 кг' AND u.email = 'seed.budprokat@example.com'
AND NOT EXISTS (SELECT 1 FROM equipment_photos WHERE storage_key = 'seed/equipment-050.png')
HAVING MAX(e.id) IS NOT NULL;
INSERT INTO equipment_photos (equipment_id, storage_key, position, created_at)
SELECT MAX(e.id), 'seed/equipment-050-2.png', 1, CURRENT_TIMESTAMP FROM equipment e JOIN users u ON u.id = e.owner_id
WHERE e.title = 'Причіп легковий 750 кг' AND u.email = 'seed.budprokat@example.com'
AND NOT EXISTS (SELECT 1 FROM equipment_photos WHERE storage_key = 'seed/equipment-050-2.png')
HAVING MAX(e.id) IS NOT NULL;

-- 051. Циркулярна пила Makita HS7601
INSERT INTO equipment (owner_id, category_id, title, description, item_condition, price_per_day, quantity, min_days, max_days, pickup_available, delivery_available, delivery_note, address, city, lat, lon, status, created_at, updated_at, settlement_id)
SELECT u.id, c.id, 'Циркулярна пила Makita HS7601', 'Справний, перевірений перед кожною орендою. Видаю з інструкцією та всім потрібним для роботи.', 'GOOD', 170.00, 1, 1, NULL, TRUE, FALSE, NULL, 'вул. Мазепи, 112', 'Львів', 49.796433, 24.046519, 'ACTIVE', CURRENT_TIMESTAMP - INTERVAL '36' DAY, CURRENT_TIMESTAMP - INTERVAL '36' DAY, (SELECT id FROM settlements WHERE osm_id = 26150791)
FROM users u, categories c WHERE u.email = 'seed.taras@example.com' AND c.name = 'Електроінструмент'
AND NOT EXISTS (SELECT 1 FROM equipment_photos WHERE storage_key = 'seed/equipment-051.png');
INSERT INTO equipment_photos (equipment_id, storage_key, position, created_at)
SELECT MAX(e.id), 'seed/equipment-051.png', 0, CURRENT_TIMESTAMP FROM equipment e JOIN users u ON u.id = e.owner_id
WHERE e.title = 'Циркулярна пила Makita HS7601' AND u.email = 'seed.taras@example.com'
AND NOT EXISTS (SELECT 1 FROM equipment_photos WHERE storage_key = 'seed/equipment-051.png')
HAVING MAX(e.id) IS NOT NULL;

-- 052. Компресор 50 л
INSERT INTO equipment (owner_id, category_id, title, description, item_condition, price_per_day, quantity, min_days, max_days, pickup_available, delivery_available, delivery_note, address, city, lat, lon, status, created_at, updated_at, settlement_id)
SELECT u.id, c.id, 'Компресор 50 л', 'Справний, перевірений перед кожною орендою. Видаю з інструкцією та всім потрібним для роботи.', 'FAIR', 340.00, 1, 1, NULL, TRUE, TRUE, 'Привезу в межах міста за 150 ₴', 'просп. Червоної Калини, 37', 'Львів', 49.849019, 24.062482, 'ACTIVE', CURRENT_TIMESTAMP - INTERVAL '47' DAY, CURRENT_TIMESTAMP - INTERVAL '47' DAY, (SELECT id FROM settlements WHERE osm_id = 26150791)
FROM users u, categories c WHERE u.email = 'seed.budprokat@example.com' AND c.name = 'Будівельне обладнання'
AND NOT EXISTS (SELECT 1 FROM equipment_photos WHERE storage_key = 'seed/equipment-052.png');
INSERT INTO equipment_photos (equipment_id, storage_key, position, created_at)
SELECT MAX(e.id), 'seed/equipment-052.png', 0, CURRENT_TIMESTAMP FROM equipment e JOIN users u ON u.id = e.owner_id
WHERE e.title = 'Компресор 50 л' AND u.email = 'seed.budprokat@example.com'
AND NOT EXISTS (SELECT 1 FROM equipment_photos WHERE storage_key = 'seed/equipment-052.png')
HAVING MAX(e.id) IS NOT NULL;

-- 053. Драбина алюмінієва 3×9 щаблів
INSERT INTO equipment (owner_id, category_id, title, description, item_condition, price_per_day, quantity, min_days, max_days, pickup_available, delivery_available, delivery_note, address, city, lat, lon, status, created_at, updated_at, settlement_id)
SELECT u.id, c.id, 'Драбина алюмінієва 3×9 щаблів', 'Акуратне використання, без пошкоджень. Перед видачею чищу й перевіряю.', 'EXCELLENT', 610.00, 1, 2, NULL, TRUE, FALSE, NULL, 'вул. Центральна, 44', 'Городок', 49.786844, 23.654141, 'ACTIVE', CURRENT_TIMESTAMP - INTERVAL '49' DAY, CURRENT_TIMESTAMP - INTERVAL '49' DAY, (SELECT id FROM settlements WHERE osm_id = 261709042)
FROM users u, categories c WHERE u.email = 'seed.andrii@example.com' AND c.name = 'Риштування та драбини'
AND NOT EXISTS (SELECT 1 FROM equipment_photos WHERE storage_key = 'seed/equipment-053.png');
INSERT INTO equipment_photos (equipment_id, storage_key, position, created_at)
SELECT MAX(e.id), 'seed/equipment-053.png', 0, CURRENT_TIMESTAMP FROM equipment e JOIN users u ON u.id = e.owner_id
WHERE e.title = 'Драбина алюмінієва 3×9 щаблів' AND u.email = 'seed.andrii@example.com'
AND NOT EXISTS (SELECT 1 FROM equipment_photos WHERE storage_key = 'seed/equipment-053.png')
HAVING MAX(e.id) IS NOT NULL;

-- 054. Подрібнювач гілок Einhell
INSERT INTO equipment (owner_id, category_id, title, description, item_condition, price_per_day, quantity, min_days, max_days, pickup_available, delivery_available, delivery_note, address, city, lat, lon, status, created_at, updated_at, settlement_id)
SELECT u.id, c.id, 'Подрібнювач гілок Einhell', 'Підходить для домашніх і професійних робіт. Видаю з кейсом і комплектом аксесуарів.', 'FAIR', 620.00, 1, 1, NULL, TRUE, FALSE, NULL, 'вул. Личаківська, 67', 'Львів', 49.875048, 24.039804, 'ACTIVE', CURRENT_TIMESTAMP - INTERVAL '7' DAY, CURRENT_TIMESTAMP - INTERVAL '7' DAY, (SELECT id FROM settlements WHERE osm_id = 26150791)
FROM users u, categories c WHERE u.email = 'seed.marta@example.com' AND c.name = 'Садова техніка'
AND NOT EXISTS (SELECT 1 FROM equipment_photos WHERE storage_key = 'seed/equipment-054.png');
INSERT INTO equipment_photos (equipment_id, storage_key, position, created_at)
SELECT MAX(e.id), 'seed/equipment-054.png', 0, CURRENT_TIMESTAMP FROM equipment e JOIN users u ON u.id = e.owner_id
WHERE e.title = 'Подрібнювач гілок Einhell' AND u.email = 'seed.marta@example.com'
AND NOT EXISTS (SELECT 1 FROM equipment_photos WHERE storage_key = 'seed/equipment-054.png')
HAVING MAX(e.id) IS NOT NULL;

-- 055. Снігоприбирач акумуляторний Greenworks
INSERT INTO equipment (owner_id, category_id, title, description, item_condition, price_per_day, quantity, min_days, max_days, pickup_available, delivery_available, delivery_note, address, city, lat, lon, status, created_at, updated_at, settlement_id)
SELECT u.id, c.id, 'Снігоприбирач акумуляторний Greenworks', 'Справний, перевірений перед кожною орендою. Видаю з інструкцією та всім потрібним для роботи.', 'EXCELLENT', 660.00, 1, 3, 30, TRUE, TRUE, NULL, 'вул. Шевченка, 105', 'Львів', 49.866239, 24.022726, 'ACTIVE', CURRENT_TIMESTAMP - INTERVAL '8' DAY, CURRENT_TIMESTAMP - INTERVAL '8' DAY, (SELECT id FROM settlements WHERE osm_id = 26150791)
FROM users u, categories c WHERE u.email = 'seed.taras@example.com' AND c.name = 'Снігоприбиральна техніка'
AND NOT EXISTS (SELECT 1 FROM equipment_photos WHERE storage_key = 'seed/equipment-055.png');
INSERT INTO equipment_photos (equipment_id, storage_key, position, created_at)
SELECT MAX(e.id), 'seed/equipment-055.png', 0, CURRENT_TIMESTAMP FROM equipment e JOIN users u ON u.id = e.owner_id
WHERE e.title = 'Снігоприбирач акумуляторний Greenworks' AND u.email = 'seed.taras@example.com'
AND NOT EXISTS (SELECT 1 FROM equipment_photos WHERE storage_key = 'seed/equipment-055.png')
HAVING MAX(e.id) IS NOT NULL;
INSERT INTO equipment_photos (equipment_id, storage_key, position, created_at)
SELECT MAX(e.id), 'seed/equipment-055-2.png', 1, CURRENT_TIMESTAMP FROM equipment e JOIN users u ON u.id = e.owner_id
WHERE e.title = 'Снігоприбирач акумуляторний Greenworks' AND u.email = 'seed.taras@example.com'
AND NOT EXISTS (SELECT 1 FROM equipment_photos WHERE storage_key = 'seed/equipment-055-2.png')
HAVING MAX(e.id) IS NOT NULL;

-- 056. Мийка високого тиску Kärcher K5
INSERT INTO equipment (owner_id, category_id, title, description, item_condition, price_per_day, quantity, min_days, max_days, pickup_available, delivery_available, delivery_note, address, city, lat, lon, status, created_at, updated_at, settlement_id)
SELECT u.id, c.id, 'Мийка високого тиску Kärcher K5', 'Справний, перевірений перед кожною орендою. Видаю з інструкцією та всім потрібним для роботи.', 'GOOD', 220.00, 1, 2, 7, TRUE, TRUE, 'Доставка по Львову, вартість за домовленістю', 'просп. Свободи, 99', 'Львів', 49.848609, 24.003487, 'ACTIVE', CURRENT_TIMESTAMP - INTERVAL '54' DAY, CURRENT_TIMESTAMP - INTERVAL '54' DAY, (SELECT id FROM settlements WHERE osm_id = 26150791)
FROM users u, categories c WHERE u.email = 'seed.andrii@example.com' AND c.name = 'Клінінгове обладнання'
AND NOT EXISTS (SELECT 1 FROM equipment_photos WHERE storage_key = 'seed/equipment-056.png');
INSERT INTO equipment_photos (equipment_id, storage_key, position, created_at)
SELECT MAX(e.id), 'seed/equipment-056.png', 0, CURRENT_TIMESTAMP FROM equipment e JOIN users u ON u.id = e.owner_id
WHERE e.title = 'Мийка високого тиску Kärcher K5' AND u.email = 'seed.andrii@example.com'
AND NOT EXISTS (SELECT 1 FROM equipment_photos WHERE storage_key = 'seed/equipment-056.png')
HAVING MAX(e.id) IS NOT NULL;

-- 057. Екшн-камера GoPro HERO12
INSERT INTO equipment (owner_id, category_id, title, description, item_condition, price_per_day, quantity, min_days, max_days, pickup_available, delivery_available, delivery_note, address, city, lat, lon, status, created_at, updated_at, settlement_id)
SELECT u.id, c.id, 'Екшн-камера GoPro HERO12', 'У хорошому стані, регулярно обслуговується. Можу підказати, як найкраще використати.', 'NEW', 1000.00, 1, 2, 30, TRUE, FALSE, NULL, 'вул. Бандери, 85', 'Львів', 49.812674, 24.011067, 'ACTIVE', CURRENT_TIMESTAMP - INTERVAL '18' DAY, CURRENT_TIMESTAMP - INTERVAL '18' DAY, (SELECT id FROM settlements WHERE osm_id = 26150791)
FROM users u, categories c WHERE u.email = 'seed.fotozona@example.com' AND c.name = 'Фото- та відеотехніка'
AND NOT EXISTS (SELECT 1 FROM equipment_photos WHERE storage_key = 'seed/equipment-057.png');
INSERT INTO equipment_photos (equipment_id, storage_key, position, created_at)
SELECT MAX(e.id), 'seed/equipment-057.png', 0, CURRENT_TIMESTAMP FROM equipment e JOIN users u ON u.id = e.owner_id
WHERE e.title = 'Екшн-камера GoPro HERO12' AND u.email = 'seed.fotozona@example.com'
AND NOT EXISTS (SELECT 1 FROM equipment_photos WHERE storage_key = 'seed/equipment-057.png')
HAVING MAX(e.id) IS NOT NULL;

-- 058. Колонка JBL PartyBox 310
INSERT INTO equipment (owner_id, category_id, title, description, item_condition, price_per_day, quantity, min_days, max_days, pickup_available, delivery_available, delivery_note, address, city, lat, lon, status, created_at, updated_at, settlement_id)
SELECT u.id, c.id, 'Колонка JBL PartyBox 310', 'У хорошому стані, регулярно обслуговується. Можу підказати, як найкраще використати.', 'GOOD', 800.00, 1, 1, 14, TRUE, FALSE, NULL, 'вул. Мазепи, 74', 'Львів', 49.820309, 23.983954, 'ACTIVE', CURRENT_TIMESTAMP - INTERVAL '39' DAY, CURRENT_TIMESTAMP - INTERVAL '39' DAY, (SELECT id FROM settlements WHERE osm_id = 26150791)
FROM users u, categories c WHERE u.email = 'seed.olena@example.com' AND c.name = 'Світло та звук'
AND NOT EXISTS (SELECT 1 FROM equipment_photos WHERE storage_key = 'seed/equipment-058.png');
INSERT INTO equipment_photos (equipment_id, storage_key, position, created_at)
SELECT MAX(e.id), 'seed/equipment-058.png', 0, CURRENT_TIMESTAMP FROM equipment e JOIN users u ON u.id = e.owner_id
WHERE e.title = 'Колонка JBL PartyBox 310' AND u.email = 'seed.olena@example.com'
AND NOT EXISTS (SELECT 1 FROM equipment_photos WHERE storage_key = 'seed/equipment-058.png')
HAVING MAX(e.id) IS NOT NULL;

-- 059. Туристичний пальник з балоном
INSERT INTO equipment (owner_id, category_id, title, description, item_condition, price_per_day, quantity, min_days, max_days, pickup_available, delivery_available, delivery_note, address, city, lat, lon, status, created_at, updated_at, settlement_id)
SELECT u.id, c.id, 'Туристичний пальник з балоном', 'У хорошому стані, регулярно обслуговується. Можу підказати, як найкраще використати.', 'GOOD', 180.00, 1, 3, NULL, TRUE, TRUE, 'Привезу в межах міста за 150 ₴', 'просп. Свободи, 52', 'Львів', 49.814711, 24.002129, 'ACTIVE', CURRENT_TIMESTAMP - INTERVAL '0' DAY, CURRENT_TIMESTAMP - INTERVAL '0' DAY, (SELECT id FROM settlements WHERE osm_id = 26150791)
FROM users u, categories c WHERE u.email = 'seed.taras@example.com' AND c.name = 'Туристичне спорядження'
AND NOT EXISTS (SELECT 1 FROM equipment_photos WHERE storage_key = 'seed/equipment-059.png');
INSERT INTO equipment_photos (equipment_id, storage_key, position, created_at)
SELECT MAX(e.id), 'seed/equipment-059.png', 0, CURRENT_TIMESTAMP FROM equipment e JOIN users u ON u.id = e.owner_id
WHERE e.title = 'Туристичний пальник з балоном' AND u.email = 'seed.taras@example.com'
AND NOT EXISTS (SELECT 1 FROM equipment_photos WHERE storage_key = 'seed/equipment-059.png')
HAVING MAX(e.id) IS NOT NULL;

-- 060. Мініекскаватор 1,5 т
INSERT INTO equipment (owner_id, category_id, title, description, item_condition, price_per_day, quantity, min_days, max_days, pickup_available, delivery_available, delivery_note, address, city, lat, lon, status, created_at, updated_at, settlement_id)
SELECT u.id, c.id, 'Мініекскаватор 1,5 т', 'Справний, перевірений перед кожною орендою. Видаю з інструкцією та всім потрібним для роботи.', 'EXCELLENT', 840.00, 1, 1, 14, TRUE, TRUE, 'Доставка по Львову, вартість за домовленістю', 'вул. Варшавська, 92', 'Львів', 49.808130, 24.049852, 'ACTIVE', CURRENT_TIMESTAMP - INTERVAL '33' DAY, CURRENT_TIMESTAMP - INTERVAL '33' DAY, (SELECT id FROM settlements WHERE osm_id = 26150791)
FROM users u, categories c WHERE u.email = 'seed.budprokat@example.com' AND c.name = 'Спецтехніка'
AND NOT EXISTS (SELECT 1 FROM equipment_photos WHERE storage_key = 'seed/equipment-060.png');
INSERT INTO equipment_photos (equipment_id, storage_key, position, created_at)
SELECT MAX(e.id), 'seed/equipment-060.png', 0, CURRENT_TIMESTAMP FROM equipment e JOIN users u ON u.id = e.owner_id
WHERE e.title = 'Мініекскаватор 1,5 т' AND u.email = 'seed.budprokat@example.com'
AND NOT EXISTS (SELECT 1 FROM equipment_photos WHERE storage_key = 'seed/equipment-060.png')
HAVING MAX(e.id) IS NOT NULL;
INSERT INTO equipment_photos (equipment_id, storage_key, position, created_at)
SELECT MAX(e.id), 'seed/equipment-060-2.png', 1, CURRENT_TIMESTAMP FROM equipment e JOIN users u ON u.id = e.owner_id
WHERE e.title = 'Мініекскаватор 1,5 т' AND u.email = 'seed.budprokat@example.com'
AND NOT EXISTS (SELECT 1 FROM equipment_photos WHERE storage_key = 'seed/equipment-060-2.png')
HAVING MAX(e.id) IS NOT NULL;

-- 061. Фрезер Bosch POF 1400
INSERT INTO equipment (owner_id, category_id, title, description, item_condition, price_per_day, quantity, min_days, max_days, pickup_available, delivery_available, delivery_note, address, city, lat, lon, status, created_at, updated_at, settlement_id)
SELECT u.id, c.id, 'Фрезер Bosch POF 1400', 'У хорошому стані, регулярно обслуговується. Можу підказати, як найкраще використати.', 'FAIR', 370.00, 1, 1, NULL, TRUE, FALSE, NULL, 'вул. Сихівська, 13', 'Львів', 49.803131, 23.984759, 'ACTIVE', CURRENT_TIMESTAMP - INTERVAL '29' DAY, CURRENT_TIMESTAMP - INTERVAL '29' DAY, (SELECT id FROM settlements WHERE osm_id = 26150791)
FROM users u, categories c WHERE u.email = 'seed.marta@example.com' AND c.name = 'Електроінструмент'
AND NOT EXISTS (SELECT 1 FROM equipment_photos WHERE storage_key = 'seed/equipment-061.png');
INSERT INTO equipment_photos (equipment_id, storage_key, position, created_at)
SELECT MAX(e.id), 'seed/equipment-061.png', 0, CURRENT_TIMESTAMP FROM equipment e JOIN users u ON u.id = e.owner_id
WHERE e.title = 'Фрезер Bosch POF 1400' AND u.email = 'seed.marta@example.com'
AND NOT EXISTS (SELECT 1 FROM equipment_photos WHERE storage_key = 'seed/equipment-061.png')
HAVING MAX(e.id) IS NOT NULL;

-- 062. Тепловентилятор 9 кВт
INSERT INTO equipment (owner_id, category_id, title, description, item_condition, price_per_day, quantity, min_days, max_days, pickup_available, delivery_available, delivery_note, address, city, lat, lon, status, created_at, updated_at, settlement_id)
SELECT u.id, c.id, 'Тепловентилятор 9 кВт', 'Надійний і простий у використанні. Повернути прошу в чистому вигляді.', 'GOOD', 790.00, 1, 2, 14, TRUE, TRUE, 'Доставка по Львову та області, ціну узгодимо', 'вул. Личаківська, 66', 'Львів', 49.798245, 23.965243, 'ACTIVE', CURRENT_TIMESTAMP - INTERVAL '49' DAY, CURRENT_TIMESTAMP - INTERVAL '49' DAY, (SELECT id FROM settlements WHERE osm_id = 26150791)
FROM users u, categories c WHERE u.email = 'seed.budprokat@example.com' AND c.name = 'Будівельне обладнання'
AND NOT EXISTS (SELECT 1 FROM equipment_photos WHERE storage_key = 'seed/equipment-062.png');
INSERT INTO equipment_photos (equipment_id, storage_key, position, created_at)
SELECT MAX(e.id), 'seed/equipment-062.png', 0, CURRENT_TIMESTAMP FROM equipment e JOIN users u ON u.id = e.owner_id
WHERE e.title = 'Тепловентилятор 9 кВт' AND u.email = 'seed.budprokat@example.com'
AND NOT EXISTS (SELECT 1 FROM equipment_photos WHERE storage_key = 'seed/equipment-062.png')
HAVING MAX(e.id) IS NOT NULL;

-- 063. Риштування пересувне 4 м
INSERT INTO equipment (owner_id, category_id, title, description, item_condition, price_per_day, quantity, min_days, max_days, pickup_available, delivery_available, delivery_note, address, city, lat, lon, status, created_at, updated_at, settlement_id)
SELECT u.id, c.id, 'Риштування пересувне 4 м', 'У хорошому стані, регулярно обслуговується. Можу підказати, як найкраще використати.', 'NEW', 410.00, 1, 3, 7, TRUE, FALSE, NULL, 'вул. Замарстинівська, 2', 'Львів', 49.859601, 23.975394, 'ACTIVE', CURRENT_TIMESTAMP - INTERVAL '25' DAY, CURRENT_TIMESTAMP - INTERVAL '25' DAY, (SELECT id FROM settlements WHERE osm_id = 26150791)
FROM users u, categories c WHERE u.email = 'seed.taras@example.com' AND c.name = 'Риштування та драбини'
AND NOT EXISTS (SELECT 1 FROM equipment_photos WHERE storage_key = 'seed/equipment-063.png');
INSERT INTO equipment_photos (equipment_id, storage_key, position, created_at)
SELECT MAX(e.id), 'seed/equipment-063.png', 0, CURRENT_TIMESTAMP FROM equipment e JOIN users u ON u.id = e.owner_id
WHERE e.title = 'Риштування пересувне 4 м' AND u.email = 'seed.taras@example.com'
AND NOT EXISTS (SELECT 1 FROM equipment_photos WHERE storage_key = 'seed/equipment-063.png')
HAVING MAX(e.id) IS NOT NULL;

-- 064. Мотобур 52 см³
INSERT INTO equipment (owner_id, category_id, title, description, item_condition, price_per_day, quantity, min_days, max_days, pickup_available, delivery_available, delivery_note, address, city, lat, lon, status, created_at, updated_at, settlement_id)
SELECT u.id, c.id, 'Мотобур 52 см³', 'У хорошому стані, регулярно обслуговується. Можу підказати, як найкраще використати.', 'FAIR', 570.00, 1, 2, 14, TRUE, TRUE, 'Доставка по Львову та області, ціну узгодимо', 'вул. Варшавська, 31', 'Львів', 49.854090, 23.995622, 'ACTIVE', CURRENT_TIMESTAMP - INTERVAL '23' DAY, CURRENT_TIMESTAMP - INTERVAL '23' DAY, (SELECT id FROM settlements WHERE osm_id = 26150791)
FROM users u, categories c WHERE u.email = 'seed.olena@example.com' AND c.name = 'Садова техніка'
AND NOT EXISTS (SELECT 1 FROM equipment_photos WHERE storage_key = 'seed/equipment-064.png');
INSERT INTO equipment_photos (equipment_id, storage_key, position, created_at)
SELECT MAX(e.id), 'seed/equipment-064.png', 0, CURRENT_TIMESTAMP FROM equipment e JOIN users u ON u.id = e.owner_id
WHERE e.title = 'Мотобур 52 см³' AND u.email = 'seed.olena@example.com'
AND NOT EXISTS (SELECT 1 FROM equipment_photos WHERE storage_key = 'seed/equipment-064.png')
HAVING MAX(e.id) IS NOT NULL;

-- 065. Снігоприбирач бензиновий Honda HSS 655
INSERT INTO equipment (owner_id, category_id, title, description, item_condition, price_per_day, quantity, min_days, max_days, pickup_available, delivery_available, delivery_note, address, city, lat, lon, status, created_at, updated_at, settlement_id)
SELECT u.id, c.id, 'Снігоприбирач бензиновий Honda HSS 655', 'Справний, перевірений перед кожною орендою. Видаю з інструкцією та всім потрібним для роботи.', 'NEW', 750.00, 2, 3, 14, TRUE, FALSE, NULL, 'вул. Замарстинівська, 72', 'Львів', 49.828231, 24.008122, 'ARCHIVED', CURRENT_TIMESTAMP - INTERVAL '40' DAY, CURRENT_TIMESTAMP - INTERVAL '40' DAY, (SELECT id FROM settlements WHERE osm_id = 26150791)
FROM users u, categories c WHERE u.email = 'seed.taras@example.com' AND c.name = 'Снігоприбиральна техніка'
AND NOT EXISTS (SELECT 1 FROM equipment_photos WHERE storage_key = 'seed/equipment-065.png');
INSERT INTO equipment_photos (equipment_id, storage_key, position, created_at)
SELECT MAX(e.id), 'seed/equipment-065.png', 0, CURRENT_TIMESTAMP FROM equipment e JOIN users u ON u.id = e.owner_id
WHERE e.title = 'Снігоприбирач бензиновий Honda HSS 655' AND u.email = 'seed.taras@example.com'
AND NOT EXISTS (SELECT 1 FROM equipment_photos WHERE storage_key = 'seed/equipment-065.png')
HAVING MAX(e.id) IS NOT NULL;
INSERT INTO equipment_photos (equipment_id, storage_key, position, created_at)
SELECT MAX(e.id), 'seed/equipment-065-2.png', 1, CURRENT_TIMESTAMP FROM equipment e JOIN users u ON u.id = e.owner_id
WHERE e.title = 'Снігоприбирач бензиновий Honda HSS 655' AND u.email = 'seed.taras@example.com'
AND NOT EXISTS (SELECT 1 FROM equipment_photos WHERE storage_key = 'seed/equipment-065-2.png')
HAVING MAX(e.id) IS NOT NULL;

-- 066. Миючий пилосос Kärcher Puzzi 10/1
INSERT INTO equipment (owner_id, category_id, title, description, item_condition, price_per_day, quantity, min_days, max_days, pickup_available, delivery_available, delivery_note, address, city, lat, lon, status, created_at, updated_at, settlement_id)
SELECT u.id, c.id, 'Миючий пилосос Kärcher Puzzi 10/1', 'Справний, перевірений перед кожною орендою. Видаю з інструкцією та всім потрібним для роботи.', 'NEW', 770.00, 1, 1, 14, FALSE, TRUE, NULL, 'вул. Наукова, 56', 'Львів', 49.867555, 23.969505, 'ACTIVE', CURRENT_TIMESTAMP - INTERVAL '23' DAY, CURRENT_TIMESTAMP - INTERVAL '23' DAY, (SELECT id FROM settlements WHERE osm_id = 26150791)
FROM users u, categories c WHERE u.email = 'seed.taras@example.com' AND c.name = 'Клінінгове обладнання'
AND NOT EXISTS (SELECT 1 FROM equipment_photos WHERE storage_key = 'seed/equipment-066.png');
INSERT INTO equipment_photos (equipment_id, storage_key, position, created_at)
SELECT MAX(e.id), 'seed/equipment-066.png', 0, CURRENT_TIMESTAMP FROM equipment e JOIN users u ON u.id = e.owner_id
WHERE e.title = 'Миючий пилосос Kärcher Puzzi 10/1' AND u.email = 'seed.taras@example.com'
AND NOT EXISTS (SELECT 1 FROM equipment_photos WHERE storage_key = 'seed/equipment-066.png')
HAVING MAX(e.id) IS NOT NULL;

-- 067. Штатив Manfrotto 190
INSERT INTO equipment (owner_id, category_id, title, description, item_condition, price_per_day, quantity, min_days, max_days, pickup_available, delivery_available, delivery_note, address, city, lat, lon, status, created_at, updated_at, settlement_id)
SELECT u.id, c.id, 'Штатив Manfrotto 190', 'У хорошому стані, регулярно обслуговується. Можу підказати, як найкраще використати.', 'GOOD', 550.00, 1, 3, 14, FALSE, TRUE, 'Привезу в межах міста за 150 ₴', 'вул. Шевченка, 3', 'Львів', 49.814629, 24.058266, 'ACTIVE', CURRENT_TIMESTAMP - INTERVAL '28' DAY, CURRENT_TIMESTAMP - INTERVAL '28' DAY, (SELECT id FROM settlements WHERE osm_id = 26150791)
FROM users u, categories c WHERE u.email = 'seed.andrii@example.com' AND c.name = 'Фото- та відеотехніка'
AND NOT EXISTS (SELECT 1 FROM equipment_photos WHERE storage_key = 'seed/equipment-067.png');
INSERT INTO equipment_photos (equipment_id, storage_key, position, created_at)
SELECT MAX(e.id), 'seed/equipment-067.png', 0, CURRENT_TIMESTAMP FROM equipment e JOIN users u ON u.id = e.owner_id
WHERE e.title = 'Штатив Manfrotto 190' AND u.email = 'seed.andrii@example.com'
AND NOT EXISTS (SELECT 1 FROM equipment_photos WHERE storage_key = 'seed/equipment-067.png')
HAVING MAX(e.id) IS NOT NULL;

-- 068. Комплект студійного світла 3×LED
INSERT INTO equipment (owner_id, category_id, title, description, item_condition, price_per_day, quantity, min_days, max_days, pickup_available, delivery_available, delivery_note, address, city, lat, lon, status, created_at, updated_at, settlement_id)
SELECT u.id, c.id, 'Комплект студійного світла 3×LED', 'Надійний і простий у використанні. Повернути прошу в чистому вигляді.', 'FAIR', 1060.00, 1, 3, 14, FALSE, TRUE, 'Привезу в межах міста за 150 ₴', 'вул. Центральна, 4', 'Жовква', 50.064185, 23.980571, 'ACTIVE', CURRENT_TIMESTAMP - INTERVAL '0' DAY, CURRENT_TIMESTAMP - INTERVAL '0' DAY, (SELECT id FROM settlements WHERE osm_id = 260954473)
FROM users u, categories c WHERE u.email = 'seed.fotozona@example.com' AND c.name = 'Світло та звук'
AND NOT EXISTS (SELECT 1 FROM equipment_photos WHERE storage_key = 'seed/equipment-068.png');
INSERT INTO equipment_photos (equipment_id, storage_key, position, created_at)
SELECT MAX(e.id), 'seed/equipment-068.png', 0, CURRENT_TIMESTAMP FROM equipment e JOIN users u ON u.id = e.owner_id
WHERE e.title = 'Комплект студійного світла 3×LED' AND u.email = 'seed.fotozona@example.com'
AND NOT EXISTS (SELECT 1 FROM equipment_photos WHERE storage_key = 'seed/equipment-068.png')
HAVING MAX(e.id) IS NOT NULL;

-- 069. Намет 4-місний Tramp
INSERT INTO equipment (owner_id, category_id, title, description, item_condition, price_per_day, quantity, min_days, max_days, pickup_available, delivery_available, delivery_note, address, city, lat, lon, status, created_at, updated_at, settlement_id)
SELECT u.id, c.id, 'Намет 4-місний Tramp', 'Справний, перевірений перед кожною орендою. Видаю з інструкцією та всім потрібним для роботи.', 'EXCELLENT', 240.00, 1, 3, 7, TRUE, FALSE, NULL, 'вул. Замарстинівська, 117', 'Львів', 49.859998, 24.046314, 'ACTIVE', CURRENT_TIMESTAMP - INTERVAL '42' DAY, CURRENT_TIMESTAMP - INTERVAL '42' DAY, (SELECT id FROM settlements WHERE osm_id = 26150791)
FROM users u, categories c WHERE u.email = 'seed.taras@example.com' AND c.name = 'Туристичне спорядження'
AND NOT EXISTS (SELECT 1 FROM equipment_photos WHERE storage_key = 'seed/equipment-069.png');
INSERT INTO equipment_photos (equipment_id, storage_key, position, created_at)
SELECT MAX(e.id), 'seed/equipment-069.png', 0, CURRENT_TIMESTAMP FROM equipment e JOIN users u ON u.id = e.owner_id
WHERE e.title = 'Намет 4-місний Tramp' AND u.email = 'seed.taras@example.com'
AND NOT EXISTS (SELECT 1 FROM equipment_photos WHERE storage_key = 'seed/equipment-069.png')
HAVING MAX(e.id) IS NOT NULL;

-- 070. Ножичний підйомник 8 м
INSERT INTO equipment (owner_id, category_id, title, description, item_condition, price_per_day, quantity, min_days, max_days, pickup_available, delivery_available, delivery_note, address, city, lat, lon, status, created_at, updated_at, settlement_id)
SELECT u.id, c.id, 'Ножичний підйомник 8 м', 'Надійний і простий у використанні. Повернути прошу в чистому вигляді.', 'GOOD', 3850.00, 1, 2, 14, TRUE, TRUE, 'Привезу в межах міста за 150 ₴', 'вул. Городоцька, 119', 'Львів', 49.859269, 24.012403, 'ACTIVE', CURRENT_TIMESTAMP - INTERVAL '32' DAY, CURRENT_TIMESTAMP - INTERVAL '32' DAY, (SELECT id FROM settlements WHERE osm_id = 26150791)
FROM users u, categories c WHERE u.email = 'seed.budprokat@example.com' AND c.name = 'Спецтехніка'
AND NOT EXISTS (SELECT 1 FROM equipment_photos WHERE storage_key = 'seed/equipment-070.png');
INSERT INTO equipment_photos (equipment_id, storage_key, position, created_at)
SELECT MAX(e.id), 'seed/equipment-070.png', 0, CURRENT_TIMESTAMP FROM equipment e JOIN users u ON u.id = e.owner_id
WHERE e.title = 'Ножичний підйомник 8 м' AND u.email = 'seed.budprokat@example.com'
AND NOT EXISTS (SELECT 1 FROM equipment_photos WHERE storage_key = 'seed/equipment-070.png')
HAVING MAX(e.id) IS NOT NULL;
INSERT INTO equipment_photos (equipment_id, storage_key, position, created_at)
SELECT MAX(e.id), 'seed/equipment-070-2.png', 1, CURRENT_TIMESTAMP FROM equipment e JOIN users u ON u.id = e.owner_id
WHERE e.title = 'Ножичний підйомник 8 м' AND u.email = 'seed.budprokat@example.com'
AND NOT EXISTS (SELECT 1 FROM equipment_photos WHERE storage_key = 'seed/equipment-070-2.png')
HAVING MAX(e.id) IS NOT NULL;

-- 071. Ексцентрикова шліфмашина Festool ETS 125
INSERT INTO equipment (owner_id, category_id, title, description, item_condition, price_per_day, quantity, min_days, max_days, pickup_available, delivery_available, delivery_note, address, city, lat, lon, status, created_at, updated_at, settlement_id)
SELECT u.id, c.id, 'Ексцентрикова шліфмашина Festool ETS 125', 'Надійний і простий у використанні. Повернути прошу в чистому вигляді.', 'EXCELLENT', 320.00, 1, 3, 7, TRUE, FALSE, NULL, 'вул. Княгині Ольги, 76', 'Львів', 49.879498, 23.989910, 'ACTIVE', CURRENT_TIMESTAMP - INTERVAL '25' DAY, CURRENT_TIMESTAMP - INTERVAL '25' DAY, (SELECT id FROM settlements WHERE osm_id = 26150791)
FROM users u, categories c WHERE u.email = 'seed.marta@example.com' AND c.name = 'Електроінструмент'
AND NOT EXISTS (SELECT 1 FROM equipment_photos WHERE storage_key = 'seed/equipment-071.png');
INSERT INTO equipment_photos (equipment_id, storage_key, position, created_at)
SELECT MAX(e.id), 'seed/equipment-071.png', 0, CURRENT_TIMESTAMP FROM equipment e JOIN users u ON u.id = e.owner_id
WHERE e.title = 'Ексцентрикова шліфмашина Festool ETS 125' AND u.email = 'seed.marta@example.com'
AND NOT EXISTS (SELECT 1 FROM equipment_photos WHERE storage_key = 'seed/equipment-071.png')
HAVING MAX(e.id) IS NOT NULL;

-- 072. Штроборіз Makita SG1251
INSERT INTO equipment (owner_id, category_id, title, description, item_condition, price_per_day, quantity, min_days, max_days, pickup_available, delivery_available, delivery_note, address, city, lat, lon, status, created_at, updated_at, settlement_id)
SELECT u.id, c.id, 'Штроборіз Makita SG1251', 'Надійний і простий у використанні. Повернути прошу в чистому вигляді.', 'EXCELLENT', 500.00, 5, 3, 30, FALSE, TRUE, 'Привезу в межах міста за 150 ₴', 'вул. Мазепи, 17', 'Львів', 49.834491, 24.052231, 'ACTIVE', CURRENT_TIMESTAMP - INTERVAL '4' DAY, CURRENT_TIMESTAMP - INTERVAL '4' DAY, (SELECT id FROM settlements WHERE osm_id = 26150791)
FROM users u, categories c WHERE u.email = 'seed.budprokat@example.com' AND c.name = 'Будівельне обладнання'
AND NOT EXISTS (SELECT 1 FROM equipment_photos WHERE storage_key = 'seed/equipment-072.png');
INSERT INTO equipment_photos (equipment_id, storage_key, position, created_at)
SELECT MAX(e.id), 'seed/equipment-072.png', 0, CURRENT_TIMESTAMP FROM equipment e JOIN users u ON u.id = e.owner_id
WHERE e.title = 'Штроборіз Makita SG1251' AND u.email = 'seed.budprokat@example.com'
AND NOT EXISTS (SELECT 1 FROM equipment_photos WHERE storage_key = 'seed/equipment-072.png')
HAVING MAX(e.id) IS NOT NULL;

-- 073. Драбина-трансформер 4×4
INSERT INTO equipment (owner_id, category_id, title, description, item_condition, price_per_day, quantity, min_days, max_days, pickup_available, delivery_available, delivery_note, address, city, lat, lon, status, created_at, updated_at, settlement_id)
SELECT u.id, c.id, 'Драбина-трансформер 4×4', 'Справний, перевірений перед кожною орендою. Видаю з інструкцією та всім потрібним для роботи.', 'GOOD', 600.00, 1, 1, 7, TRUE, TRUE, 'Доставка по Львову та області, ціну узгодимо', 'вул. Володимира Великого, 92', 'Львів', 49.798727, 23.961341, 'ACTIVE', CURRENT_TIMESTAMP - INTERVAL '13' DAY, CURRENT_TIMESTAMP - INTERVAL '13' DAY, (SELECT id FROM settlements WHERE osm_id = 26150791)
FROM users u, categories c WHERE u.email = 'seed.taras@example.com' AND c.name = 'Риштування та драбини'
AND NOT EXISTS (SELECT 1 FROM equipment_photos WHERE storage_key = 'seed/equipment-073.png');
INSERT INTO equipment_photos (equipment_id, storage_key, position, created_at)
SELECT MAX(e.id), 'seed/equipment-073.png', 0, CURRENT_TIMESTAMP FROM equipment e JOIN users u ON u.id = e.owner_id
WHERE e.title = 'Драбина-трансформер 4×4' AND u.email = 'seed.taras@example.com'
AND NOT EXISTS (SELECT 1 FROM equipment_photos WHERE storage_key = 'seed/equipment-073.png')
HAVING MAX(e.id) IS NOT NULL;

-- 074. Аератор газону
INSERT INTO equipment (owner_id, category_id, title, description, item_condition, price_per_day, quantity, min_days, max_days, pickup_available, delivery_available, delivery_note, address, city, lat, lon, status, created_at, updated_at, settlement_id)
SELECT u.id, c.id, 'Аератор газону', 'Підходить для домашніх і професійних робіт. Видаю з кейсом і комплектом аксесуарів.', 'EXCELLENT', 400.00, 2, 1, NULL, TRUE, TRUE, 'Доставка по Львову, вартість за домовленістю', 'вул. Шевченка, 114', 'Львів', 49.840947, 23.966184, 'ACTIVE', CURRENT_TIMESTAMP - INTERVAL '7' DAY, CURRENT_TIMESTAMP - INTERVAL '7' DAY, (SELECT id FROM settlements WHERE osm_id = 26150791)
FROM users u, categories c WHERE u.email = 'seed.marta@example.com' AND c.name = 'Садова техніка'
AND NOT EXISTS (SELECT 1 FROM equipment_photos WHERE storage_key = 'seed/equipment-074.png');
INSERT INTO equipment_photos (equipment_id, storage_key, position, created_at)
SELECT MAX(e.id), 'seed/equipment-074.png', 0, CURRENT_TIMESTAMP FROM equipment e JOIN users u ON u.id = e.owner_id
WHERE e.title = 'Аератор газону' AND u.email = 'seed.marta@example.com'
AND NOT EXISTS (SELECT 1 FROM equipment_photos WHERE storage_key = 'seed/equipment-074.png')
HAVING MAX(e.id) IS NOT NULL;

-- 075. Снігоприбирач електричний Einhell
INSERT INTO equipment (owner_id, category_id, title, description, item_condition, price_per_day, quantity, min_days, max_days, pickup_available, delivery_available, delivery_note, address, city, lat, lon, status, created_at, updated_at, settlement_id)
SELECT u.id, c.id, 'Снігоприбирач електричний Einhell', 'Акуратне використання, без пошкоджень. Перед видачею чищу й перевіряю.', 'NEW', 770.00, 1, 1, 30, TRUE, TRUE, 'Доставка по Львову, вартість за домовленістю', 'вул. Кульпарківська, 89', 'Львів', 49.861972, 23.975669, 'ACTIVE', CURRENT_TIMESTAMP - INTERVAL '51' DAY, CURRENT_TIMESTAMP - INTERVAL '51' DAY, (SELECT id FROM settlements WHERE osm_id = 26150791)
FROM users u, categories c WHERE u.email = 'seed.andrii@example.com' AND c.name = 'Снігоприбиральна техніка'
AND NOT EXISTS (SELECT 1 FROM equipment_photos WHERE storage_key = 'seed/equipment-075.png');
INSERT INTO equipment_photos (equipment_id, storage_key, position, created_at)
SELECT MAX(e.id), 'seed/equipment-075.png', 0, CURRENT_TIMESTAMP FROM equipment e JOIN users u ON u.id = e.owner_id
WHERE e.title = 'Снігоприбирач електричний Einhell' AND u.email = 'seed.andrii@example.com'
AND NOT EXISTS (SELECT 1 FROM equipment_photos WHERE storage_key = 'seed/equipment-075.png')
HAVING MAX(e.id) IS NOT NULL;
INSERT INTO equipment_photos (equipment_id, storage_key, position, created_at)
SELECT MAX(e.id), 'seed/equipment-075-2.png', 1, CURRENT_TIMESTAMP FROM equipment e JOIN users u ON u.id = e.owner_id
WHERE e.title = 'Снігоприбирач електричний Einhell' AND u.email = 'seed.andrii@example.com'
AND NOT EXISTS (SELECT 1 FROM equipment_photos WHERE storage_key = 'seed/equipment-075-2.png')
HAVING MAX(e.id) IS NOT NULL;

-- 076. Будівельний пилосос Makita VC2512L
INSERT INTO equipment (owner_id, category_id, title, description, item_condition, price_per_day, quantity, min_days, max_days, pickup_available, delivery_available, delivery_note, address, city, lat, lon, status, created_at, updated_at, settlement_id)
SELECT u.id, c.id, 'Будівельний пилосос Makita VC2512L', 'Підходить для домашніх і професійних робіт. Видаю з кейсом і комплектом аксесуарів.', 'EXCELLENT', 790.00, 1, 2, 14, FALSE, TRUE, 'Доставка по Львову, вартість за домовленістю', 'вул. Шевченка, 77', 'Львів', 49.840554, 24.062685, 'ACTIVE', CURRENT_TIMESTAMP - INTERVAL '49' DAY, CURRENT_TIMESTAMP - INTERVAL '49' DAY, (SELECT id FROM settlements WHERE osm_id = 26150791)
FROM users u, categories c WHERE u.email = 'seed.marta@example.com' AND c.name = 'Клінінгове обладнання'
AND NOT EXISTS (SELECT 1 FROM equipment_photos WHERE storage_key = 'seed/equipment-076.png');
INSERT INTO equipment_photos (equipment_id, storage_key, position, created_at)
SELECT MAX(e.id), 'seed/equipment-076.png', 0, CURRENT_TIMESTAMP FROM equipment e JOIN users u ON u.id = e.owner_id
WHERE e.title = 'Будівельний пилосос Makita VC2512L' AND u.email = 'seed.marta@example.com'
AND NOT EXISTS (SELECT 1 FROM equipment_photos WHERE storage_key = 'seed/equipment-076.png')
HAVING MAX(e.id) IS NOT NULL;

-- 077. Відеокамера Panasonic HC-X1500
INSERT INTO equipment (owner_id, category_id, title, description, item_condition, price_per_day, quantity, min_days, max_days, pickup_available, delivery_available, delivery_note, address, city, lat, lon, status, created_at, updated_at, settlement_id)
SELECT u.id, c.id, 'Відеокамера Panasonic HC-X1500', 'У хорошому стані, регулярно обслуговується. Можу підказати, як найкраще використати.', 'GOOD', 1380.00, 3, 1, 30, TRUE, TRUE, NULL, 'вул. Мазепи, 12', 'Львів', 49.814992, 23.998441, 'ACTIVE', CURRENT_TIMESTAMP - INTERVAL '14' DAY, CURRENT_TIMESTAMP - INTERVAL '14' DAY, (SELECT id FROM settlements WHERE osm_id = 26150791)
FROM users u, categories c WHERE u.email = 'seed.fotozona@example.com' AND c.name = 'Фото- та відеотехніка'
AND NOT EXISTS (SELECT 1 FROM equipment_photos WHERE storage_key = 'seed/equipment-077.png');
INSERT INTO equipment_photos (equipment_id, storage_key, position, created_at)
SELECT MAX(e.id), 'seed/equipment-077.png', 0, CURRENT_TIMESTAMP FROM equipment e JOIN users u ON u.id = e.owner_id
WHERE e.title = 'Відеокамера Panasonic HC-X1500' AND u.email = 'seed.fotozona@example.com'
AND NOT EXISTS (SELECT 1 FROM equipment_photos WHERE storage_key = 'seed/equipment-077.png')
HAVING MAX(e.id) IS NOT NULL;

-- 078. Радіосистема Shure BLX24
INSERT INTO equipment (owner_id, category_id, title, description, item_condition, price_per_day, quantity, min_days, max_days, pickup_available, delivery_available, delivery_note, address, city, lat, lon, status, created_at, updated_at, settlement_id)
SELECT u.id, c.id, 'Радіосистема Shure BLX24', 'Підходить для домашніх і професійних робіт. Видаю з кейсом і комплектом аксесуарів.', 'EXCELLENT', 390.00, 1, 1, NULL, TRUE, TRUE, 'Привезу в межах міста за 150 ₴', 'вул. Наукова, 55', 'Львів', 49.861427, 24.081517, 'ACTIVE', CURRENT_TIMESTAMP - INTERVAL '4' DAY, CURRENT_TIMESTAMP - INTERVAL '4' DAY, (SELECT id FROM settlements WHERE osm_id = 26150791)
FROM users u, categories c WHERE u.email = 'seed.fotozona@example.com' AND c.name = 'Світло та звук'
AND NOT EXISTS (SELECT 1 FROM equipment_photos WHERE storage_key = 'seed/equipment-078.png');
INSERT INTO equipment_photos (equipment_id, storage_key, position, created_at)
SELECT MAX(e.id), 'seed/equipment-078.png', 0, CURRENT_TIMESTAMP FROM equipment e JOIN users u ON u.id = e.owner_id
WHERE e.title = 'Радіосистема Shure BLX24' AND u.email = 'seed.fotozona@example.com'
AND NOT EXISTS (SELECT 1 FROM equipment_photos WHERE storage_key = 'seed/equipment-078.png')
HAVING MAX(e.id) IS NOT NULL;

-- 079. Каяк надувний двомісний
INSERT INTO equipment (owner_id, category_id, title, description, item_condition, price_per_day, quantity, min_days, max_days, pickup_available, delivery_available, delivery_note, address, city, lat, lon, status, created_at, updated_at, settlement_id)
SELECT u.id, c.id, 'Каяк надувний двомісний', 'Акуратне використання, без пошкоджень. Перед видачею чищу й перевіряю.', 'FAIR', 280.00, 3, 3, 30, TRUE, TRUE, NULL, 'вул. Пасічна, 20', 'Львів', 49.798882, 24.005128, 'ACTIVE', CURRENT_TIMESTAMP - INTERVAL '39' DAY, CURRENT_TIMESTAMP - INTERVAL '39' DAY, (SELECT id FROM settlements WHERE osm_id = 26150791)
FROM users u, categories c WHERE u.email = 'seed.andrii@example.com' AND c.name = 'Туристичне спорядження'
AND NOT EXISTS (SELECT 1 FROM equipment_photos WHERE storage_key = 'seed/equipment-079.png');
INSERT INTO equipment_photos (equipment_id, storage_key, position, created_at)
SELECT MAX(e.id), 'seed/equipment-079.png', 0, CURRENT_TIMESTAMP FROM equipment e JOIN users u ON u.id = e.owner_id
WHERE e.title = 'Каяк надувний двомісний' AND u.email = 'seed.andrii@example.com'
AND NOT EXISTS (SELECT 1 FROM equipment_photos WHERE storage_key = 'seed/equipment-079.png')
HAVING MAX(e.id) IS NOT NULL;

-- 080. Мотопомпа для брудної води
INSERT INTO equipment (owner_id, category_id, title, description, item_condition, price_per_day, quantity, min_days, max_days, pickup_available, delivery_available, delivery_note, address, city, lat, lon, status, created_at, updated_at, settlement_id)
SELECT u.id, c.id, 'Мотопомпа для брудної води', 'Справний, перевірений перед кожною орендою. Видаю з інструкцією та всім потрібним для роботи.', 'GOOD', 2340.00, 1, 2, 14, TRUE, FALSE, NULL, 'вул. Мазепи, 101', 'Львів', 49.835871, 23.997084, 'ACTIVE', CURRENT_TIMESTAMP - INTERVAL '2' DAY, CURRENT_TIMESTAMP - INTERVAL '2' DAY, (SELECT id FROM settlements WHERE osm_id = 26150791)
FROM users u, categories c WHERE u.email = 'seed.budprokat@example.com' AND c.name = 'Спецтехніка'
AND NOT EXISTS (SELECT 1 FROM equipment_photos WHERE storage_key = 'seed/equipment-080.png');
INSERT INTO equipment_photos (equipment_id, storage_key, position, created_at)
SELECT MAX(e.id), 'seed/equipment-080.png', 0, CURRENT_TIMESTAMP FROM equipment e JOIN users u ON u.id = e.owner_id
WHERE e.title = 'Мотопомпа для брудної води' AND u.email = 'seed.budprokat@example.com'
AND NOT EXISTS (SELECT 1 FROM equipment_photos WHERE storage_key = 'seed/equipment-080.png')
HAVING MAX(e.id) IS NOT NULL;
INSERT INTO equipment_photos (equipment_id, storage_key, position, created_at)
SELECT MAX(e.id), 'seed/equipment-080-2.png', 1, CURRENT_TIMESTAMP FROM equipment e JOIN users u ON u.id = e.owner_id
WHERE e.title = 'Мотопомпа для брудної води' AND u.email = 'seed.budprokat@example.com'
AND NOT EXISTS (SELECT 1 FROM equipment_photos WHERE storage_key = 'seed/equipment-080-2.png')
HAVING MAX(e.id) IS NOT NULL;

-- 081. Акумуляторний гайковерт Milwaukee M18
INSERT INTO equipment (owner_id, category_id, title, description, item_condition, price_per_day, quantity, min_days, max_days, pickup_available, delivery_available, delivery_note, address, city, lat, lon, status, created_at, updated_at, settlement_id)
SELECT u.id, c.id, 'Акумуляторний гайковерт Milwaukee M18', 'Акуратне використання, без пошкоджень. Перед видачею чищу й перевіряю.', 'GOOD', 310.00, 1, 1, 7, FALSE, TRUE, 'Привезу в межах міста за 150 ₴', 'вул. Городоцька, 9', 'Львів', 49.869243, 24.032590, 'ACTIVE', CURRENT_TIMESTAMP - INTERVAL '53' DAY, CURRENT_TIMESTAMP - INTERVAL '53' DAY, (SELECT id FROM settlements WHERE osm_id = 26150791)
FROM users u, categories c WHERE u.email = 'seed.andrii@example.com' AND c.name = 'Електроінструмент'
AND NOT EXISTS (SELECT 1 FROM equipment_photos WHERE storage_key = 'seed/equipment-081.png');
INSERT INTO equipment_photos (equipment_id, storage_key, position, created_at)
SELECT MAX(e.id), 'seed/equipment-081.png', 0, CURRENT_TIMESTAMP FROM equipment e JOIN users u ON u.id = e.owner_id
WHERE e.title = 'Акумуляторний гайковерт Milwaukee M18' AND u.email = 'seed.andrii@example.com'
AND NOT EXISTS (SELECT 1 FROM equipment_photos WHERE storage_key = 'seed/equipment-081.png')
HAVING MAX(e.id) IS NOT NULL;

-- 082. Генератор бензиновий 3 кВт
INSERT INTO equipment (owner_id, category_id, title, description, item_condition, price_per_day, quantity, min_days, max_days, pickup_available, delivery_available, delivery_note, address, city, lat, lon, status, created_at, updated_at, settlement_id)
SELECT u.id, c.id, 'Генератор бензиновий 3 кВт', 'Справний, перевірений перед кожною орендою. Видаю з інструкцією та всім потрібним для роботи.', 'FAIR', 270.00, 3, 1, NULL, FALSE, TRUE, 'Привезу в межах міста за 150 ₴', 'вул. Наукова, 72', 'Львів', 49.838224, 24.056159, 'ACTIVE', CURRENT_TIMESTAMP - INTERVAL '20' DAY, CURRENT_TIMESTAMP - INTERVAL '20' DAY, (SELECT id FROM settlements WHERE osm_id = 26150791)
FROM users u, categories c WHERE u.email = 'seed.budprokat@example.com' AND c.name = 'Будівельне обладнання'
AND NOT EXISTS (SELECT 1 FROM equipment_photos WHERE storage_key = 'seed/equipment-082.png');
INSERT INTO equipment_photos (equipment_id, storage_key, position, created_at)
SELECT MAX(e.id), 'seed/equipment-082.png', 0, CURRENT_TIMESTAMP FROM equipment e JOIN users u ON u.id = e.owner_id
WHERE e.title = 'Генератор бензиновий 3 кВт' AND u.email = 'seed.budprokat@example.com'
AND NOT EXISTS (SELECT 1 FROM equipment_photos WHERE storage_key = 'seed/equipment-082.png')
HAVING MAX(e.id) IS NOT NULL;

-- 083. Помост малярний 1,5 м
INSERT INTO equipment (owner_id, category_id, title, description, item_condition, price_per_day, quantity, min_days, max_days, pickup_available, delivery_available, delivery_note, address, city, lat, lon, status, created_at, updated_at, settlement_id)
SELECT u.id, c.id, 'Помост малярний 1,5 м', 'Акуратне використання, без пошкоджень. Перед видачею чищу й перевіряю.', 'EXCELLENT', 220.00, 1, 1, 7, TRUE, FALSE, NULL, 'вул. Центральна, 32', 'Винники', 49.807321, 24.134205, 'ACTIVE', CURRENT_TIMESTAMP - INTERVAL '35' DAY, CURRENT_TIMESTAMP - INTERVAL '35' DAY, (SELECT id FROM settlements WHERE osm_id = 313247858)
FROM users u, categories c WHERE u.email = 'seed.marta@example.com' AND c.name = 'Риштування та драбини'
AND NOT EXISTS (SELECT 1 FROM equipment_photos WHERE storage_key = 'seed/equipment-083.png');
INSERT INTO equipment_photos (equipment_id, storage_key, position, created_at)
SELECT MAX(e.id), 'seed/equipment-083.png', 0, CURRENT_TIMESTAMP FROM equipment e JOIN users u ON u.id = e.owner_id
WHERE e.title = 'Помост малярний 1,5 м' AND u.email = 'seed.marta@example.com'
AND NOT EXISTS (SELECT 1 FROM equipment_photos WHERE storage_key = 'seed/equipment-083.png')
HAVING MAX(e.id) IS NOT NULL;

-- 084. Газонокосарка Husqvarna LC 140
INSERT INTO equipment (owner_id, category_id, title, description, item_condition, price_per_day, quantity, min_days, max_days, pickup_available, delivery_available, delivery_note, address, city, lat, lon, status, created_at, updated_at, settlement_id)
SELECT u.id, c.id, 'Газонокосарка Husqvarna LC 140', 'Акуратне використання, без пошкоджень. Перед видачею чищу й перевіряю.', 'NEW', 680.00, 1, 1, 30, TRUE, FALSE, NULL, 'вул. Наукова, 38', 'Львів', 49.797218, 24.048946, 'ACTIVE', CURRENT_TIMESTAMP - INTERVAL '54' DAY, CURRENT_TIMESTAMP - INTERVAL '54' DAY, (SELECT id FROM settlements WHERE osm_id = 26150791)
FROM users u, categories c WHERE u.email = 'seed.marta@example.com' AND c.name = 'Садова техніка'
AND NOT EXISTS (SELECT 1 FROM equipment_photos WHERE storage_key = 'seed/equipment-084.png');
INSERT INTO equipment_photos (equipment_id, storage_key, position, created_at)
SELECT MAX(e.id), 'seed/equipment-084.png', 0, CURRENT_TIMESTAMP FROM equipment e JOIN users u ON u.id = e.owner_id
WHERE e.title = 'Газонокосарка Husqvarna LC 140' AND u.email = 'seed.marta@example.com'
AND NOT EXISTS (SELECT 1 FROM equipment_photos WHERE storage_key = 'seed/equipment-084.png')
HAVING MAX(e.id) IS NOT NULL;

-- 085. Снігоприбирач акумуляторний Greenworks
INSERT INTO equipment (owner_id, category_id, title, description, item_condition, price_per_day, quantity, min_days, max_days, pickup_available, delivery_available, delivery_note, address, city, lat, lon, status, created_at, updated_at, settlement_id)
SELECT u.id, c.id, 'Снігоприбирач акумуляторний Greenworks', 'Надійний і простий у використанні. Повернути прошу в чистому вигляді.', 'NEW', 350.00, 1, 1, 30, TRUE, FALSE, NULL, 'вул. Шевченка, 108', 'Львів', 49.866784, 23.976876, 'ACTIVE', CURRENT_TIMESTAMP - INTERVAL '60' DAY, CURRENT_TIMESTAMP - INTERVAL '60' DAY, (SELECT id FROM settlements WHERE osm_id = 26150791)
FROM users u, categories c WHERE u.email = 'seed.marta@example.com' AND c.name = 'Снігоприбиральна техніка'
AND NOT EXISTS (SELECT 1 FROM equipment_photos WHERE storage_key = 'seed/equipment-085.png');
INSERT INTO equipment_photos (equipment_id, storage_key, position, created_at)
SELECT MAX(e.id), 'seed/equipment-085.png', 0, CURRENT_TIMESTAMP FROM equipment e JOIN users u ON u.id = e.owner_id
WHERE e.title = 'Снігоприбирач акумуляторний Greenworks' AND u.email = 'seed.marta@example.com'
AND NOT EXISTS (SELECT 1 FROM equipment_photos WHERE storage_key = 'seed/equipment-085.png')
HAVING MAX(e.id) IS NOT NULL;
INSERT INTO equipment_photos (equipment_id, storage_key, position, created_at)
SELECT MAX(e.id), 'seed/equipment-085-2.png', 1, CURRENT_TIMESTAMP FROM equipment e JOIN users u ON u.id = e.owner_id
WHERE e.title = 'Снігоприбирач акумуляторний Greenworks' AND u.email = 'seed.marta@example.com'
AND NOT EXISTS (SELECT 1 FROM equipment_photos WHERE storage_key = 'seed/equipment-085-2.png')
HAVING MAX(e.id) IS NOT NULL;

-- 086. Парогенератор Kärcher SC 4
INSERT INTO equipment (owner_id, category_id, title, description, item_condition, price_per_day, quantity, min_days, max_days, pickup_available, delivery_available, delivery_note, address, city, lat, lon, status, created_at, updated_at, settlement_id)
SELECT u.id, c.id, 'Парогенератор Kärcher SC 4', 'Справний, перевірений перед кожною орендою. Видаю з інструкцією та всім потрібним для роботи.', 'GOOD', 660.00, 1, 1, 7, TRUE, TRUE, 'Привезу в межах міста за 150 ₴', 'просп. Свободи, 111', 'Львів', 49.799696, 24.097236, 'ACTIVE', CURRENT_TIMESTAMP - INTERVAL '9' DAY, CURRENT_TIMESTAMP - INTERVAL '9' DAY, (SELECT id FROM settlements WHERE osm_id = 26150791)
FROM users u, categories c WHERE u.email = 'seed.marta@example.com' AND c.name = 'Клінінгове обладнання'
AND NOT EXISTS (SELECT 1 FROM equipment_photos WHERE storage_key = 'seed/equipment-086.png');
INSERT INTO equipment_photos (equipment_id, storage_key, position, created_at)
SELECT MAX(e.id), 'seed/equipment-086.png', 0, CURRENT_TIMESTAMP FROM equipment e JOIN users u ON u.id = e.owner_id
WHERE e.title = 'Парогенератор Kärcher SC 4' AND u.email = 'seed.marta@example.com'
AND NOT EXISTS (SELECT 1 FROM equipment_photos WHERE storage_key = 'seed/equipment-086.png')
HAVING MAX(e.id) IS NOT NULL;

-- 087. Фотоапарат Sony A7 III
INSERT INTO equipment (owner_id, category_id, title, description, item_condition, price_per_day, quantity, min_days, max_days, pickup_available, delivery_available, delivery_note, address, city, lat, lon, status, created_at, updated_at, settlement_id)
SELECT u.id, c.id, 'Фотоапарат Sony A7 III', 'У хорошому стані, регулярно обслуговується. Можу підказати, як найкраще використати.', 'NEW', 1040.00, 5, 1, 30, TRUE, FALSE, NULL, 'вул. Пасічна, 60', 'Львів', 49.797092, 24.033615, 'ACTIVE', CURRENT_TIMESTAMP - INTERVAL '27' DAY, CURRENT_TIMESTAMP - INTERVAL '27' DAY, (SELECT id FROM settlements WHERE osm_id = 26150791)
FROM users u, categories c WHERE u.email = 'seed.fotozona@example.com' AND c.name = 'Фото- та відеотехніка'
AND NOT EXISTS (SELECT 1 FROM equipment_photos WHERE storage_key = 'seed/equipment-087.png');
INSERT INTO equipment_photos (equipment_id, storage_key, position, created_at)
SELECT MAX(e.id), 'seed/equipment-087.png', 0, CURRENT_TIMESTAMP FROM equipment e JOIN users u ON u.id = e.owner_id
WHERE e.title = 'Фотоапарат Sony A7 III' AND u.email = 'seed.fotozona@example.com'
AND NOT EXISTS (SELECT 1 FROM equipment_photos WHERE storage_key = 'seed/equipment-087.png')
HAVING MAX(e.id) IS NOT NULL;

-- 088. Проєктор Epson EB-X49
INSERT INTO equipment (owner_id, category_id, title, description, item_condition, price_per_day, quantity, min_days, max_days, pickup_available, delivery_available, delivery_note, address, city, lat, lon, status, created_at, updated_at, settlement_id)
SELECT u.id, c.id, 'Проєктор Epson EB-X49', 'Справний, перевірений перед кожною орендою. Видаю з інструкцією та всім потрібним для роботи.', 'GOOD', 700.00, 1, 1, 7, TRUE, TRUE, 'Доставка по Львову та області, ціну узгодимо', 'вул. Виговського, 31', 'Львів', 49.839678, 24.086224, 'ACTIVE', CURRENT_TIMESTAMP - INTERVAL '60' DAY, CURRENT_TIMESTAMP - INTERVAL '60' DAY, (SELECT id FROM settlements WHERE osm_id = 26150791)
FROM users u, categories c WHERE u.email = 'seed.olena@example.com' AND c.name = 'Світло та звук'
AND NOT EXISTS (SELECT 1 FROM equipment_photos WHERE storage_key = 'seed/equipment-088.png');
INSERT INTO equipment_photos (equipment_id, storage_key, position, created_at)
SELECT MAX(e.id), 'seed/equipment-088.png', 0, CURRENT_TIMESTAMP FROM equipment e JOIN users u ON u.id = e.owner_id
WHERE e.title = 'Проєктор Epson EB-X49' AND u.email = 'seed.olena@example.com'
AND NOT EXISTS (SELECT 1 FROM equipment_photos WHERE storage_key = 'seed/equipment-088.png')
HAVING MAX(e.id) IS NOT NULL;

-- 089. Спальник −10 °C
INSERT INTO equipment (owner_id, category_id, title, description, item_condition, price_per_day, quantity, min_days, max_days, pickup_available, delivery_available, delivery_note, address, city, lat, lon, status, created_at, updated_at, settlement_id)
SELECT u.id, c.id, 'Спальник −10 °C', 'Підходить для домашніх і професійних робіт. Видаю з кейсом і комплектом аксесуарів.', 'GOOD', 420.00, 2, 1, NULL, TRUE, TRUE, 'Привезу в межах міста за 150 ₴', 'вул. Володимира Великого, 32', 'Львів', 49.801924, 23.991070, 'ACTIVE', CURRENT_TIMESTAMP - INTERVAL '30' DAY, CURRENT_TIMESTAMP - INTERVAL '30' DAY, (SELECT id FROM settlements WHERE osm_id = 26150791)
FROM users u, categories c WHERE u.email = 'seed.taras@example.com' AND c.name = 'Туристичне спорядження'
AND NOT EXISTS (SELECT 1 FROM equipment_photos WHERE storage_key = 'seed/equipment-089.png');
INSERT INTO equipment_photos (equipment_id, storage_key, position, created_at)
SELECT MAX(e.id), 'seed/equipment-089.png', 0, CURRENT_TIMESTAMP FROM equipment e JOIN users u ON u.id = e.owner_id
WHERE e.title = 'Спальник −10 °C' AND u.email = 'seed.taras@example.com'
AND NOT EXISTS (SELECT 1 FROM equipment_photos WHERE storage_key = 'seed/equipment-089.png')
HAVING MAX(e.id) IS NOT NULL;

-- 090. Генератор дизельний 10 кВт
INSERT INTO equipment (owner_id, category_id, title, description, item_condition, price_per_day, quantity, min_days, max_days, pickup_available, delivery_available, delivery_note, address, city, lat, lon, status, created_at, updated_at, settlement_id)
SELECT u.id, c.id, 'Генератор дизельний 10 кВт', 'Підходить для домашніх і професійних робіт. Видаю з кейсом і комплектом аксесуарів.', 'EXCELLENT', 1600.00, 1, 1, 7, FALSE, TRUE, 'Привезу в межах міста за 150 ₴', 'вул. Зелена, 77', 'Львів', 49.834907, 23.974720, 'ACTIVE', CURRENT_TIMESTAMP - INTERVAL '57' DAY, CURRENT_TIMESTAMP - INTERVAL '57' DAY, (SELECT id FROM settlements WHERE osm_id = 26150791)
FROM users u, categories c WHERE u.email = 'seed.budprokat@example.com' AND c.name = 'Спецтехніка'
AND NOT EXISTS (SELECT 1 FROM equipment_photos WHERE storage_key = 'seed/equipment-090.png');
INSERT INTO equipment_photos (equipment_id, storage_key, position, created_at)
SELECT MAX(e.id), 'seed/equipment-090.png', 0, CURRENT_TIMESTAMP FROM equipment e JOIN users u ON u.id = e.owner_id
WHERE e.title = 'Генератор дизельний 10 кВт' AND u.email = 'seed.budprokat@example.com'
AND NOT EXISTS (SELECT 1 FROM equipment_photos WHERE storage_key = 'seed/equipment-090.png')
HAVING MAX(e.id) IS NOT NULL;
INSERT INTO equipment_photos (equipment_id, storage_key, position, created_at)
SELECT MAX(e.id), 'seed/equipment-090-2.png', 1, CURRENT_TIMESTAMP FROM equipment e JOIN users u ON u.id = e.owner_id
WHERE e.title = 'Генератор дизельний 10 кВт' AND u.email = 'seed.budprokat@example.com'
AND NOT EXISTS (SELECT 1 FROM equipment_photos WHERE storage_key = 'seed/equipment-090-2.png')
HAVING MAX(e.id) IS NOT NULL;

-- 091. Реноватор Einhell TE-MG 300
INSERT INTO equipment (owner_id, category_id, title, description, item_condition, price_per_day, quantity, min_days, max_days, pickup_available, delivery_available, delivery_note, address, city, lat, lon, status, created_at, updated_at, settlement_id)
SELECT u.id, c.id, 'Реноватор Einhell TE-MG 300', 'У хорошому стані, регулярно обслуговується. Можу підказати, як найкраще використати.', 'GOOD', 240.00, 1, 1, 14, TRUE, FALSE, NULL, 'вул. Кульпарківська, 108', 'Львів', 49.807735, 24.042416, 'ACTIVE', CURRENT_TIMESTAMP - INTERVAL '16' DAY, CURRENT_TIMESTAMP - INTERVAL '16' DAY, (SELECT id FROM settlements WHERE osm_id = 26150791)
FROM users u, categories c WHERE u.email = 'seed.marta@example.com' AND c.name = 'Електроінструмент'
AND NOT EXISTS (SELECT 1 FROM equipment_photos WHERE storage_key = 'seed/equipment-091.png');
INSERT INTO equipment_photos (equipment_id, storage_key, position, created_at)
SELECT MAX(e.id), 'seed/equipment-091.png', 0, CURRENT_TIMESTAMP FROM equipment e JOIN users u ON u.id = e.owner_id
WHERE e.title = 'Реноватор Einhell TE-MG 300' AND u.email = 'seed.marta@example.com'
AND NOT EXISTS (SELECT 1 FROM equipment_photos WHERE storage_key = 'seed/equipment-091.png')
HAVING MAX(e.id) IS NOT NULL;

-- 092. Будівельний фен Bosch GHG 23-66
INSERT INTO equipment (owner_id, category_id, title, description, item_condition, price_per_day, quantity, min_days, max_days, pickup_available, delivery_available, delivery_note, address, city, lat, lon, status, created_at, updated_at, settlement_id)
SELECT u.id, c.id, 'Будівельний фен Bosch GHG 23-66', 'Надійний і простий у використанні. Повернути прошу в чистому вигляді.', 'GOOD', 320.00, 1, 1, 14, FALSE, TRUE, 'Доставка по Львову та області, ціну узгодимо', 'вул. Шевченка, 105', 'Львів', 49.830047, 23.961476, 'ACTIVE', CURRENT_TIMESTAMP - INTERVAL '3' DAY, CURRENT_TIMESTAMP - INTERVAL '3' DAY, (SELECT id FROM settlements WHERE osm_id = 26150791)
FROM users u, categories c WHERE u.email = 'seed.budprokat@example.com' AND c.name = 'Будівельне обладнання'
AND NOT EXISTS (SELECT 1 FROM equipment_photos WHERE storage_key = 'seed/equipment-092.png');
INSERT INTO equipment_photos (equipment_id, storage_key, position, created_at)
SELECT MAX(e.id), 'seed/equipment-092.png', 0, CURRENT_TIMESTAMP FROM equipment e JOIN users u ON u.id = e.owner_id
WHERE e.title = 'Будівельний фен Bosch GHG 23-66' AND u.email = 'seed.budprokat@example.com'
AND NOT EXISTS (SELECT 1 FROM equipment_photos WHERE storage_key = 'seed/equipment-092.png')
HAVING MAX(e.id) IS NOT NULL;

-- 093. Вишка-тура 6 м
INSERT INTO equipment (owner_id, category_id, title, description, item_condition, price_per_day, quantity, min_days, max_days, pickup_available, delivery_available, delivery_note, address, city, lat, lon, status, created_at, updated_at, settlement_id)
SELECT u.id, c.id, 'Вишка-тура 6 м', 'У хорошому стані, регулярно обслуговується. Можу підказати, як найкраще використати.', 'GOOD', 320.00, 2, 1, 14, FALSE, TRUE, 'Доставка по Львову, вартість за домовленістю', 'вул. Мазепи, 86', 'Львів', 49.857704, 23.964415, 'ACTIVE', CURRENT_TIMESTAMP - INTERVAL '33' DAY, CURRENT_TIMESTAMP - INTERVAL '33' DAY, (SELECT id FROM settlements WHERE osm_id = 26150791)
FROM users u, categories c WHERE u.email = 'seed.marta@example.com' AND c.name = 'Риштування та драбини'
AND NOT EXISTS (SELECT 1 FROM equipment_photos WHERE storage_key = 'seed/equipment-093.png');
INSERT INTO equipment_photos (equipment_id, storage_key, position, created_at)
SELECT MAX(e.id), 'seed/equipment-093.png', 0, CURRENT_TIMESTAMP FROM equipment e JOIN users u ON u.id = e.owner_id
WHERE e.title = 'Вишка-тура 6 м' AND u.email = 'seed.marta@example.com'
AND NOT EXISTS (SELECT 1 FROM equipment_photos WHERE storage_key = 'seed/equipment-093.png')
HAVING MAX(e.id) IS NOT NULL;

-- 094. Тример бензиновий Stihl FS 55
INSERT INTO equipment (owner_id, category_id, title, description, item_condition, price_per_day, quantity, min_days, max_days, pickup_available, delivery_available, delivery_note, address, city, lat, lon, status, created_at, updated_at, settlement_id)
SELECT u.id, c.id, 'Тример бензиновий Stihl FS 55', 'Підходить для домашніх і професійних робіт. Видаю з кейсом і комплектом аксесуарів.', 'GOOD', 220.00, 2, 1, 14, TRUE, TRUE, 'Доставка по Львову, вартість за домовленістю', 'вул. Сихівська, 78', 'Львів', 49.803276, 24.050375, 'ACTIVE', CURRENT_TIMESTAMP - INTERVAL '38' DAY, CURRENT_TIMESTAMP - INTERVAL '38' DAY, (SELECT id FROM settlements WHERE osm_id = 26150791)
FROM users u, categories c WHERE u.email = 'seed.taras@example.com' AND c.name = 'Садова техніка'
AND NOT EXISTS (SELECT 1 FROM equipment_photos WHERE storage_key = 'seed/equipment-094.png');
INSERT INTO equipment_photos (equipment_id, storage_key, position, created_at)
SELECT MAX(e.id), 'seed/equipment-094.png', 0, CURRENT_TIMESTAMP FROM equipment e JOIN users u ON u.id = e.owner_id
WHERE e.title = 'Тример бензиновий Stihl FS 55' AND u.email = 'seed.taras@example.com'
AND NOT EXISTS (SELECT 1 FROM equipment_photos WHERE storage_key = 'seed/equipment-094.png')
HAVING MAX(e.id) IS NOT NULL;

-- 095. Снігоприбирач бензиновий Honda HSS 655
INSERT INTO equipment (owner_id, category_id, title, description, item_condition, price_per_day, quantity, min_days, max_days, pickup_available, delivery_available, delivery_note, address, city, lat, lon, status, created_at, updated_at, settlement_id)
SELECT u.id, c.id, 'Снігоприбирач бензиновий Honda HSS 655', 'Справний, перевірений перед кожною орендою. Видаю з інструкцією та всім потрібним для роботи.', 'GOOD', 490.00, 1, 1, NULL, TRUE, FALSE, NULL, 'вул. Варшавська, 14', 'Львів', 49.857814, 24.016251, 'ACTIVE', CURRENT_TIMESTAMP - INTERVAL '21' DAY, CURRENT_TIMESTAMP - INTERVAL '21' DAY, (SELECT id FROM settlements WHERE osm_id = 26150791)
FROM users u, categories c WHERE u.email = 'seed.olena@example.com' AND c.name = 'Снігоприбиральна техніка'
AND NOT EXISTS (SELECT 1 FROM equipment_photos WHERE storage_key = 'seed/equipment-095.png');
INSERT INTO equipment_photos (equipment_id, storage_key, position, created_at)
SELECT MAX(e.id), 'seed/equipment-095.png', 0, CURRENT_TIMESTAMP FROM equipment e JOIN users u ON u.id = e.owner_id
WHERE e.title = 'Снігоприбирач бензиновий Honda HSS 655' AND u.email = 'seed.olena@example.com'
AND NOT EXISTS (SELECT 1 FROM equipment_photos WHERE storage_key = 'seed/equipment-095.png')
HAVING MAX(e.id) IS NOT NULL;
INSERT INTO equipment_photos (equipment_id, storage_key, position, created_at)
SELECT MAX(e.id), 'seed/equipment-095-2.png', 1, CURRENT_TIMESTAMP FROM equipment e JOIN users u ON u.id = e.owner_id
WHERE e.title = 'Снігоприбирач бензиновий Honda HSS 655' AND u.email = 'seed.olena@example.com'
AND NOT EXISTS (SELECT 1 FROM equipment_photos WHERE storage_key = 'seed/equipment-095-2.png')
HAVING MAX(e.id) IS NOT NULL;

-- 096. Поломийна машина Kärcher BR 30/4
INSERT INTO equipment (owner_id, category_id, title, description, item_condition, price_per_day, quantity, min_days, max_days, pickup_available, delivery_available, delivery_note, address, city, lat, lon, status, created_at, updated_at, settlement_id)
SELECT u.id, c.id, 'Поломийна машина Kärcher BR 30/4', 'Справний, перевірений перед кожною орендою. Видаю з інструкцією та всім потрібним для роботи.', 'GOOD', 570.00, 1, 2, 14, TRUE, FALSE, NULL, 'вул. Варшавська, 75', 'Львів', 49.883530, 24.092351, 'ACTIVE', CURRENT_TIMESTAMP - INTERVAL '13' DAY, CURRENT_TIMESTAMP - INTERVAL '13' DAY, (SELECT id FROM settlements WHERE osm_id = 26150791)
FROM users u, categories c WHERE u.email = 'seed.olena@example.com' AND c.name = 'Клінінгове обладнання'
AND NOT EXISTS (SELECT 1 FROM equipment_photos WHERE storage_key = 'seed/equipment-096.png');
INSERT INTO equipment_photos (equipment_id, storage_key, position, created_at)
SELECT MAX(e.id), 'seed/equipment-096.png', 0, CURRENT_TIMESTAMP FROM equipment e JOIN users u ON u.id = e.owner_id
WHERE e.title = 'Поломийна машина Kärcher BR 30/4' AND u.email = 'seed.olena@example.com'
AND NOT EXISTS (SELECT 1 FROM equipment_photos WHERE storage_key = 'seed/equipment-096.png')
HAVING MAX(e.id) IS NOT NULL;

-- 097. Фотоапарат Canon EOS R6
INSERT INTO equipment (owner_id, category_id, title, description, item_condition, price_per_day, quantity, min_days, max_days, pickup_available, delivery_available, delivery_note, address, city, lat, lon, status, created_at, updated_at, settlement_id)
SELECT u.id, c.id, 'Фотоапарат Canon EOS R6', 'Надійний і простий у використанні. Повернути прошу в чистому вигляді.', 'EXCELLENT', 630.00, 1, 3, 14, TRUE, FALSE, NULL, 'вул. Княгині Ольги, 111', 'Львів', 49.852263, 24.077436, 'ACTIVE', CURRENT_TIMESTAMP - INTERVAL '19' DAY, CURRENT_TIMESTAMP - INTERVAL '19' DAY, (SELECT id FROM settlements WHERE osm_id = 26150791)
FROM users u, categories c WHERE u.email = 'seed.andrii@example.com' AND c.name = 'Фото- та відеотехніка'
AND NOT EXISTS (SELECT 1 FROM equipment_photos WHERE storage_key = 'seed/equipment-097.png');
INSERT INTO equipment_photos (equipment_id, storage_key, position, created_at)
SELECT MAX(e.id), 'seed/equipment-097.png', 0, CURRENT_TIMESTAMP FROM equipment e JOIN users u ON u.id = e.owner_id
WHERE e.title = 'Фотоапарат Canon EOS R6' AND u.email = 'seed.andrii@example.com'
AND NOT EXISTS (SELECT 1 FROM equipment_photos WHERE storage_key = 'seed/equipment-097.png')
HAVING MAX(e.id) IS NOT NULL;

-- 098. Мікшерний пульт Yamaha MG10XU
INSERT INTO equipment (owner_id, category_id, title, description, item_condition, price_per_day, quantity, min_days, max_days, pickup_available, delivery_available, delivery_note, address, city, lat, lon, status, created_at, updated_at, settlement_id)
SELECT u.id, c.id, 'Мікшерний пульт Yamaha MG10XU', 'Акуратне використання, без пошкоджень. Перед видачею чищу й перевіряю.', 'GOOD', 460.00, 1, 1, 7, FALSE, TRUE, 'Доставка по Львову та області, ціну узгодимо', 'вул. Центральна, 57', 'Брюховичі', 49.911808, 23.958683, 'ACTIVE', CURRENT_TIMESTAMP - INTERVAL '4' DAY, CURRENT_TIMESTAMP - INTERVAL '4' DAY, (SELECT id FROM settlements WHERE osm_id = 337541358)
FROM users u, categories c WHERE u.email = 'seed.fotozona@example.com' AND c.name = 'Світло та звук'
AND NOT EXISTS (SELECT 1 FROM equipment_photos WHERE storage_key = 'seed/equipment-098.png');
INSERT INTO equipment_photos (equipment_id, storage_key, position, created_at)
SELECT MAX(e.id), 'seed/equipment-098.png', 0, CURRENT_TIMESTAMP FROM equipment e JOIN users u ON u.id = e.owner_id
WHERE e.title = 'Мікшерний пульт Yamaha MG10XU' AND u.email = 'seed.fotozona@example.com'
AND NOT EXISTS (SELECT 1 FROM equipment_photos WHERE storage_key = 'seed/equipment-098.png')
HAVING MAX(e.id) IS NOT NULL;

-- 099. SUP-дошка 320 см
INSERT INTO equipment (owner_id, category_id, title, description, item_condition, price_per_day, quantity, min_days, max_days, pickup_available, delivery_available, delivery_note, address, city, lat, lon, status, created_at, updated_at, settlement_id)
SELECT u.id, c.id, 'SUP-дошка 320 см', 'Підходить для домашніх і професійних робіт. Видаю з кейсом і комплектом аксесуарів.', 'FAIR', 290.00, 2, 1, 14, TRUE, FALSE, NULL, 'вул. Кульпарківська, 117', 'Львів', 49.823481, 24.028961, 'ACTIVE', CURRENT_TIMESTAMP - INTERVAL '11' DAY, CURRENT_TIMESTAMP - INTERVAL '11' DAY, (SELECT id FROM settlements WHERE osm_id = 26150791)
FROM users u, categories c WHERE u.email = 'seed.taras@example.com' AND c.name = 'Туристичне спорядження'
AND NOT EXISTS (SELECT 1 FROM equipment_photos WHERE storage_key = 'seed/equipment-099.png');
INSERT INTO equipment_photos (equipment_id, storage_key, position, created_at)
SELECT MAX(e.id), 'seed/equipment-099.png', 0, CURRENT_TIMESTAMP FROM equipment e JOIN users u ON u.id = e.owner_id
WHERE e.title = 'SUP-дошка 320 см' AND u.email = 'seed.taras@example.com'
AND NOT EXISTS (SELECT 1 FROM equipment_photos WHERE storage_key = 'seed/equipment-099.png')
HAVING MAX(e.id) IS NOT NULL;

-- 100. Причіп легковий 750 кг
INSERT INTO equipment (owner_id, category_id, title, description, item_condition, price_per_day, quantity, min_days, max_days, pickup_available, delivery_available, delivery_note, address, city, lat, lon, status, created_at, updated_at, settlement_id)
SELECT u.id, c.id, 'Причіп легковий 750 кг', 'Надійний і простий у використанні. Повернути прошу в чистому вигляді.', 'EXCELLENT', 2890.00, 1, 3, 7, TRUE, FALSE, NULL, 'вул. Шевченка, 79', 'Львів', 49.799895, 24.078825, 'ACTIVE', CURRENT_TIMESTAMP - INTERVAL '22' DAY, CURRENT_TIMESTAMP - INTERVAL '22' DAY, (SELECT id FROM settlements WHERE osm_id = 26150791)
FROM users u, categories c WHERE u.email = 'seed.budprokat@example.com' AND c.name = 'Спецтехніка'
AND NOT EXISTS (SELECT 1 FROM equipment_photos WHERE storage_key = 'seed/equipment-100.png');
INSERT INTO equipment_photos (equipment_id, storage_key, position, created_at)
SELECT MAX(e.id), 'seed/equipment-100.png', 0, CURRENT_TIMESTAMP FROM equipment e JOIN users u ON u.id = e.owner_id
WHERE e.title = 'Причіп легковий 750 кг' AND u.email = 'seed.budprokat@example.com'
AND NOT EXISTS (SELECT 1 FROM equipment_photos WHERE storage_key = 'seed/equipment-100.png')
HAVING MAX(e.id) IS NOT NULL;
INSERT INTO equipment_photos (equipment_id, storage_key, position, created_at)
SELECT MAX(e.id), 'seed/equipment-100-2.png', 1, CURRENT_TIMESTAMP FROM equipment e JOIN users u ON u.id = e.owner_id
WHERE e.title = 'Причіп легковий 750 кг' AND u.email = 'seed.budprokat@example.com'
AND NOT EXISTS (SELECT 1 FROM equipment_photos WHERE storage_key = 'seed/equipment-100-2.png')
HAVING MAX(e.id) IS NOT NULL;

-- 101. Перфоратор Bosch GBH 2-26
INSERT INTO equipment (owner_id, category_id, title, description, item_condition, price_per_day, quantity, min_days, max_days, pickup_available, delivery_available, delivery_note, address, city, lat, lon, status, created_at, updated_at, settlement_id)
SELECT u.id, c.id, 'Перфоратор Bosch GBH 2-26', 'Підходить для домашніх і професійних робіт. Видаю з кейсом і комплектом аксесуарів.', 'GOOD', 390.00, 1, 1, 30, TRUE, TRUE, NULL, 'вул. Виговського, 100', 'Львів', 49.835156, 24.054229, 'ACTIVE', CURRENT_TIMESTAMP - INTERVAL '2' DAY, CURRENT_TIMESTAMP - INTERVAL '2' DAY, (SELECT id FROM settlements WHERE osm_id = 26150791)
FROM users u, categories c WHERE u.email = 'seed.marta@example.com' AND c.name = 'Електроінструмент'
AND NOT EXISTS (SELECT 1 FROM equipment_photos WHERE storage_key = 'seed/equipment-101.png');
INSERT INTO equipment_photos (equipment_id, storage_key, position, created_at)
SELECT MAX(e.id), 'seed/equipment-101.png', 0, CURRENT_TIMESTAMP FROM equipment e JOIN users u ON u.id = e.owner_id
WHERE e.title = 'Перфоратор Bosch GBH 2-26' AND u.email = 'seed.marta@example.com'
AND NOT EXISTS (SELECT 1 FROM equipment_photos WHERE storage_key = 'seed/equipment-101.png')
HAVING MAX(e.id) IS NOT NULL;

-- 102. Бетонозмішувач 180 л
INSERT INTO equipment (owner_id, category_id, title, description, item_condition, price_per_day, quantity, min_days, max_days, pickup_available, delivery_available, delivery_note, address, city, lat, lon, status, created_at, updated_at, settlement_id)
SELECT u.id, c.id, 'Бетонозмішувач 180 л', 'Акуратне використання, без пошкоджень. Перед видачею чищу й перевіряю.', 'NEW', 770.00, 2, 1, 7, TRUE, TRUE, 'Доставка по Львову, вартість за домовленістю', 'вул. Володимира Великого, 86', 'Львів', 49.864603, 24.073979, 'ACTIVE', CURRENT_TIMESTAMP - INTERVAL '50' DAY, CURRENT_TIMESTAMP - INTERVAL '50' DAY, (SELECT id FROM settlements WHERE osm_id = 26150791)
FROM users u, categories c WHERE u.email = 'seed.budprokat@example.com' AND c.name = 'Будівельне обладнання'
AND NOT EXISTS (SELECT 1 FROM equipment_photos WHERE storage_key = 'seed/equipment-102.png');
INSERT INTO equipment_photos (equipment_id, storage_key, position, created_at)
SELECT MAX(e.id), 'seed/equipment-102.png', 0, CURRENT_TIMESTAMP FROM equipment e JOIN users u ON u.id = e.owner_id
WHERE e.title = 'Бетонозмішувач 180 л' AND u.email = 'seed.budprokat@example.com'
AND NOT EXISTS (SELECT 1 FROM equipment_photos WHERE storage_key = 'seed/equipment-102.png')
HAVING MAX(e.id) IS NOT NULL;

-- 103. Драбина алюмінієва 3×9 щаблів
INSERT INTO equipment (owner_id, category_id, title, description, item_condition, price_per_day, quantity, min_days, max_days, pickup_available, delivery_available, delivery_note, address, city, lat, lon, status, created_at, updated_at, settlement_id)
SELECT u.id, c.id, 'Драбина алюмінієва 3×9 щаблів', 'Справний, перевірений перед кожною орендою. Видаю з інструкцією та всім потрібним для роботи.', 'EXCELLENT', 380.00, 1, 3, 7, TRUE, FALSE, NULL, 'просп. Свободи, 2', 'Львів', 49.846770, 23.989450, 'ACTIVE', CURRENT_TIMESTAMP - INTERVAL '4' DAY, CURRENT_TIMESTAMP - INTERVAL '4' DAY, (SELECT id FROM settlements WHERE osm_id = 26150791)
FROM users u, categories c WHERE u.email = 'seed.taras@example.com' AND c.name = 'Риштування та драбини'
AND NOT EXISTS (SELECT 1 FROM equipment_photos WHERE storage_key = 'seed/equipment-103.png');
INSERT INTO equipment_photos (equipment_id, storage_key, position, created_at)
SELECT MAX(e.id), 'seed/equipment-103.png', 0, CURRENT_TIMESTAMP FROM equipment e JOIN users u ON u.id = e.owner_id
WHERE e.title = 'Драбина алюмінієва 3×9 щаблів' AND u.email = 'seed.taras@example.com'
AND NOT EXISTS (SELECT 1 FROM equipment_photos WHERE storage_key = 'seed/equipment-103.png')
HAVING MAX(e.id) IS NOT NULL;

-- 104. Культиватор 6,5 к.с.
INSERT INTO equipment (owner_id, category_id, title, description, item_condition, price_per_day, quantity, min_days, max_days, pickup_available, delivery_available, delivery_note, address, city, lat, lon, status, created_at, updated_at, settlement_id)
SELECT u.id, c.id, 'Культиватор 6,5 к.с.', 'Підходить для домашніх і професійних робіт. Видаю з кейсом і комплектом аксесуарів.', 'GOOD', 240.00, 1, 1, 30, TRUE, TRUE, NULL, 'вул. Городоцька, 6', 'Львів', 49.844464, 23.960321, 'ACTIVE', CURRENT_TIMESTAMP - INTERVAL '36' DAY, CURRENT_TIMESTAMP - INTERVAL '36' DAY, (SELECT id FROM settlements WHERE osm_id = 26150791)
FROM users u, categories c WHERE u.email = 'seed.taras@example.com' AND c.name = 'Садова техніка'
AND NOT EXISTS (SELECT 1 FROM equipment_photos WHERE storage_key = 'seed/equipment-104.png');
INSERT INTO equipment_photos (equipment_id, storage_key, position, created_at)
SELECT MAX(e.id), 'seed/equipment-104.png', 0, CURRENT_TIMESTAMP FROM equipment e JOIN users u ON u.id = e.owner_id
WHERE e.title = 'Культиватор 6,5 к.с.' AND u.email = 'seed.taras@example.com'
AND NOT EXISTS (SELECT 1 FROM equipment_photos WHERE storage_key = 'seed/equipment-104.png')
HAVING MAX(e.id) IS NOT NULL;

-- 105. Снігоприбирач електричний Einhell
INSERT INTO equipment (owner_id, category_id, title, description, item_condition, price_per_day, quantity, min_days, max_days, pickup_available, delivery_available, delivery_note, address, city, lat, lon, status, created_at, updated_at, settlement_id)
SELECT u.id, c.id, 'Снігоприбирач електричний Einhell', 'У хорошому стані, регулярно обслуговується. Можу підказати, як найкраще використати.', 'GOOD', 630.00, 1, 1, 7, TRUE, TRUE, 'Доставка по Львову, вартість за домовленістю', 'вул. Хоткевича, 91', 'Львів', 49.850952, 24.007719, 'ACTIVE', CURRENT_TIMESTAMP - INTERVAL '11' DAY, CURRENT_TIMESTAMP - INTERVAL '11' DAY, (SELECT id FROM settlements WHERE osm_id = 26150791)
FROM users u, categories c WHERE u.email = 'seed.taras@example.com' AND c.name = 'Снігоприбиральна техніка'
AND NOT EXISTS (SELECT 1 FROM equipment_photos WHERE storage_key = 'seed/equipment-105.png');
INSERT INTO equipment_photos (equipment_id, storage_key, position, created_at)
SELECT MAX(e.id), 'seed/equipment-105.png', 0, CURRENT_TIMESTAMP FROM equipment e JOIN users u ON u.id = e.owner_id
WHERE e.title = 'Снігоприбирач електричний Einhell' AND u.email = 'seed.taras@example.com'
AND NOT EXISTS (SELECT 1 FROM equipment_photos WHERE storage_key = 'seed/equipment-105.png')
HAVING MAX(e.id) IS NOT NULL;
INSERT INTO equipment_photos (equipment_id, storage_key, position, created_at)
SELECT MAX(e.id), 'seed/equipment-105-2.png', 1, CURRENT_TIMESTAMP FROM equipment e JOIN users u ON u.id = e.owner_id
WHERE e.title = 'Снігоприбирач електричний Einhell' AND u.email = 'seed.taras@example.com'
AND NOT EXISTS (SELECT 1 FROM equipment_photos WHERE storage_key = 'seed/equipment-105-2.png')
HAVING MAX(e.id) IS NOT NULL;

-- 106. Мийка високого тиску Kärcher K5
INSERT INTO equipment (owner_id, category_id, title, description, item_condition, price_per_day, quantity, min_days, max_days, pickup_available, delivery_available, delivery_note, address, city, lat, lon, status, created_at, updated_at, settlement_id)
SELECT u.id, c.id, 'Мийка високого тиску Kärcher K5', 'Надійний і простий у використанні. Повернути прошу в чистому вигляді.', 'GOOD', 320.00, 1, 1, NULL, TRUE, FALSE, NULL, 'вул. Личаківська, 100', 'Львів', 49.840575, 23.962105, 'ACTIVE', CURRENT_TIMESTAMP - INTERVAL '36' DAY, CURRENT_TIMESTAMP - INTERVAL '36' DAY, (SELECT id FROM settlements WHERE osm_id = 26150791)
FROM users u, categories c WHERE u.email = 'seed.marta@example.com' AND c.name = 'Клінінгове обладнання'
AND NOT EXISTS (SELECT 1 FROM equipment_photos WHERE storage_key = 'seed/equipment-106.png');
INSERT INTO equipment_photos (equipment_id, storage_key, position, created_at)
SELECT MAX(e.id), 'seed/equipment-106.png', 0, CURRENT_TIMESTAMP FROM equipment e JOIN users u ON u.id = e.owner_id
WHERE e.title = 'Мийка високого тиску Kärcher K5' AND u.email = 'seed.marta@example.com'
AND NOT EXISTS (SELECT 1 FROM equipment_photos WHERE storage_key = 'seed/equipment-106.png')
HAVING MAX(e.id) IS NOT NULL;

-- 107. Об’єктив Sony 70-200 f/2.8
INSERT INTO equipment (owner_id, category_id, title, description, item_condition, price_per_day, quantity, min_days, max_days, pickup_available, delivery_available, delivery_note, address, city, lat, lon, status, created_at, updated_at, settlement_id)
SELECT u.id, c.id, 'Об’єктив Sony 70-200 f/2.8', 'Надійний і простий у використанні. Повернути прошу в чистому вигляді.', 'EXCELLENT', 1440.00, 1, 2, 14, TRUE, FALSE, NULL, 'вул. Наукова, 28', 'Львів', 49.850086, 23.999829, 'ACTIVE', CURRENT_TIMESTAMP - INTERVAL '23' DAY, CURRENT_TIMESTAMP - INTERVAL '23' DAY, (SELECT id FROM settlements WHERE osm_id = 26150791)
FROM users u, categories c WHERE u.email = 'seed.fotozona@example.com' AND c.name = 'Фото- та відеотехніка'
AND NOT EXISTS (SELECT 1 FROM equipment_photos WHERE storage_key = 'seed/equipment-107.png');
INSERT INTO equipment_photos (equipment_id, storage_key, position, created_at)
SELECT MAX(e.id), 'seed/equipment-107.png', 0, CURRENT_TIMESTAMP FROM equipment e JOIN users u ON u.id = e.owner_id
WHERE e.title = 'Об’єктив Sony 70-200 f/2.8' AND u.email = 'seed.fotozona@example.com'
AND NOT EXISTS (SELECT 1 FROM equipment_photos WHERE storage_key = 'seed/equipment-107.png')
HAVING MAX(e.id) IS NOT NULL;

-- 108. Колонка JBL PartyBox 310
INSERT INTO equipment (owner_id, category_id, title, description, item_condition, price_per_day, quantity, min_days, max_days, pickup_available, delivery_available, delivery_note, address, city, lat, lon, status, created_at, updated_at, settlement_id)
SELECT u.id, c.id, 'Колонка JBL PartyBox 310', 'Акуратне використання, без пошкоджень. Перед видачею чищу й перевіряю.', 'GOOD', 650.00, 3, 1, 30, TRUE, TRUE, NULL, 'вул. Пасічна, 85', 'Львів', 49.816817, 24.047220, 'ACTIVE', CURRENT_TIMESTAMP - INTERVAL '41' DAY, CURRENT_TIMESTAMP - INTERVAL '41' DAY, (SELECT id FROM settlements WHERE osm_id = 26150791)
FROM users u, categories c WHERE u.email = 'seed.fotozona@example.com' AND c.name = 'Світло та звук'
AND NOT EXISTS (SELECT 1 FROM equipment_photos WHERE storage_key = 'seed/equipment-108.png');
INSERT INTO equipment_photos (equipment_id, storage_key, position, created_at)
SELECT MAX(e.id), 'seed/equipment-108.png', 0, CURRENT_TIMESTAMP FROM equipment e JOIN users u ON u.id = e.owner_id
WHERE e.title = 'Колонка JBL PartyBox 310' AND u.email = 'seed.fotozona@example.com'
AND NOT EXISTS (SELECT 1 FROM equipment_photos WHERE storage_key = 'seed/equipment-108.png')
HAVING MAX(e.id) IS NOT NULL;

-- 109. Рюкзак 70 л Deuter
INSERT INTO equipment (owner_id, category_id, title, description, item_condition, price_per_day, quantity, min_days, max_days, pickup_available, delivery_available, delivery_note, address, city, lat, lon, status, created_at, updated_at, settlement_id)
SELECT u.id, c.id, 'Рюкзак 70 л Deuter', 'Надійний і простий у використанні. Повернути прошу в чистому вигляді.', 'NEW', 380.00, 1, 2, 14, TRUE, FALSE, NULL, 'вул. Замарстинівська, 113', 'Львів', 49.879311, 24.045472, 'ACTIVE', CURRENT_TIMESTAMP - INTERVAL '6' DAY, CURRENT_TIMESTAMP - INTERVAL '6' DAY, (SELECT id FROM settlements WHERE osm_id = 26150791)
FROM users u, categories c WHERE u.email = 'seed.taras@example.com' AND c.name = 'Туристичне спорядження'
AND NOT EXISTS (SELECT 1 FROM equipment_photos WHERE storage_key = 'seed/equipment-109.png');
INSERT INTO equipment_photos (equipment_id, storage_key, position, created_at)
SELECT MAX(e.id), 'seed/equipment-109.png', 0, CURRENT_TIMESTAMP FROM equipment e JOIN users u ON u.id = e.owner_id
WHERE e.title = 'Рюкзак 70 л Deuter' AND u.email = 'seed.taras@example.com'
AND NOT EXISTS (SELECT 1 FROM equipment_photos WHERE storage_key = 'seed/equipment-109.png')
HAVING MAX(e.id) IS NOT NULL;

-- 110. Мініекскаватор 1,5 т
INSERT INTO equipment (owner_id, category_id, title, description, item_condition, price_per_day, quantity, min_days, max_days, pickup_available, delivery_available, delivery_note, address, city, lat, lon, status, created_at, updated_at, settlement_id)
SELECT u.id, c.id, 'Мініекскаватор 1,5 т', 'Надійний і простий у використанні. Повернути прошу в чистому вигляді.', 'GOOD', 980.00, 1, 2, 14, TRUE, TRUE, NULL, 'вул. Наукова, 93', 'Львів', 49.822742, 24.008590, 'ACTIVE', CURRENT_TIMESTAMP - INTERVAL '25' DAY, CURRENT_TIMESTAMP - INTERVAL '25' DAY, (SELECT id FROM settlements WHERE osm_id = 26150791)
FROM users u, categories c WHERE u.email = 'seed.budprokat@example.com' AND c.name = 'Спецтехніка'
AND NOT EXISTS (SELECT 1 FROM equipment_photos WHERE storage_key = 'seed/equipment-110.png');
INSERT INTO equipment_photos (equipment_id, storage_key, position, created_at)
SELECT MAX(e.id), 'seed/equipment-110.png', 0, CURRENT_TIMESTAMP FROM equipment e JOIN users u ON u.id = e.owner_id
WHERE e.title = 'Мініекскаватор 1,5 т' AND u.email = 'seed.budprokat@example.com'
AND NOT EXISTS (SELECT 1 FROM equipment_photos WHERE storage_key = 'seed/equipment-110.png')
HAVING MAX(e.id) IS NOT NULL;
INSERT INTO equipment_photos (equipment_id, storage_key, position, created_at)
SELECT MAX(e.id), 'seed/equipment-110-2.png', 1, CURRENT_TIMESTAMP FROM equipment e JOIN users u ON u.id = e.owner_id
WHERE e.title = 'Мініекскаватор 1,5 т' AND u.email = 'seed.budprokat@example.com'
AND NOT EXISTS (SELECT 1 FROM equipment_photos WHERE storage_key = 'seed/equipment-110-2.png')
HAVING MAX(e.id) IS NOT NULL;

-- 111. Шуруповерт Makita DF333
INSERT INTO equipment (owner_id, category_id, title, description, item_condition, price_per_day, quantity, min_days, max_days, pickup_available, delivery_available, delivery_note, address, city, lat, lon, status, created_at, updated_at, settlement_id)
SELECT u.id, c.id, 'Шуруповерт Makita DF333', 'У хорошому стані, регулярно обслуговується. Можу підказати, як найкраще використати.', 'EXCELLENT', 310.00, 5, 1, 30, TRUE, FALSE, NULL, 'вул. Хоткевича, 69', 'Львів', 49.842076, 24.045969, 'ACTIVE', CURRENT_TIMESTAMP - INTERVAL '56' DAY, CURRENT_TIMESTAMP - INTERVAL '56' DAY, (SELECT id FROM settlements WHERE osm_id = 26150791)
FROM users u, categories c WHERE u.email = 'seed.andrii@example.com' AND c.name = 'Електроінструмент'
AND NOT EXISTS (SELECT 1 FROM equipment_photos WHERE storage_key = 'seed/equipment-111.png');
INSERT INTO equipment_photos (equipment_id, storage_key, position, created_at)
SELECT MAX(e.id), 'seed/equipment-111.png', 0, CURRENT_TIMESTAMP FROM equipment e JOIN users u ON u.id = e.owner_id
WHERE e.title = 'Шуруповерт Makita DF333' AND u.email = 'seed.andrii@example.com'
AND NOT EXISTS (SELECT 1 FROM equipment_photos WHERE storage_key = 'seed/equipment-111.png')
HAVING MAX(e.id) IS NOT NULL;

-- 112. Віброплита 90 кг
INSERT INTO equipment (owner_id, category_id, title, description, item_condition, price_per_day, quantity, min_days, max_days, pickup_available, delivery_available, delivery_note, address, city, lat, lon, status, created_at, updated_at, settlement_id)
SELECT u.id, c.id, 'Віброплита 90 кг', 'Надійний і простий у використанні. Повернути прошу в чистому вигляді.', 'EXCELLENT', 1200.00, 1, 1, 14, TRUE, TRUE, NULL, 'вул. Хоткевича, 100', 'Львів', 49.849898, 24.093177, 'ACTIVE', CURRENT_TIMESTAMP - INTERVAL '22' DAY, CURRENT_TIMESTAMP - INTERVAL '22' DAY, (SELECT id FROM settlements WHERE osm_id = 26150791)
FROM users u, categories c WHERE u.email = 'seed.budprokat@example.com' AND c.name = 'Будівельне обладнання'
AND NOT EXISTS (SELECT 1 FROM equipment_photos WHERE storage_key = 'seed/equipment-112.png');
INSERT INTO equipment_photos (equipment_id, storage_key, position, created_at)
SELECT MAX(e.id), 'seed/equipment-112.png', 0, CURRENT_TIMESTAMP FROM equipment e JOIN users u ON u.id = e.owner_id
WHERE e.title = 'Віброплита 90 кг' AND u.email = 'seed.budprokat@example.com'
AND NOT EXISTS (SELECT 1 FROM equipment_photos WHERE storage_key = 'seed/equipment-112.png')
HAVING MAX(e.id) IS NOT NULL;

-- 113. Риштування пересувне 4 м
INSERT INTO equipment (owner_id, category_id, title, description, item_condition, price_per_day, quantity, min_days, max_days, pickup_available, delivery_available, delivery_note, address, city, lat, lon, status, created_at, updated_at, settlement_id)
SELECT u.id, c.id, 'Риштування пересувне 4 м', 'Справний, перевірений перед кожною орендою. Видаю з інструкцією та всім потрібним для роботи.', 'FAIR', 140.00, 5, 3, 14, TRUE, TRUE, 'Доставка по Львову та області, ціну узгодимо', 'вул. Центральна, 8', 'Пустомити', 49.709813, 23.898717, 'ACTIVE', CURRENT_TIMESTAMP - INTERVAL '38' DAY, CURRENT_TIMESTAMP - INTERVAL '38' DAY, (SELECT id FROM settlements WHERE osm_id = 337547925)
FROM users u, categories c WHERE u.email = 'seed.andrii@example.com' AND c.name = 'Риштування та драбини'
AND NOT EXISTS (SELECT 1 FROM equipment_photos WHERE storage_key = 'seed/equipment-113.png');
INSERT INTO equipment_photos (equipment_id, storage_key, position, created_at)
SELECT MAX(e.id), 'seed/equipment-113.png', 0, CURRENT_TIMESTAMP FROM equipment e JOIN users u ON u.id = e.owner_id
WHERE e.title = 'Риштування пересувне 4 м' AND u.email = 'seed.andrii@example.com'
AND NOT EXISTS (SELECT 1 FROM equipment_photos WHERE storage_key = 'seed/equipment-113.png')
HAVING MAX(e.id) IS NOT NULL;

-- 114. Кущоріз Bosch AHS 50
INSERT INTO equipment (owner_id, category_id, title, description, item_condition, price_per_day, quantity, min_days, max_days, pickup_available, delivery_available, delivery_note, address, city, lat, lon, status, created_at, updated_at, settlement_id)
SELECT u.id, c.id, 'Кущоріз Bosch AHS 50', 'Акуратне використання, без пошкоджень. Перед видачею чищу й перевіряю.', 'NEW', 430.00, 1, 1, 14, FALSE, TRUE, 'Привезу в межах міста за 150 ₴', 'вул. Володимира Великого, 29', 'Львів', 49.842842, 24.069944, 'ACTIVE', CURRENT_TIMESTAMP - INTERVAL '15' DAY, CURRENT_TIMESTAMP - INTERVAL '15' DAY, (SELECT id FROM settlements WHERE osm_id = 26150791)
FROM users u, categories c WHERE u.email = 'seed.olena@example.com' AND c.name = 'Садова техніка'
AND NOT EXISTS (SELECT 1 FROM equipment_photos WHERE storage_key = 'seed/equipment-114.png');
INSERT INTO equipment_photos (equipment_id, storage_key, position, created_at)
SELECT MAX(e.id), 'seed/equipment-114.png', 0, CURRENT_TIMESTAMP FROM equipment e JOIN users u ON u.id = e.owner_id
WHERE e.title = 'Кущоріз Bosch AHS 50' AND u.email = 'seed.olena@example.com'
AND NOT EXISTS (SELECT 1 FROM equipment_photos WHERE storage_key = 'seed/equipment-114.png')
HAVING MAX(e.id) IS NOT NULL;

-- 115. Снігоприбирач акумуляторний Greenworks
INSERT INTO equipment (owner_id, category_id, title, description, item_condition, price_per_day, quantity, min_days, max_days, pickup_available, delivery_available, delivery_note, address, city, lat, lon, status, created_at, updated_at, settlement_id)
SELECT u.id, c.id, 'Снігоприбирач акумуляторний Greenworks', 'У хорошому стані, регулярно обслуговується. Можу підказати, як найкраще використати.', 'GOOD', 590.00, 3, 2, NULL, TRUE, FALSE, NULL, 'вул. Зелена, 16', 'Львів', 49.794995, 24.091252, 'ACTIVE', CURRENT_TIMESTAMP - INTERVAL '15' DAY, CURRENT_TIMESTAMP - INTERVAL '15' DAY, (SELECT id FROM settlements WHERE osm_id = 26150791)
FROM users u, categories c WHERE u.email = 'seed.andrii@example.com' AND c.name = 'Снігоприбиральна техніка'
AND NOT EXISTS (SELECT 1 FROM equipment_photos WHERE storage_key = 'seed/equipment-115.png');
INSERT INTO equipment_photos (equipment_id, storage_key, position, created_at)
SELECT MAX(e.id), 'seed/equipment-115.png', 0, CURRENT_TIMESTAMP FROM equipment e JOIN users u ON u.id = e.owner_id
WHERE e.title = 'Снігоприбирач акумуляторний Greenworks' AND u.email = 'seed.andrii@example.com'
AND NOT EXISTS (SELECT 1 FROM equipment_photos WHERE storage_key = 'seed/equipment-115.png')
HAVING MAX(e.id) IS NOT NULL;
INSERT INTO equipment_photos (equipment_id, storage_key, position, created_at)
SELECT MAX(e.id), 'seed/equipment-115-2.png', 1, CURRENT_TIMESTAMP FROM equipment e JOIN users u ON u.id = e.owner_id
WHERE e.title = 'Снігоприбирач акумуляторний Greenworks' AND u.email = 'seed.andrii@example.com'
AND NOT EXISTS (SELECT 1 FROM equipment_photos WHERE storage_key = 'seed/equipment-115-2.png')
HAVING MAX(e.id) IS NOT NULL;

-- 116. Миючий пилосос Kärcher Puzzi 10/1
INSERT INTO equipment (owner_id, category_id, title, description, item_condition, price_per_day, quantity, min_days, max_days, pickup_available, delivery_available, delivery_note, address, city, lat, lon, status, created_at, updated_at, settlement_id)
SELECT u.id, c.id, 'Миючий пилосос Kärcher Puzzi 10/1', 'Акуратне використання, без пошкоджень. Перед видачею чищу й перевіряю.', 'EXCELLENT', 350.00, 1, 1, NULL, TRUE, FALSE, NULL, 'просп. Червоної Калини, 47', 'Львів', 49.853080, 23.967985, 'ACTIVE', CURRENT_TIMESTAMP - INTERVAL '50' DAY, CURRENT_TIMESTAMP - INTERVAL '50' DAY, (SELECT id FROM settlements WHERE osm_id = 26150791)
FROM users u, categories c WHERE u.email = 'seed.taras@example.com' AND c.name = 'Клінінгове обладнання'
AND NOT EXISTS (SELECT 1 FROM equipment_photos WHERE storage_key = 'seed/equipment-116.png');
INSERT INTO equipment_photos (equipment_id, storage_key, position, created_at)
SELECT MAX(e.id), 'seed/equipment-116.png', 0, CURRENT_TIMESTAMP FROM equipment e JOIN users u ON u.id = e.owner_id
WHERE e.title = 'Миючий пилосос Kärcher Puzzi 10/1' AND u.email = 'seed.taras@example.com'
AND NOT EXISTS (SELECT 1 FROM equipment_photos WHERE storage_key = 'seed/equipment-116.png')
HAVING MAX(e.id) IS NOT NULL;

-- 117. Дрон DJI Mini 4 Pro
INSERT INTO equipment (owner_id, category_id, title, description, item_condition, price_per_day, quantity, min_days, max_days, pickup_available, delivery_available, delivery_note, address, city, lat, lon, status, created_at, updated_at, settlement_id)
SELECT u.id, c.id, 'Дрон DJI Mini 4 Pro', 'У хорошому стані, регулярно обслуговується. Можу підказати, як найкраще використати.', 'EXCELLENT', 1240.00, 2, 1, 7, FALSE, TRUE, 'Привезу в межах міста за 150 ₴', 'вул. Стрийська, 61', 'Львів', 49.856905, 23.960067, 'ACTIVE', CURRENT_TIMESTAMP - INTERVAL '42' DAY, CURRENT_TIMESTAMP - INTERVAL '42' DAY, (SELECT id FROM settlements WHERE osm_id = 26150791)
FROM users u, categories c WHERE u.email = 'seed.fotozona@example.com' AND c.name = 'Фото- та відеотехніка'
AND NOT EXISTS (SELECT 1 FROM equipment_photos WHERE storage_key = 'seed/equipment-117.png');
INSERT INTO equipment_photos (equipment_id, storage_key, position, created_at)
SELECT MAX(e.id), 'seed/equipment-117.png', 0, CURRENT_TIMESTAMP FROM equipment e JOIN users u ON u.id = e.owner_id
WHERE e.title = 'Дрон DJI Mini 4 Pro' AND u.email = 'seed.fotozona@example.com'
AND NOT EXISTS (SELECT 1 FROM equipment_photos WHERE storage_key = 'seed/equipment-117.png')
HAVING MAX(e.id) IS NOT NULL;

-- 118. Комплект студійного світла 3×LED
INSERT INTO equipment (owner_id, category_id, title, description, item_condition, price_per_day, quantity, min_days, max_days, pickup_available, delivery_available, delivery_note, address, city, lat, lon, status, created_at, updated_at, settlement_id)
SELECT u.id, c.id, 'Комплект студійного світла 3×LED', 'Акуратне використання, без пошкоджень. Перед видачею чищу й перевіряю.', 'NEW', 740.00, 5, 3, NULL, TRUE, FALSE, NULL, 'вул. Шевченка, 114', 'Львів', 49.851490, 24.010740, 'ACTIVE', CURRENT_TIMESTAMP - INTERVAL '56' DAY, CURRENT_TIMESTAMP - INTERVAL '56' DAY, (SELECT id FROM settlements WHERE osm_id = 26150791)
FROM users u, categories c WHERE u.email = 'seed.olena@example.com' AND c.name = 'Світло та звук'
AND NOT EXISTS (SELECT 1 FROM equipment_photos WHERE storage_key = 'seed/equipment-118.png');
INSERT INTO equipment_photos (equipment_id, storage_key, position, created_at)
SELECT MAX(e.id), 'seed/equipment-118.png', 0, CURRENT_TIMESTAMP FROM equipment e JOIN users u ON u.id = e.owner_id
WHERE e.title = 'Комплект студійного світла 3×LED' AND u.email = 'seed.olena@example.com'
AND NOT EXISTS (SELECT 1 FROM equipment_photos WHERE storage_key = 'seed/equipment-118.png')
HAVING MAX(e.id) IS NOT NULL;

-- 119. Туристичний пальник з балоном
INSERT INTO equipment (owner_id, category_id, title, description, item_condition, price_per_day, quantity, min_days, max_days, pickup_available, delivery_available, delivery_note, address, city, lat, lon, status, created_at, updated_at, settlement_id)
SELECT u.id, c.id, 'Туристичний пальник з балоном', 'Надійний і простий у використанні. Повернути прошу в чистому вигляді.', 'EXCELLENT', 310.00, 2, 1, 30, TRUE, FALSE, NULL, 'вул. Княгині Ольги, 116', 'Львів', 49.865589, 24.041396, 'ACTIVE', CURRENT_TIMESTAMP - INTERVAL '57' DAY, CURRENT_TIMESTAMP - INTERVAL '57' DAY, (SELECT id FROM settlements WHERE osm_id = 26150791)
FROM users u, categories c WHERE u.email = 'seed.taras@example.com' AND c.name = 'Туристичне спорядження'
AND NOT EXISTS (SELECT 1 FROM equipment_photos WHERE storage_key = 'seed/equipment-119.png');
INSERT INTO equipment_photos (equipment_id, storage_key, position, created_at)
SELECT MAX(e.id), 'seed/equipment-119.png', 0, CURRENT_TIMESTAMP FROM equipment e JOIN users u ON u.id = e.owner_id
WHERE e.title = 'Туристичний пальник з балоном' AND u.email = 'seed.taras@example.com'
AND NOT EXISTS (SELECT 1 FROM equipment_photos WHERE storage_key = 'seed/equipment-119.png')
HAVING MAX(e.id) IS NOT NULL;

-- 120. Ножичний підйомник 8 м
INSERT INTO equipment (owner_id, category_id, title, description, item_condition, price_per_day, quantity, min_days, max_days, pickup_available, delivery_available, delivery_note, address, city, lat, lon, status, created_at, updated_at, settlement_id)
SELECT u.id, c.id, 'Ножичний підйомник 8 м', 'У хорошому стані, регулярно обслуговується. Можу підказати, як найкраще використати.', 'GOOD', 4790.00, 1, 1, 7, TRUE, TRUE, 'Доставка по Львову, вартість за домовленістю', 'вул. Зелена, 63', 'Львів', 49.841000, 23.973242, 'ACTIVE', CURRENT_TIMESTAMP - INTERVAL '10' DAY, CURRENT_TIMESTAMP - INTERVAL '10' DAY, (SELECT id FROM settlements WHERE osm_id = 26150791)
FROM users u, categories c WHERE u.email = 'seed.budprokat@example.com' AND c.name = 'Спецтехніка'
AND NOT EXISTS (SELECT 1 FROM equipment_photos WHERE storage_key = 'seed/equipment-120.png');
INSERT INTO equipment_photos (equipment_id, storage_key, position, created_at)
SELECT MAX(e.id), 'seed/equipment-120.png', 0, CURRENT_TIMESTAMP FROM equipment e JOIN users u ON u.id = e.owner_id
WHERE e.title = 'Ножичний підйомник 8 м' AND u.email = 'seed.budprokat@example.com'
AND NOT EXISTS (SELECT 1 FROM equipment_photos WHERE storage_key = 'seed/equipment-120.png')
HAVING MAX(e.id) IS NOT NULL;
INSERT INTO equipment_photos (equipment_id, storage_key, position, created_at)
SELECT MAX(e.id), 'seed/equipment-120-2.png', 1, CURRENT_TIMESTAMP FROM equipment e JOIN users u ON u.id = e.owner_id
WHERE e.title = 'Ножичний підйомник 8 м' AND u.email = 'seed.budprokat@example.com'
AND NOT EXISTS (SELECT 1 FROM equipment_photos WHERE storage_key = 'seed/equipment-120-2.png')
HAVING MAX(e.id) IS NOT NULL;

-- 121. Кутова шліфмашина DeWalt DWE4157
INSERT INTO equipment (owner_id, category_id, title, description, item_condition, price_per_day, quantity, min_days, max_days, pickup_available, delivery_available, delivery_note, address, city, lat, lon, status, created_at, updated_at, settlement_id)
SELECT u.id, c.id, 'Кутова шліфмашина DeWalt DWE4157', 'Акуратне використання, без пошкоджень. Перед видачею чищу й перевіряю.', 'FAIR', 370.00, 1, 1, 30, TRUE, FALSE, NULL, 'просп. Свободи, 50', 'Львів', 49.869378, 23.963250, 'ACTIVE', CURRENT_TIMESTAMP - INTERVAL '33' DAY, CURRENT_TIMESTAMP - INTERVAL '33' DAY, (SELECT id FROM settlements WHERE osm_id = 26150791)
FROM users u, categories c WHERE u.email = 'seed.olena@example.com' AND c.name = 'Електроінструмент'
AND NOT EXISTS (SELECT 1 FROM equipment_photos WHERE storage_key = 'seed/equipment-121.png');
INSERT INTO equipment_photos (equipment_id, storage_key, position, created_at)
SELECT MAX(e.id), 'seed/equipment-121.png', 0, CURRENT_TIMESTAMP FROM equipment e JOIN users u ON u.id = e.owner_id
WHERE e.title = 'Кутова шліфмашина DeWalt DWE4157' AND u.email = 'seed.olena@example.com'
AND NOT EXISTS (SELECT 1 FROM equipment_photos WHERE storage_key = 'seed/equipment-121.png')
HAVING MAX(e.id) IS NOT NULL;

-- 122. Відбійний молоток Makita HM1307
INSERT INTO equipment (owner_id, category_id, title, description, item_condition, price_per_day, quantity, min_days, max_days, pickup_available, delivery_available, delivery_note, address, city, lat, lon, status, created_at, updated_at, settlement_id)
SELECT u.id, c.id, 'Відбійний молоток Makita HM1307', 'Акуратне використання, без пошкоджень. Перед видачею чищу й перевіряю.', 'NEW', 1130.00, 2, 2, 14, FALSE, TRUE, 'Доставка по Львову, вартість за домовленістю', 'вул. Пасічна, 73', 'Львів', 49.826415, 24.030589, 'ACTIVE', CURRENT_TIMESTAMP - INTERVAL '49' DAY, CURRENT_TIMESTAMP - INTERVAL '49' DAY, (SELECT id FROM settlements WHERE osm_id = 26150791)
FROM users u, categories c WHERE u.email = 'seed.budprokat@example.com' AND c.name = 'Будівельне обладнання'
AND NOT EXISTS (SELECT 1 FROM equipment_photos WHERE storage_key = 'seed/equipment-122.png');
INSERT INTO equipment_photos (equipment_id, storage_key, position, created_at)
SELECT MAX(e.id), 'seed/equipment-122.png', 0, CURRENT_TIMESTAMP FROM equipment e JOIN users u ON u.id = e.owner_id
WHERE e.title = 'Відбійний молоток Makita HM1307' AND u.email = 'seed.budprokat@example.com'
AND NOT EXISTS (SELECT 1 FROM equipment_photos WHERE storage_key = 'seed/equipment-122.png')
HAVING MAX(e.id) IS NOT NULL;

-- 123. Драбина-трансформер 4×4
INSERT INTO equipment (owner_id, category_id, title, description, item_condition, price_per_day, quantity, min_days, max_days, pickup_available, delivery_available, delivery_note, address, city, lat, lon, status, created_at, updated_at, settlement_id)
SELECT u.id, c.id, 'Драбина-трансформер 4×4', 'Підходить для домашніх і професійних робіт. Видаю з кейсом і комплектом аксесуарів.', 'GOOD', 380.00, 1, 1, NULL, TRUE, FALSE, NULL, 'вул. Шевченка, 78', 'Львів', 49.867893, 24.084333, 'ACTIVE', CURRENT_TIMESTAMP - INTERVAL '20' DAY, CURRENT_TIMESTAMP - INTERVAL '20' DAY, (SELECT id FROM settlements WHERE osm_id = 26150791)
FROM users u, categories c WHERE u.email = 'seed.marta@example.com' AND c.name = 'Риштування та драбини'
AND NOT EXISTS (SELECT 1 FROM equipment_photos WHERE storage_key = 'seed/equipment-123.png');
INSERT INTO equipment_photos (equipment_id, storage_key, position, created_at)
SELECT MAX(e.id), 'seed/equipment-123.png', 0, CURRENT_TIMESTAMP FROM equipment e JOIN users u ON u.id = e.owner_id
WHERE e.title = 'Драбина-трансформер 4×4' AND u.email = 'seed.marta@example.com'
AND NOT EXISTS (SELECT 1 FROM equipment_photos WHERE storage_key = 'seed/equipment-123.png')
HAVING MAX(e.id) IS NOT NULL;

-- 124. Повітродувка Makita UB1103
INSERT INTO equipment (owner_id, category_id, title, description, item_condition, price_per_day, quantity, min_days, max_days, pickup_available, delivery_available, delivery_note, address, city, lat, lon, status, created_at, updated_at, settlement_id)
SELECT u.id, c.id, 'Повітродувка Makita UB1103', 'Справний, перевірений перед кожною орендою. Видаю з інструкцією та всім потрібним для роботи.', 'EXCELLENT', 560.00, 2, 3, 7, TRUE, FALSE, NULL, 'вул. Сихівська, 34', 'Львів', 49.843847, 23.963914, 'ACTIVE', CURRENT_TIMESTAMP - INTERVAL '46' DAY, CURRENT_TIMESTAMP - INTERVAL '46' DAY, (SELECT id FROM settlements WHERE osm_id = 26150791)
FROM users u, categories c WHERE u.email = 'seed.olena@example.com' AND c.name = 'Садова техніка'
AND NOT EXISTS (SELECT 1 FROM equipment_photos WHERE storage_key = 'seed/equipment-124.png');
INSERT INTO equipment_photos (equipment_id, storage_key, position, created_at)
SELECT MAX(e.id), 'seed/equipment-124.png', 0, CURRENT_TIMESTAMP FROM equipment e JOIN users u ON u.id = e.owner_id
WHERE e.title = 'Повітродувка Makita UB1103' AND u.email = 'seed.olena@example.com'
AND NOT EXISTS (SELECT 1 FROM equipment_photos WHERE storage_key = 'seed/equipment-124.png')
HAVING MAX(e.id) IS NOT NULL;

-- 125. Снігоприбирач бензиновий Honda HSS 655
INSERT INTO equipment (owner_id, category_id, title, description, item_condition, price_per_day, quantity, min_days, max_days, pickup_available, delivery_available, delivery_note, address, city, lat, lon, status, created_at, updated_at, settlement_id)
SELECT u.id, c.id, 'Снігоприбирач бензиновий Honda HSS 655', 'Надійний і простий у використанні. Повернути прошу в чистому вигляді.', 'GOOD', 900.00, 1, 1, 7, TRUE, TRUE, 'Доставка по Львову та області, ціну узгодимо', 'вул. Пасічна, 72', 'Львів', 49.852524, 23.969741, 'ACTIVE', CURRENT_TIMESTAMP - INTERVAL '51' DAY, CURRENT_TIMESTAMP - INTERVAL '51' DAY, (SELECT id FROM settlements WHERE osm_id = 26150791)
FROM users u, categories c WHERE u.email = 'seed.andrii@example.com' AND c.name = 'Снігоприбиральна техніка'
AND NOT EXISTS (SELECT 1 FROM equipment_photos WHERE storage_key = 'seed/equipment-125.png');
INSERT INTO equipment_photos (equipment_id, storage_key, position, created_at)
SELECT MAX(e.id), 'seed/equipment-125.png', 0, CURRENT_TIMESTAMP FROM equipment e JOIN users u ON u.id = e.owner_id
WHERE e.title = 'Снігоприбирач бензиновий Honda HSS 655' AND u.email = 'seed.andrii@example.com'
AND NOT EXISTS (SELECT 1 FROM equipment_photos WHERE storage_key = 'seed/equipment-125.png')
HAVING MAX(e.id) IS NOT NULL;
INSERT INTO equipment_photos (equipment_id, storage_key, position, created_at)
SELECT MAX(e.id), 'seed/equipment-125-2.png', 1, CURRENT_TIMESTAMP FROM equipment e JOIN users u ON u.id = e.owner_id
WHERE e.title = 'Снігоприбирач бензиновий Honda HSS 655' AND u.email = 'seed.andrii@example.com'
AND NOT EXISTS (SELECT 1 FROM equipment_photos WHERE storage_key = 'seed/equipment-125-2.png')
HAVING MAX(e.id) IS NOT NULL;

-- 126. Будівельний пилосос Makita VC2512L
INSERT INTO equipment (owner_id, category_id, title, description, item_condition, price_per_day, quantity, min_days, max_days, pickup_available, delivery_available, delivery_note, address, city, lat, lon, status, created_at, updated_at, settlement_id)
SELECT u.id, c.id, 'Будівельний пилосос Makita VC2512L', 'Справний, перевірений перед кожною орендою. Видаю з інструкцією та всім потрібним для роботи.', 'EXCELLENT', 370.00, 1, 2, 14, TRUE, FALSE, NULL, 'вул. Городоцька, 42', 'Львів', 49.799480, 24.023084, 'ACTIVE', CURRENT_TIMESTAMP - INTERVAL '40' DAY, CURRENT_TIMESTAMP - INTERVAL '40' DAY, (SELECT id FROM settlements WHERE osm_id = 26150791)
FROM users u, categories c WHERE u.email = 'seed.andrii@example.com' AND c.name = 'Клінінгове обладнання'
AND NOT EXISTS (SELECT 1 FROM equipment_photos WHERE storage_key = 'seed/equipment-126.png');
INSERT INTO equipment_photos (equipment_id, storage_key, position, created_at)
SELECT MAX(e.id), 'seed/equipment-126.png', 0, CURRENT_TIMESTAMP FROM equipment e JOIN users u ON u.id = e.owner_id
WHERE e.title = 'Будівельний пилосос Makita VC2512L' AND u.email = 'seed.andrii@example.com'
AND NOT EXISTS (SELECT 1 FROM equipment_photos WHERE storage_key = 'seed/equipment-126.png')
HAVING MAX(e.id) IS NOT NULL;

-- 127. Стабілізатор DJI RS 3
INSERT INTO equipment (owner_id, category_id, title, description, item_condition, price_per_day, quantity, min_days, max_days, pickup_available, delivery_available, delivery_note, address, city, lat, lon, status, created_at, updated_at, settlement_id)
SELECT u.id, c.id, 'Стабілізатор DJI RS 3', 'У хорошому стані, регулярно обслуговується. Можу підказати, як найкраще використати.', 'GOOD', 830.00, 1, 1, 30, FALSE, TRUE, 'Доставка по Львову, вартість за домовленістю', 'вул. Виговського, 72', 'Львів', 49.843174, 24.038524, 'ACTIVE', CURRENT_TIMESTAMP - INTERVAL '27' DAY, CURRENT_TIMESTAMP - INTERVAL '27' DAY, (SELECT id FROM settlements WHERE osm_id = 26150791)
FROM users u, categories c WHERE u.email = 'seed.taras@example.com' AND c.name = 'Фото- та відеотехніка'
AND NOT EXISTS (SELECT 1 FROM equipment_photos WHERE storage_key = 'seed/equipment-127.png');
INSERT INTO equipment_photos (equipment_id, storage_key, position, created_at)
SELECT MAX(e.id), 'seed/equipment-127.png', 0, CURRENT_TIMESTAMP FROM equipment e JOIN users u ON u.id = e.owner_id
WHERE e.title = 'Стабілізатор DJI RS 3' AND u.email = 'seed.taras@example.com'
AND NOT EXISTS (SELECT 1 FROM equipment_photos WHERE storage_key = 'seed/equipment-127.png')
HAVING MAX(e.id) IS NOT NULL;

-- 128. Радіосистема Shure BLX24
INSERT INTO equipment (owner_id, category_id, title, description, item_condition, price_per_day, quantity, min_days, max_days, pickup_available, delivery_available, delivery_note, address, city, lat, lon, status, created_at, updated_at, settlement_id)
SELECT u.id, c.id, 'Радіосистема Shure BLX24', 'У хорошому стані, регулярно обслуговується. Можу підказати, як найкраще використати.', 'GOOD', 520.00, 5, 1, 14, TRUE, FALSE, NULL, 'вул. Центральна, 32', 'Городок', 49.779611, 23.650781, 'ACTIVE', CURRENT_TIMESTAMP - INTERVAL '42' DAY, CURRENT_TIMESTAMP - INTERVAL '42' DAY, (SELECT id FROM settlements WHERE osm_id = 261709042)
FROM users u, categories c WHERE u.email = 'seed.fotozona@example.com' AND c.name = 'Світло та звук'
AND NOT EXISTS (SELECT 1 FROM equipment_photos WHERE storage_key = 'seed/equipment-128.png');
INSERT INTO equipment_photos (equipment_id, storage_key, position, created_at)
SELECT MAX(e.id), 'seed/equipment-128.png', 0, CURRENT_TIMESTAMP FROM equipment e JOIN users u ON u.id = e.owner_id
WHERE e.title = 'Радіосистема Shure BLX24' AND u.email = 'seed.fotozona@example.com'
AND NOT EXISTS (SELECT 1 FROM equipment_photos WHERE storage_key = 'seed/equipment-128.png')
HAVING MAX(e.id) IS NOT NULL;

-- 129. Намет 4-місний Tramp
INSERT INTO equipment (owner_id, category_id, title, description, item_condition, price_per_day, quantity, min_days, max_days, pickup_available, delivery_available, delivery_note, address, city, lat, lon, status, created_at, updated_at, settlement_id)
SELECT u.id, c.id, 'Намет 4-місний Tramp', 'Справний, перевірений перед кожною орендою. Видаю з інструкцією та всім потрібним для роботи.', 'FAIR', 240.00, 3, 1, NULL, TRUE, FALSE, NULL, 'вул. Городоцька, 42', 'Львів', 49.866909, 24.003601, 'ARCHIVED', CURRENT_TIMESTAMP - INTERVAL '60' DAY, CURRENT_TIMESTAMP - INTERVAL '60' DAY, (SELECT id FROM settlements WHERE osm_id = 26150791)
FROM users u, categories c WHERE u.email = 'seed.andrii@example.com' AND c.name = 'Туристичне спорядження'
AND NOT EXISTS (SELECT 1 FROM equipment_photos WHERE storage_key = 'seed/equipment-129.png');
INSERT INTO equipment_photos (equipment_id, storage_key, position, created_at)
SELECT MAX(e.id), 'seed/equipment-129.png', 0, CURRENT_TIMESTAMP FROM equipment e JOIN users u ON u.id = e.owner_id
WHERE e.title = 'Намет 4-місний Tramp' AND u.email = 'seed.andrii@example.com'
AND NOT EXISTS (SELECT 1 FROM equipment_photos WHERE storage_key = 'seed/equipment-129.png')
HAVING MAX(e.id) IS NOT NULL;

-- 130. Мотопомпа для брудної води
INSERT INTO equipment (owner_id, category_id, title, description, item_condition, price_per_day, quantity, min_days, max_days, pickup_available, delivery_available, delivery_note, address, city, lat, lon, status, created_at, updated_at, settlement_id)
SELECT u.id, c.id, 'Мотопомпа для брудної води', 'Справний, перевірений перед кожною орендою. Видаю з інструкцією та всім потрібним для роботи.', 'GOOD', 4270.00, 1, 1, NULL, TRUE, TRUE, 'Доставка по Львову та області, ціну узгодимо', 'вул. Пасічна, 13', 'Львів', 49.855647, 24.039077, 'ACTIVE', CURRENT_TIMESTAMP - INTERVAL '36' DAY, CURRENT_TIMESTAMP - INTERVAL '36' DAY, (SELECT id FROM settlements WHERE osm_id = 26150791)
FROM users u, categories c WHERE u.email = 'seed.budprokat@example.com' AND c.name = 'Спецтехніка'
AND NOT EXISTS (SELECT 1 FROM equipment_photos WHERE storage_key = 'seed/equipment-130.png');
INSERT INTO equipment_photos (equipment_id, storage_key, position, created_at)
SELECT MAX(e.id), 'seed/equipment-130.png', 0, CURRENT_TIMESTAMP FROM equipment e JOIN users u ON u.id = e.owner_id
WHERE e.title = 'Мотопомпа для брудної води' AND u.email = 'seed.budprokat@example.com'
AND NOT EXISTS (SELECT 1 FROM equipment_photos WHERE storage_key = 'seed/equipment-130.png')
HAVING MAX(e.id) IS NOT NULL;
INSERT INTO equipment_photos (equipment_id, storage_key, position, created_at)
SELECT MAX(e.id), 'seed/equipment-130-2.png', 1, CURRENT_TIMESTAMP FROM equipment e JOIN users u ON u.id = e.owner_id
WHERE e.title = 'Мотопомпа для брудної води' AND u.email = 'seed.budprokat@example.com'
AND NOT EXISTS (SELECT 1 FROM equipment_photos WHERE storage_key = 'seed/equipment-130-2.png')
HAVING MAX(e.id) IS NOT NULL;

-- 131. Електролобзик Bosch PST 700
INSERT INTO equipment (owner_id, category_id, title, description, item_condition, price_per_day, quantity, min_days, max_days, pickup_available, delivery_available, delivery_note, address, city, lat, lon, status, created_at, updated_at, settlement_id)
SELECT u.id, c.id, 'Електролобзик Bosch PST 700', 'Підходить для домашніх і професійних робіт. Видаю з кейсом і комплектом аксесуарів.', 'GOOD', 220.00, 2, 1, 30, TRUE, TRUE, 'Привезу в межах міста за 150 ₴', 'вул. Сихівська, 40', 'Львів', 49.852777, 24.022219, 'ACTIVE', CURRENT_TIMESTAMP - INTERVAL '38' DAY, CURRENT_TIMESTAMP - INTERVAL '38' DAY, (SELECT id FROM settlements WHERE osm_id = 26150791)
FROM users u, categories c WHERE u.email = 'seed.andrii@example.com' AND c.name = 'Електроінструмент'
AND NOT EXISTS (SELECT 1 FROM equipment_photos WHERE storage_key = 'seed/equipment-131.png');
INSERT INTO equipment_photos (equipment_id, storage_key, position, created_at)
SELECT MAX(e.id), 'seed/equipment-131.png', 0, CURRENT_TIMESTAMP FROM equipment e JOIN users u ON u.id = e.owner_id
WHERE e.title = 'Електролобзик Bosch PST 700' AND u.email = 'seed.andrii@example.com'
AND NOT EXISTS (SELECT 1 FROM equipment_photos WHERE storage_key = 'seed/equipment-131.png')
HAVING MAX(e.id) IS NOT NULL;

-- 132. Плиткоріз електричний 800 мм
INSERT INTO equipment (owner_id, category_id, title, description, item_condition, price_per_day, quantity, min_days, max_days, pickup_available, delivery_available, delivery_note, address, city, lat, lon, status, created_at, updated_at, settlement_id)
SELECT u.id, c.id, 'Плиткоріз електричний 800 мм', 'Підходить для домашніх і професійних робіт. Видаю з кейсом і комплектом аксесуарів.', 'FAIR', 370.00, 2, 1, 14, TRUE, FALSE, NULL, 'вул. Мазепи, 26', 'Львів', 49.882210, 24.021908, 'ACTIVE', CURRENT_TIMESTAMP - INTERVAL '1' DAY, CURRENT_TIMESTAMP - INTERVAL '1' DAY, (SELECT id FROM settlements WHERE osm_id = 26150791)
FROM users u, categories c WHERE u.email = 'seed.budprokat@example.com' AND c.name = 'Будівельне обладнання'
AND NOT EXISTS (SELECT 1 FROM equipment_photos WHERE storage_key = 'seed/equipment-132.png');
INSERT INTO equipment_photos (equipment_id, storage_key, position, created_at)
SELECT MAX(e.id), 'seed/equipment-132.png', 0, CURRENT_TIMESTAMP FROM equipment e JOIN users u ON u.id = e.owner_id
WHERE e.title = 'Плиткоріз електричний 800 мм' AND u.email = 'seed.budprokat@example.com'
AND NOT EXISTS (SELECT 1 FROM equipment_photos WHERE storage_key = 'seed/equipment-132.png')
HAVING MAX(e.id) IS NOT NULL;

-- 133. Помост малярний 1,5 м
INSERT INTO equipment (owner_id, category_id, title, description, item_condition, price_per_day, quantity, min_days, max_days, pickup_available, delivery_available, delivery_note, address, city, lat, lon, status, created_at, updated_at, settlement_id)
SELECT u.id, c.id, 'Помост малярний 1,5 м', 'Акуратне використання, без пошкоджень. Перед видачею чищу й перевіряю.', 'FAIR', 530.00, 1, 3, 7, TRUE, TRUE, NULL, 'просп. Свободи, 46', 'Львів', 49.863401, 24.020146, 'ACTIVE', CURRENT_TIMESTAMP - INTERVAL '22' DAY, CURRENT_TIMESTAMP - INTERVAL '22' DAY, (SELECT id FROM settlements WHERE osm_id = 26150791)
FROM users u, categories c WHERE u.email = 'seed.taras@example.com' AND c.name = 'Риштування та драбини'
AND NOT EXISTS (SELECT 1 FROM equipment_photos WHERE storage_key = 'seed/equipment-133.png');
INSERT INTO equipment_photos (equipment_id, storage_key, position, created_at)
SELECT MAX(e.id), 'seed/equipment-133.png', 0, CURRENT_TIMESTAMP FROM equipment e JOIN users u ON u.id = e.owner_id
WHERE e.title = 'Помост малярний 1,5 м' AND u.email = 'seed.taras@example.com'
AND NOT EXISTS (SELECT 1 FROM equipment_photos WHERE storage_key = 'seed/equipment-133.png')
HAVING MAX(e.id) IS NOT NULL;

-- 134. Подрібнювач гілок Einhell
INSERT INTO equipment (owner_id, category_id, title, description, item_condition, price_per_day, quantity, min_days, max_days, pickup_available, delivery_available, delivery_note, address, city, lat, lon, status, created_at, updated_at, settlement_id)
SELECT u.id, c.id, 'Подрібнювач гілок Einhell', 'Справний, перевірений перед кожною орендою. Видаю з інструкцією та всім потрібним для роботи.', 'GOOD', 280.00, 1, 1, 30, TRUE, FALSE, NULL, 'вул. Мазепи, 75', 'Львів', 49.862156, 24.053245, 'ACTIVE', CURRENT_TIMESTAMP - INTERVAL '4' DAY, CURRENT_TIMESTAMP - INTERVAL '4' DAY, (SELECT id FROM settlements WHERE osm_id = 26150791)
FROM users u, categories c WHERE u.email = 'seed.marta@example.com' AND c.name = 'Садова техніка'
AND NOT EXISTS (SELECT 1 FROM equipment_photos WHERE storage_key = 'seed/equipment-134.png');
INSERT INTO equipment_photos (equipment_id, storage_key, position, created_at)
SELECT MAX(e.id), 'seed/equipment-134.png', 0, CURRENT_TIMESTAMP FROM equipment e JOIN users u ON u.id = e.owner_id
WHERE e.title = 'Подрібнювач гілок Einhell' AND u.email = 'seed.marta@example.com'
AND NOT EXISTS (SELECT 1 FROM equipment_photos WHERE storage_key = 'seed/equipment-134.png')
HAVING MAX(e.id) IS NOT NULL;

-- 135. Снігоприбирач електричний Einhell
INSERT INTO equipment (owner_id, category_id, title, description, item_condition, price_per_day, quantity, min_days, max_days, pickup_available, delivery_available, delivery_note, address, city, lat, lon, status, created_at, updated_at, settlement_id)
SELECT u.id, c.id, 'Снігоприбирач електричний Einhell', 'Справний, перевірений перед кожною орендою. Видаю з інструкцією та всім потрібним для роботи.', 'EXCELLENT', 720.00, 1, 1, 7, FALSE, TRUE, 'Доставка по Львову, вартість за домовленістю', 'вул. Городоцька, 62', 'Львів', 49.835221, 24.093203, 'ACTIVE', CURRENT_TIMESTAMP - INTERVAL '3' DAY, CURRENT_TIMESTAMP - INTERVAL '3' DAY, (SELECT id FROM settlements WHERE osm_id = 26150791)
FROM users u, categories c WHERE u.email = 'seed.olena@example.com' AND c.name = 'Снігоприбиральна техніка'
AND NOT EXISTS (SELECT 1 FROM equipment_photos WHERE storage_key = 'seed/equipment-135.png');
INSERT INTO equipment_photos (equipment_id, storage_key, position, created_at)
SELECT MAX(e.id), 'seed/equipment-135.png', 0, CURRENT_TIMESTAMP FROM equipment e JOIN users u ON u.id = e.owner_id
WHERE e.title = 'Снігоприбирач електричний Einhell' AND u.email = 'seed.olena@example.com'
AND NOT EXISTS (SELECT 1 FROM equipment_photos WHERE storage_key = 'seed/equipment-135.png')
HAVING MAX(e.id) IS NOT NULL;
INSERT INTO equipment_photos (equipment_id, storage_key, position, created_at)
SELECT MAX(e.id), 'seed/equipment-135-2.png', 1, CURRENT_TIMESTAMP FROM equipment e JOIN users u ON u.id = e.owner_id
WHERE e.title = 'Снігоприбирач електричний Einhell' AND u.email = 'seed.olena@example.com'
AND NOT EXISTS (SELECT 1 FROM equipment_photos WHERE storage_key = 'seed/equipment-135-2.png')
HAVING MAX(e.id) IS NOT NULL;

-- 136. Парогенератор Kärcher SC 4
INSERT INTO equipment (owner_id, category_id, title, description, item_condition, price_per_day, quantity, min_days, max_days, pickup_available, delivery_available, delivery_note, address, city, lat, lon, status, created_at, updated_at, settlement_id)
SELECT u.id, c.id, 'Парогенератор Kärcher SC 4', 'Справний, перевірений перед кожною орендою. Видаю з інструкцією та всім потрібним для роботи.', 'GOOD', 680.00, 1, 1, NULL, TRUE, FALSE, NULL, 'вул. Кульпарківська, 50', 'Львів', 49.815906, 24.087094, 'ACTIVE', CURRENT_TIMESTAMP - INTERVAL '48' DAY, CURRENT_TIMESTAMP - INTERVAL '48' DAY, (SELECT id FROM settlements WHERE osm_id = 26150791)
FROM users u, categories c WHERE u.email = 'seed.olena@example.com' AND c.name = 'Клінінгове обладнання'
AND NOT EXISTS (SELECT 1 FROM equipment_photos WHERE storage_key = 'seed/equipment-136.png');
INSERT INTO equipment_photos (equipment_id, storage_key, position, created_at)
SELECT MAX(e.id), 'seed/equipment-136.png', 0, CURRENT_TIMESTAMP FROM equipment e JOIN users u ON u.id = e.owner_id
WHERE e.title = 'Парогенератор Kärcher SC 4' AND u.email = 'seed.olena@example.com'
AND NOT EXISTS (SELECT 1 FROM equipment_photos WHERE storage_key = 'seed/equipment-136.png')
HAVING MAX(e.id) IS NOT NULL;

-- 137. Екшн-камера GoPro HERO12
INSERT INTO equipment (owner_id, category_id, title, description, item_condition, price_per_day, quantity, min_days, max_days, pickup_available, delivery_available, delivery_note, address, city, lat, lon, status, created_at, updated_at, settlement_id)
SELECT u.id, c.id, 'Екшн-камера GoPro HERO12', 'Акуратне використання, без пошкоджень. Перед видачею чищу й перевіряю.', 'FAIR', 840.00, 1, 3, 7, TRUE, TRUE, NULL, 'вул. Виговського, 30', 'Львів', 49.832370, 24.037531, 'ACTIVE', CURRENT_TIMESTAMP - INTERVAL '1' DAY, CURRENT_TIMESTAMP - INTERVAL '1' DAY, (SELECT id FROM settlements WHERE osm_id = 26150791)
FROM users u, categories c WHERE u.email = 'seed.fotozona@example.com' AND c.name = 'Фото- та відеотехніка'
AND NOT EXISTS (SELECT 1 FROM equipment_photos WHERE storage_key = 'seed/equipment-137.png');
INSERT INTO equipment_photos (equipment_id, storage_key, position, created_at)
SELECT MAX(e.id), 'seed/equipment-137.png', 0, CURRENT_TIMESTAMP FROM equipment e JOIN users u ON u.id = e.owner_id
WHERE e.title = 'Екшн-камера GoPro HERO12' AND u.email = 'seed.fotozona@example.com'
AND NOT EXISTS (SELECT 1 FROM equipment_photos WHERE storage_key = 'seed/equipment-137.png')
HAVING MAX(e.id) IS NOT NULL;

-- 138. Проєктор Epson EB-X49
INSERT INTO equipment (owner_id, category_id, title, description, item_condition, price_per_day, quantity, min_days, max_days, pickup_available, delivery_available, delivery_note, address, city, lat, lon, status, created_at, updated_at, settlement_id)
SELECT u.id, c.id, 'Проєктор Epson EB-X49', 'Підходить для домашніх і професійних робіт. Видаю з кейсом і комплектом аксесуарів.', 'NEW', 540.00, 1, 2, 30, TRUE, FALSE, NULL, 'вул. Стрийська, 96', 'Львів', 49.804238, 23.960231, 'ACTIVE', CURRENT_TIMESTAMP - INTERVAL '35' DAY, CURRENT_TIMESTAMP - INTERVAL '35' DAY, (SELECT id FROM settlements WHERE osm_id = 26150791)
FROM users u, categories c WHERE u.email = 'seed.fotozona@example.com' AND c.name = 'Світло та звук'
AND NOT EXISTS (SELECT 1 FROM equipment_photos WHERE storage_key = 'seed/equipment-138.png');
INSERT INTO equipment_photos (equipment_id, storage_key, position, created_at)
SELECT MAX(e.id), 'seed/equipment-138.png', 0, CURRENT_TIMESTAMP FROM equipment e JOIN users u ON u.id = e.owner_id
WHERE e.title = 'Проєктор Epson EB-X49' AND u.email = 'seed.fotozona@example.com'
AND NOT EXISTS (SELECT 1 FROM equipment_photos WHERE storage_key = 'seed/equipment-138.png')
HAVING MAX(e.id) IS NOT NULL;

-- 139. Каяк надувний двомісний
INSERT INTO equipment (owner_id, category_id, title, description, item_condition, price_per_day, quantity, min_days, max_days, pickup_available, delivery_available, delivery_note, address, city, lat, lon, status, created_at, updated_at, settlement_id)
SELECT u.id, c.id, 'Каяк надувний двомісний', 'У хорошому стані, регулярно обслуговується. Можу підказати, як найкраще використати.', 'EXCELLENT', 340.00, 1, 2, 30, TRUE, TRUE, 'Доставка по Львову, вартість за домовленістю', 'просп. Свободи, 63', 'Львів', 49.825947, 24.009059, 'ACTIVE', CURRENT_TIMESTAMP - INTERVAL '58' DAY, CURRENT_TIMESTAMP - INTERVAL '58' DAY, (SELECT id FROM settlements WHERE osm_id = 26150791)
FROM users u, categories c WHERE u.email = 'seed.olena@example.com' AND c.name = 'Туристичне спорядження'
AND NOT EXISTS (SELECT 1 FROM equipment_photos WHERE storage_key = 'seed/equipment-139.png');
INSERT INTO equipment_photos (equipment_id, storage_key, position, created_at)
SELECT MAX(e.id), 'seed/equipment-139.png', 0, CURRENT_TIMESTAMP FROM equipment e JOIN users u ON u.id = e.owner_id
WHERE e.title = 'Каяк надувний двомісний' AND u.email = 'seed.olena@example.com'
AND NOT EXISTS (SELECT 1 FROM equipment_photos WHERE storage_key = 'seed/equipment-139.png')
HAVING MAX(e.id) IS NOT NULL;

-- 140. Генератор дизельний 10 кВт
INSERT INTO equipment (owner_id, category_id, title, description, item_condition, price_per_day, quantity, min_days, max_days, pickup_available, delivery_available, delivery_note, address, city, lat, lon, status, created_at, updated_at, settlement_id)
SELECT u.id, c.id, 'Генератор дизельний 10 кВт', 'Підходить для домашніх і професійних робіт. Видаю з кейсом і комплектом аксесуарів.', 'EXCELLENT', 3050.00, 1, 3, 30, FALSE, TRUE, 'Доставка по Львову та області, ціну узгодимо', 'вул. Бандери, 5', 'Львів', 49.814102, 24.026596, 'ACTIVE', CURRENT_TIMESTAMP - INTERVAL '4' DAY, CURRENT_TIMESTAMP - INTERVAL '4' DAY, (SELECT id FROM settlements WHERE osm_id = 26150791)
FROM users u, categories c WHERE u.email = 'seed.budprokat@example.com' AND c.name = 'Спецтехніка'
AND NOT EXISTS (SELECT 1 FROM equipment_photos WHERE storage_key = 'seed/equipment-140.png');
INSERT INTO equipment_photos (equipment_id, storage_key, position, created_at)
SELECT MAX(e.id), 'seed/equipment-140.png', 0, CURRENT_TIMESTAMP FROM equipment e JOIN users u ON u.id = e.owner_id
WHERE e.title = 'Генератор дизельний 10 кВт' AND u.email = 'seed.budprokat@example.com'
AND NOT EXISTS (SELECT 1 FROM equipment_photos WHERE storage_key = 'seed/equipment-140.png')
HAVING MAX(e.id) IS NOT NULL;
INSERT INTO equipment_photos (equipment_id, storage_key, position, created_at)
SELECT MAX(e.id), 'seed/equipment-140-2.png', 1, CURRENT_TIMESTAMP FROM equipment e JOIN users u ON u.id = e.owner_id
WHERE e.title = 'Генератор дизельний 10 кВт' AND u.email = 'seed.budprokat@example.com'
AND NOT EXISTS (SELECT 1 FROM equipment_photos WHERE storage_key = 'seed/equipment-140-2.png')
HAVING MAX(e.id) IS NOT NULL;

-- 141. Дриль ударна Metabo SBE 650
INSERT INTO equipment (owner_id, category_id, title, description, item_condition, price_per_day, quantity, min_days, max_days, pickup_available, delivery_available, delivery_note, address, city, lat, lon, status, created_at, updated_at, settlement_id)
SELECT u.id, c.id, 'Дриль ударна Metabo SBE 650', 'Підходить для домашніх і професійних робіт. Видаю з кейсом і комплектом аксесуарів.', 'GOOD', 200.00, 1, 3, 30, TRUE, TRUE, 'Доставка по Львову та області, ціну узгодимо', 'просп. Червоної Калини, 26', 'Львів', 49.880360, 24.071098, 'ACTIVE', CURRENT_TIMESTAMP - INTERVAL '11' DAY, CURRENT_TIMESTAMP - INTERVAL '11' DAY, (SELECT id FROM settlements WHERE osm_id = 26150791)
FROM users u, categories c WHERE u.email = 'seed.taras@example.com' AND c.name = 'Електроінструмент'
AND NOT EXISTS (SELECT 1 FROM equipment_photos WHERE storage_key = 'seed/equipment-141.png');
INSERT INTO equipment_photos (equipment_id, storage_key, position, created_at)
SELECT MAX(e.id), 'seed/equipment-141.png', 0, CURRENT_TIMESTAMP FROM equipment e JOIN users u ON u.id = e.owner_id
WHERE e.title = 'Дриль ударна Metabo SBE 650' AND u.email = 'seed.taras@example.com'
AND NOT EXISTS (SELECT 1 FROM equipment_photos WHERE storage_key = 'seed/equipment-141.png')
HAVING MAX(e.id) IS NOT NULL;

-- 142. Лазерний нівелір Bosch GLL 3-80
INSERT INTO equipment (owner_id, category_id, title, description, item_condition, price_per_day, quantity, min_days, max_days, pickup_available, delivery_available, delivery_note, address, city, lat, lon, status, created_at, updated_at, settlement_id)
SELECT u.id, c.id, 'Лазерний нівелір Bosch GLL 3-80', 'Надійний і простий у використанні. Повернути прошу в чистому вигляді.', 'NEW', 880.00, 1, 2, 30, TRUE, TRUE, NULL, 'вул. Сихівська, 76', 'Львів', 49.881239, 24.015603, 'ACTIVE', CURRENT_TIMESTAMP - INTERVAL '0' DAY, CURRENT_TIMESTAMP - INTERVAL '0' DAY, (SELECT id FROM settlements WHERE osm_id = 26150791)
FROM users u, categories c WHERE u.email = 'seed.budprokat@example.com' AND c.name = 'Будівельне обладнання'
AND NOT EXISTS (SELECT 1 FROM equipment_photos WHERE storage_key = 'seed/equipment-142.png');
INSERT INTO equipment_photos (equipment_id, storage_key, position, created_at)
SELECT MAX(e.id), 'seed/equipment-142.png', 0, CURRENT_TIMESTAMP FROM equipment e JOIN users u ON u.id = e.owner_id
WHERE e.title = 'Лазерний нівелір Bosch GLL 3-80' AND u.email = 'seed.budprokat@example.com'
AND NOT EXISTS (SELECT 1 FROM equipment_photos WHERE storage_key = 'seed/equipment-142.png')
HAVING MAX(e.id) IS NOT NULL;

-- 143. Вишка-тура 6 м
INSERT INTO equipment (owner_id, category_id, title, description, item_condition, price_per_day, quantity, min_days, max_days, pickup_available, delivery_available, delivery_note, address, city, lat, lon, status, created_at, updated_at, settlement_id)
SELECT u.id, c.id, 'Вишка-тура 6 м', 'Справний, перевірений перед кожною орендою. Видаю з інструкцією та всім потрібним для роботи.', 'EXCELLENT', 180.00, 1, 3, 30, TRUE, TRUE, NULL, 'вул. Центральна, 5', 'Жовква', 50.054425, 23.975763, 'ACTIVE', CURRENT_TIMESTAMP - INTERVAL '6' DAY, CURRENT_TIMESTAMP - INTERVAL '6' DAY, (SELECT id FROM settlements WHERE osm_id = 260954473)
FROM users u, categories c WHERE u.email = 'seed.olena@example.com' AND c.name = 'Риштування та драбини'
AND NOT EXISTS (SELECT 1 FROM equipment_photos WHERE storage_key = 'seed/equipment-143.png');
INSERT INTO equipment_photos (equipment_id, storage_key, position, created_at)
SELECT MAX(e.id), 'seed/equipment-143.png', 0, CURRENT_TIMESTAMP FROM equipment e JOIN users u ON u.id = e.owner_id
WHERE e.title = 'Вишка-тура 6 м' AND u.email = 'seed.olena@example.com'
AND NOT EXISTS (SELECT 1 FROM equipment_photos WHERE storage_key = 'seed/equipment-143.png')
HAVING MAX(e.id) IS NOT NULL;

-- 144. Мотобур 52 см³
INSERT INTO equipment (owner_id, category_id, title, description, item_condition, price_per_day, quantity, min_days, max_days, pickup_available, delivery_available, delivery_note, address, city, lat, lon, status, created_at, updated_at, settlement_id)
SELECT u.id, c.id, 'Мотобур 52 см³', 'Підходить для домашніх і професійних робіт. Видаю з кейсом і комплектом аксесуарів.', 'FAIR', 380.00, 1, 1, 14, TRUE, FALSE, NULL, 'вул. Кульпарківська, 77', 'Львів', 49.863668, 24.027887, 'ACTIVE', CURRENT_TIMESTAMP - INTERVAL '7' DAY, CURRENT_TIMESTAMP - INTERVAL '7' DAY, (SELECT id FROM settlements WHERE osm_id = 26150791)
FROM users u, categories c WHERE u.email = 'seed.olena@example.com' AND c.name = 'Садова техніка'
AND NOT EXISTS (SELECT 1 FROM equipment_photos WHERE storage_key = 'seed/equipment-144.png');
INSERT INTO equipment_photos (equipment_id, storage_key, position, created_at)
SELECT MAX(e.id), 'seed/equipment-144.png', 0, CURRENT_TIMESTAMP FROM equipment e JOIN users u ON u.id = e.owner_id
WHERE e.title = 'Мотобур 52 см³' AND u.email = 'seed.olena@example.com'
AND NOT EXISTS (SELECT 1 FROM equipment_photos WHERE storage_key = 'seed/equipment-144.png')
HAVING MAX(e.id) IS NOT NULL;

-- 145. Снігоприбирач акумуляторний Greenworks
INSERT INTO equipment (owner_id, category_id, title, description, item_condition, price_per_day, quantity, min_days, max_days, pickup_available, delivery_available, delivery_note, address, city, lat, lon, status, created_at, updated_at, settlement_id)
SELECT u.id, c.id, 'Снігоприбирач акумуляторний Greenworks', 'Надійний і простий у використанні. Повернути прошу в чистому вигляді.', 'NEW', 370.00, 1, 1, 7, TRUE, FALSE, NULL, 'вул. Зелена, 94', 'Львів', 49.875043, 24.044812, 'ACTIVE', CURRENT_TIMESTAMP - INTERVAL '1' DAY, CURRENT_TIMESTAMP - INTERVAL '1' DAY, (SELECT id FROM settlements WHERE osm_id = 26150791)
FROM users u, categories c WHERE u.email = 'seed.andrii@example.com' AND c.name = 'Снігоприбиральна техніка'
AND NOT EXISTS (SELECT 1 FROM equipment_photos WHERE storage_key = 'seed/equipment-145.png');
INSERT INTO equipment_photos (equipment_id, storage_key, position, created_at)
SELECT MAX(e.id), 'seed/equipment-145.png', 0, CURRENT_TIMESTAMP FROM equipment e JOIN users u ON u.id = e.owner_id
WHERE e.title = 'Снігоприбирач акумуляторний Greenworks' AND u.email = 'seed.andrii@example.com'
AND NOT EXISTS (SELECT 1 FROM equipment_photos WHERE storage_key = 'seed/equipment-145.png')
HAVING MAX(e.id) IS NOT NULL;
INSERT INTO equipment_photos (equipment_id, storage_key, position, created_at)
SELECT MAX(e.id), 'seed/equipment-145-2.png', 1, CURRENT_TIMESTAMP FROM equipment e JOIN users u ON u.id = e.owner_id
WHERE e.title = 'Снігоприбирач акумуляторний Greenworks' AND u.email = 'seed.andrii@example.com'
AND NOT EXISTS (SELECT 1 FROM equipment_photos WHERE storage_key = 'seed/equipment-145-2.png')
HAVING MAX(e.id) IS NOT NULL;

-- 146. Поломийна машина Kärcher BR 30/4
INSERT INTO equipment (owner_id, category_id, title, description, item_condition, price_per_day, quantity, min_days, max_days, pickup_available, delivery_available, delivery_note, address, city, lat, lon, status, created_at, updated_at, settlement_id)
SELECT u.id, c.id, 'Поломийна машина Kärcher BR 30/4', 'Підходить для домашніх і професійних робіт. Видаю з кейсом і комплектом аксесуарів.', 'EXCELLENT', 340.00, 1, 1, NULL, TRUE, FALSE, NULL, 'вул. Княгині Ольги, 20', 'Львів', 49.863696, 23.980451, 'ACTIVE', CURRENT_TIMESTAMP - INTERVAL '21' DAY, CURRENT_TIMESTAMP - INTERVAL '21' DAY, (SELECT id FROM settlements WHERE osm_id = 26150791)
FROM users u, categories c WHERE u.email = 'seed.taras@example.com' AND c.name = 'Клінінгове обладнання'
AND NOT EXISTS (SELECT 1 FROM equipment_photos WHERE storage_key = 'seed/equipment-146.png');
INSERT INTO equipment_photos (equipment_id, storage_key, position, created_at)
SELECT MAX(e.id), 'seed/equipment-146.png', 0, CURRENT_TIMESTAMP FROM equipment e JOIN users u ON u.id = e.owner_id
WHERE e.title = 'Поломийна машина Kärcher BR 30/4' AND u.email = 'seed.taras@example.com'
AND NOT EXISTS (SELECT 1 FROM equipment_photos WHERE storage_key = 'seed/equipment-146.png')
HAVING MAX(e.id) IS NOT NULL;

-- 147. Штатив Manfrotto 190
INSERT INTO equipment (owner_id, category_id, title, description, item_condition, price_per_day, quantity, min_days, max_days, pickup_available, delivery_available, delivery_note, address, city, lat, lon, status, created_at, updated_at, settlement_id)
SELECT u.id, c.id, 'Штатив Manfrotto 190', 'Акуратне використання, без пошкоджень. Перед видачею чищу й перевіряю.', 'EXCELLENT', 910.00, 1, 1, 7, TRUE, FALSE, NULL, 'вул. Липинського, 57', 'Львів', 49.880456, 23.997869, 'ACTIVE', CURRENT_TIMESTAMP - INTERVAL '36' DAY, CURRENT_TIMESTAMP - INTERVAL '36' DAY, (SELECT id FROM settlements WHERE osm_id = 26150791)
FROM users u, categories c WHERE u.email = 'seed.fotozona@example.com' AND c.name = 'Фото- та відеотехніка'
AND NOT EXISTS (SELECT 1 FROM equipment_photos WHERE storage_key = 'seed/equipment-147.png');
INSERT INTO equipment_photos (equipment_id, storage_key, position, created_at)
SELECT MAX(e.id), 'seed/equipment-147.png', 0, CURRENT_TIMESTAMP FROM equipment e JOIN users u ON u.id = e.owner_id
WHERE e.title = 'Штатив Manfrotto 190' AND u.email = 'seed.fotozona@example.com'
AND NOT EXISTS (SELECT 1 FROM equipment_photos WHERE storage_key = 'seed/equipment-147.png')
HAVING MAX(e.id) IS NOT NULL;

-- 148. Мікшерний пульт Yamaha MG10XU
INSERT INTO equipment (owner_id, category_id, title, description, item_condition, price_per_day, quantity, min_days, max_days, pickup_available, delivery_available, delivery_note, address, city, lat, lon, status, created_at, updated_at, settlement_id)
SELECT u.id, c.id, 'Мікшерний пульт Yamaha MG10XU', 'Підходить для домашніх і професійних робіт. Видаю з кейсом і комплектом аксесуарів.', 'FAIR', 490.00, 2, 3, 14, TRUE, TRUE, 'Привезу в межах міста за 150 ₴', 'вул. Наукова, 16', 'Львів', 49.821485, 23.960148, 'ACTIVE', CURRENT_TIMESTAMP - INTERVAL '59' DAY, CURRENT_TIMESTAMP - INTERVAL '59' DAY, (SELECT id FROM settlements WHERE osm_id = 26150791)
FROM users u, categories c WHERE u.email = 'seed.marta@example.com' AND c.name = 'Світло та звук'
AND NOT EXISTS (SELECT 1 FROM equipment_photos WHERE storage_key = 'seed/equipment-148.png');
INSERT INTO equipment_photos (equipment_id, storage_key, position, created_at)
SELECT MAX(e.id), 'seed/equipment-148.png', 0, CURRENT_TIMESTAMP FROM equipment e JOIN users u ON u.id = e.owner_id
WHERE e.title = 'Мікшерний пульт Yamaha MG10XU' AND u.email = 'seed.marta@example.com'
AND NOT EXISTS (SELECT 1 FROM equipment_photos WHERE storage_key = 'seed/equipment-148.png')
HAVING MAX(e.id) IS NOT NULL;

-- 149. Спальник −10 °C
INSERT INTO equipment (owner_id, category_id, title, description, item_condition, price_per_day, quantity, min_days, max_days, pickup_available, delivery_available, delivery_note, address, city, lat, lon, status, created_at, updated_at, settlement_id)
SELECT u.id, c.id, 'Спальник −10 °C', 'Підходить для домашніх і професійних робіт. Видаю з кейсом і комплектом аксесуарів.', 'NEW', 490.00, 1, 3, NULL, TRUE, TRUE, 'Привезу в межах міста за 150 ₴', 'вул. Володимира Великого, 40', 'Львів', 49.871496, 24.050434, 'ACTIVE', CURRENT_TIMESTAMP - INTERVAL '38' DAY, CURRENT_TIMESTAMP - INTERVAL '38' DAY, (SELECT id FROM settlements WHERE osm_id = 26150791)
FROM users u, categories c WHERE u.email = 'seed.olena@example.com' AND c.name = 'Туристичне спорядження'
AND NOT EXISTS (SELECT 1 FROM equipment_photos WHERE storage_key = 'seed/equipment-149.png');
INSERT INTO equipment_photos (equipment_id, storage_key, position, created_at)
SELECT MAX(e.id), 'seed/equipment-149.png', 0, CURRENT_TIMESTAMP FROM equipment e JOIN users u ON u.id = e.owner_id
WHERE e.title = 'Спальник −10 °C' AND u.email = 'seed.olena@example.com'
AND NOT EXISTS (SELECT 1 FROM equipment_photos WHERE storage_key = 'seed/equipment-149.png')
HAVING MAX(e.id) IS NOT NULL;

-- 150. Причіп легковий 750 кг
INSERT INTO equipment (owner_id, category_id, title, description, item_condition, price_per_day, quantity, min_days, max_days, pickup_available, delivery_available, delivery_note, address, city, lat, lon, status, created_at, updated_at, settlement_id)
SELECT u.id, c.id, 'Причіп легковий 750 кг', 'Надійний і простий у використанні. Повернути прошу в чистому вигляді.', 'EXCELLENT', 2560.00, 1, 1, 7, TRUE, FALSE, NULL, 'вул. Княгині Ольги, 18', 'Львів', 49.861940, 23.985134, 'ACTIVE', CURRENT_TIMESTAMP - INTERVAL '51' DAY, CURRENT_TIMESTAMP - INTERVAL '51' DAY, (SELECT id FROM settlements WHERE osm_id = 26150791)
FROM users u, categories c WHERE u.email = 'seed.budprokat@example.com' AND c.name = 'Спецтехніка'
AND NOT EXISTS (SELECT 1 FROM equipment_photos WHERE storage_key = 'seed/equipment-150.png');
INSERT INTO equipment_photos (equipment_id, storage_key, position, created_at)
SELECT MAX(e.id), 'seed/equipment-150.png', 0, CURRENT_TIMESTAMP FROM equipment e JOIN users u ON u.id = e.owner_id
WHERE e.title = 'Причіп легковий 750 кг' AND u.email = 'seed.budprokat@example.com'
AND NOT EXISTS (SELECT 1 FROM equipment_photos WHERE storage_key = 'seed/equipment-150.png')
HAVING MAX(e.id) IS NOT NULL;
INSERT INTO equipment_photos (equipment_id, storage_key, position, created_at)
SELECT MAX(e.id), 'seed/equipment-150-2.png', 1, CURRENT_TIMESTAMP FROM equipment e JOIN users u ON u.id = e.owner_id
WHERE e.title = 'Причіп легковий 750 кг' AND u.email = 'seed.budprokat@example.com'
AND NOT EXISTS (SELECT 1 FROM equipment_photos WHERE storage_key = 'seed/equipment-150-2.png')
HAVING MAX(e.id) IS NOT NULL;
