-- 샘플 대회 1
INSERT INTO competition (title, description, status, start_at, end_at)
SELECT '샘플 대회 1', '이건 테스트용 대회입니다.', 'OPEN', NOW(), DATE_ADD(NOW(), INTERVAL 30 DAY)
    WHERE NOT EXISTS (SELECT 1 FROM competition WHERE title = '샘플 대회 1');


-- 샘플 대회 2
INSERT INTO competition (title, description, status, start_at, end_at)
SELECT '샘플 대회 2', '둘째 샘플', 'UPCOMING', DATE_ADD(NOW(), INTERVAL 7 DAY), DATE_ADD(NOW(), INTERVAL 37 DAY)
WHERE NOT EXISTS (SELECT 1 FROM competition WHERE title = '샘플 대회 2');

-- 샘플 대회 3
INSERT INTO competition (title, description, status, start_at, end_at)
SELECT '샘플 대회 3', '마감된 샘플', 'CLOSED', DATE_ADD(NOW(), INTERVAL -60 DAY), DATE_ADD(NOW(), INTERVAL -30 DAY)
WHERE NOT EXISTS (SELECT 1 FROM competition WHERE title = '샘플 대회 3');
