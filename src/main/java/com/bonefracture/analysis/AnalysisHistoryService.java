package com.bonefracture.analysis;

import com.bonefracture.persistence.AnalysisEntity;

import java.util.List;
import java.util.Optional;

public interface AnalysisHistoryService {

    AnalysisEntity save(AnalysisEntity analysis);

    List<AnalysisEntity> findByUser(Long userId);

    Optional<AnalysisEntity> findById(Long analysisId);
}
