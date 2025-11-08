package com.example.demo.domain.repository;

import com.example.demo.domain.entity.Comp;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CompRepository extends JpaRepository<Comp,Long> {


    @Query("SELECT c FROM Comp c " +
            "WHERE c.compName LIKE %:keyword% " )
    List<Comp> searchByKeyword(@Param("keyword") String keyword);
}
