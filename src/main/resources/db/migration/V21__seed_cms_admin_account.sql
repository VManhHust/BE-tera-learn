-- Local bootstrap account for the TERA CMS.
-- Password: Tera@Admin2026 (BCrypt strength 12).
INSERT INTO users (email, password_hash, display_name, role, status)
VALUES (
    'admin@tera.vn',
    '$2a$12$BImzWPoOmK2LgTqKyb.1xOqoQbDehN55lPX993ufvMqgUf2Aj14vS',
    'Quản trị viên TERA',
    'ADMIN',
    'ACTIVE'
)
ON CONFLICT (email) DO NOTHING;
