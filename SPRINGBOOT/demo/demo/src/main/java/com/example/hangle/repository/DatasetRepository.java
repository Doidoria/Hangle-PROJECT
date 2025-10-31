package com.example.hangle.repository;

import com.example.hangle.entity.Dataset;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface DatasetRepository extends JpaRepository<Dataset, Long> {

    // 등록일 기준 최신순 정렬
    List<Dataset> findAllByOrderByCreatedAtDesc();

    // 좋아요 기준 내림차순 정렬
    List<Dataset> findAllByOrderByLikesDesc();
}
