package com.bonefracture.analysis;

import com.bonefracture.persistence.AnalysisEntity;
import com.bonefracture.persistence.AnalysisRepository;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@Primary
public class JpaAnalysisHistoryService implements AnalysisHistoryService {

    private final AnalysisRepository analysisRepository;

    public JpaAnalysisHistoryService(AnalysisRepository analysisRepository) {
        this.analysisRepository = analysisRepository;
    }

    @Override
    public AnalysisEntity save(AnalysisEntity analysis) {
        return analysisRepository.save(analysis);
    }

    @Override
    public List<AnalysisEntity> findByUser(Long userId) {
        return analysisRepository.findByUserIdOrderByCreatedAtDesc(userId);
    }

    @Override
    public Optional<AnalysisEntity> findById(Long analysisId) {
        return analysisRepository.findById(analysisId);
    }
}
