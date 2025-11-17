package com.example.demo.domain.submmitex.repository;

import com.example.demo.domain.submmitex.entity.Submission;
import jdk.jfr.Registered;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SubmissionRepository extends JpaRepository<Submission,Long> {
}
