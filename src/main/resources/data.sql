-- =====================================================================================
-- [tag] 프리셋 태그 시드 (담당: 정민성)
-- 외모(APPEARANCE) 6개, 성격(PERSONALITY) 6개
-- 자유 태그(FREE)는 글·고양이를 등록할 때 자동으로 만들어지므로 넣지 않는다.
--
-- INSERT IGNORE: name에 unique 제약이 있어서, 서버를 다시 켜서 이미 있는 이름을
-- 넣으려 하면 오류 대신 그 행만 건너뛴다. ddl-auto가 update여도 중복 없이 재실행된다.
-- created_at, updated_at: BaseEntity의 Auditing은 SQL로 넣을 때는 동작하지 않아 직접 채운다.
-- 이름은 태그 정규화 규칙(공백 없음, 영문 소문자)을 이미 만족하는 형태로 넣는다.
-- =====================================================================================

-- 외모(APPEARANCE)
INSERT IGNORE INTO tag (name, type, created_at, updated_at) VALUES ('장모',   'APPEARANCE', NOW(6), NOW(6));
INSERT IGNORE INTO tag (name, type, created_at, updated_at) VALUES ('단모',   'APPEARANCE', NOW(6), NOW(6));
INSERT IGNORE INTO tag (name, type, created_at, updated_at) VALUES ('치즈',   'APPEARANCE', NOW(6), NOW(6));
INSERT IGNORE INTO tag (name, type, created_at, updated_at) VALUES ('고등어', 'APPEARANCE', NOW(6), NOW(6));
INSERT IGNORE INTO tag (name, type, created_at, updated_at) VALUES ('턱시도', 'APPEARANCE', NOW(6), NOW(6));
INSERT IGNORE INTO tag (name, type, created_at, updated_at) VALUES ('삼색',   'APPEARANCE', NOW(6), NOW(6));

-- 성격(PERSONALITY)
INSERT IGNORE INTO tag (name, type, created_at, updated_at) VALUES ('개냥이',   'PERSONALITY', NOW(6), NOW(6));
INSERT IGNORE INTO tag (name, type, created_at, updated_at) VALUES ('얌전함',   'PERSONALITY', NOW(6), NOW(6));
INSERT IGNORE INTO tag (name, type, created_at, updated_at) VALUES ('활발함',   'PERSONALITY', NOW(6), NOW(6));
INSERT IGNORE INTO tag (name, type, created_at, updated_at) VALUES ('겁많음',   'PERSONALITY', NOW(6), NOW(6));
INSERT IGNORE INTO tag (name, type, created_at, updated_at) VALUES ('먹보',     'PERSONALITY', NOW(6), NOW(6));
INSERT IGNORE INTO tag (name, type, created_at, updated_at) VALUES ('수다쟁이', 'PERSONALITY', NOW(6), NOW(6));