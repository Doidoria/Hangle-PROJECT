package com.example.demo.domain.leaderboard.repository;


import com.example.demo.domain.leaderboard.entity.Leaderboard;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface LeaderboardRepository extends JpaRepository<Leaderboard,Long> {

    //순위 조회
    @Query("""
            SELECT l FROM Leaderboard l
            JOIN FETCH l.competition c
            JOIN FETCH l.user u
            JOIN FETCH l.submission s
            ORDER BY  c.id ASC, s.score DESC
    """)
    List<Leaderboard> findAllOrderByCompThenScore();

    //서치 compId 비동기 처리
    @Query("""
    SELECT l FROM Leaderboard l
    JOIN FETCH l.competition c
    JOIN FETCH l.user u
    JOIN FETCH l.submission s
    WHERE LOWER(u.username) LIKE LOWER(CONCAT('%', :keyword, '%'))
       OR LOWER(c.title) LIKE LOWER(CONCAT('%', :keyword, '%'))
    ORDER BY  c.id ASC, s.score DESC, l.submittedAt ASC
    """)
    List<Leaderboard> searchAllByKeyword(@Param("keyword") String keyword);

    List<Leaderboard> findByCompetition_Id(Long competitionid);

    @Query("""
    SELECT l FROM Leaderboard l
    WHERE l.competition.id = :competitionId
      AND l.user.id = :userId
""")
    Optional<Leaderboard> findByCompetitionIdAndUserId(
            @Param("competitionId") Long competitionId,
            @Param("userId") Long userId
    );




}
