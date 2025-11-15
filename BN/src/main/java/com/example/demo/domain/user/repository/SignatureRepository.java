package com.example.demo.domain.user.repository;

import com.example.demo.domain.user.entity.Signature;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SignatureRepository extends JpaRepository<Signature,Byte> {
}
