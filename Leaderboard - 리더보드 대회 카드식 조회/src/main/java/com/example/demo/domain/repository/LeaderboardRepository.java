package com.example.demo.domain.repository;

import com.example.demo.domain.dto.LeaderboardDto;
import com.example.demo.domain.entity.Comp;
import com.example.demo.domain.entity.Leaderboard;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LeaderboardRepository extends JpaRepository<Leaderboard, Long> {


    //순위 조회
    @Query("""
            SELECT l FROM Leaderboard l
            JOIN FETCH l.comp c
            JOIN FETCH l.user u
            ORDER BY c.compId ASC, l.best_score DESC
    """)
    List<Leaderboard> findAllOrderByCompThenScore();



    //서치 compId 비동기 처리
//    @Query("""
//        SELECT l FROM Leaderboard l
//        WHERE l.comp.compId = :compId
//        AND LOWER(l.user.userName) LIKE LOWER(CONCAT('%', :keyword, '%'))
//    """)
//    List<Leaderboard> searchByKeyword(@Param("keyword") String keyword, @Param("compId") Long compId);

    @Query("""
        SELECT l FROM Leaderboard l
        JOIN FETCH l.comp c
        JOIN FETCH l.user u
        WHERE LOWER(u.userName) LIKE LOWER(CONCAT('%', :keyword, '%'))
        ORDER BY c.compId ASC, l.best_score DESC
    """)
    List<Leaderboard> searchAllByKeyword(@Param("keyword") String keyword);

}
