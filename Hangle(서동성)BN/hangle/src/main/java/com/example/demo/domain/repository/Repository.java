package com.example.demo.domain.repository;

import org.springframework.data.jpa.repository.JpaRepository;

public interface Repository  extends JpaRepository<Dataset, Long> {
    List<Dataset> findTop5ByOrderByViewCountDesc();
}
