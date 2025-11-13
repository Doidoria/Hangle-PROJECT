package com.example.demo.domain.leaderboard.repository;


import com.example.demo.domain.leaderboard.entity.Leaderboard;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LeaderboardRepository extends JpaRepository<Leaderboard,Long> {

    //순위 조회
    @Query("""
            SELECT l FROM Leaderboard l
            JOIN FETCH l.competition c
            JOIN FETCH l.user u
            ORDER BY  c.id ASC, l.score DESC
    """)
    List<Leaderboard> findAllOrderByCompThenScore();

    //서치 compId 비동기 처리
    @Query("""
    SELECT l FROM Leaderboard l
    JOIN FETCH l.competition c
    JOIN FETCH l.user u
    WHERE LOWER(u.username) LIKE LOWER(CONCAT('%', :keyword, '%'))
       OR LOWER(c.title) LIKE LOWER(CONCAT('%', :keyword, '%'))
    ORDER BY  c.id ASC, l.score DESC, l.submittedAt ASC
    """)
    List<Leaderboard> searchAllByKeyword(@Param("keyword") String keyword);

    List<Leaderboard> findByCompetition_Id(Long competitionid);


    // ========================
    // FK받은 경우
    // ========================

    //순위 조회
//    @Query("""
//            SELECT l FROM Leaderboard l
//            JOIN FETCH l.comp c
//            JOIN FETCH l.user u
//            JOIN FETCH l.submit s
//            ORDER BY c.compId ASC, s.best_score DESC
//    """)
//    List<Leaderboard> findAllOrderByCompThenScore();

//    //서치 compId 비동기 처리
//    @Query("""
//    SELECT l FROM Leaderboard l
//    JOIN FETCH l.comp c
//    JOIN FETCH l.user u
//    JOIN FETCH l.submit s
//    WHERE LOWER(u.userName) LIKE LOWER(CONCAT('%', :keyword, '%'))
//       OR LOWER(c.compName) LIKE LOWER(CONCAT('%', :keyword, '%'))
//    ORDER BY c.compId ASC, s.best_score DESC, l.last_submit_time ASC
//""")
//    List<Leaderboard> searchAllByKeyword(@Param("keyword") String keyword);
//
//    List<Leaderboard> findByComp_CompId(Long compId);


}
