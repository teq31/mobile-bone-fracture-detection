package com.bonefracture.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AnalysisRepository extends JpaRepository<AnalysisEntity, Long> {

    List<AnalysisEntity> findByUserIdOrderByCreatedAtDesc(Long userId);
}
