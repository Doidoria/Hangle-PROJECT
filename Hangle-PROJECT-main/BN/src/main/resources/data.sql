INSERT INTO competitions (title, description, status, start_at, end_at, created_at)
VALUES
('샘플 대회 1', '이건 테스트용 대회입니다.', 'OPEN', NOW(), DATE_ADD(NOW(), INTERVAL 30 DAY), NOW()),
('샘플 대회 2', '둘째 샘플', 'UPCOMING', DATE_ADD(NOW(), INTERVAL 7 DAY), DATE_ADD(NOW(), INTERVAL 37 DAY), NOW()),
('샘플 대회 3', '마감된 샘플', 'CLOSED', DATE_ADD(NOW(), INTERVAL -60 DAY), DATE_ADD(NOW(), INTERVAL -30 DAY), NOW());
