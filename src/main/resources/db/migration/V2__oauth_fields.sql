-- OAuth 지원을 위한 users 테이블 수정
ALTER TABLE users ADD COLUMN provider VARCHAR(20);
ALTER TABLE users ADD COLUMN provider_id VARCHAR(255);

-- 기존 password NOT NULL 제약 해제 (OAuth 유저는 password 없음)
ALTER TABLE users ALTER COLUMN password DROP NOT NULL;

-- provider + provider_id 유니크 (동일 OAuth 계정 중복 방지)
CREATE UNIQUE INDEX idx_users_provider ON users(provider, provider_id) WHERE provider IS NOT NULL;
