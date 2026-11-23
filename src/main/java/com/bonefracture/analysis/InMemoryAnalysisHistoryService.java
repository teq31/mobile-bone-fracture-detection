package com.bonefracture.analysis;

import com.bonefracture.persistence.AnalysisEntity;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

@Service
@Profile("in-memory")
public class InMemoryAnalysisHistoryService implements AnalysisHistoryService {

    private final Map<Long, AnalysisEntity> byId = new ConcurrentHashMap<>();
    private final AtomicLong sequence = new AtomicLong();

    @Override
    public AnalysisEntity save(AnalysisEntity analysis) {
        long id = sequence.incrementAndGet();
        setId(analysis, id);
        byId.put(id, analysis);
        return analysis;
    }

    @Override
    public List<AnalysisEntity> findByUser(Long userId) {
        List<AnalysisEntity> result = new ArrayList<>();
        for (AnalysisEntity analysis : byId.values()) {
            if (analysis.getUserId().equals(userId)) {
                result.add(analysis);
            }
        }
        result.sort((a, b) -> b.getCreatedAt().compareTo(a.getCreatedAt()));
        return result;
    }

    @Override
    public Optional<AnalysisEntity> findById(Long analysisId) {
        return Optional.ofNullable(byId.get(analysisId));
    }

    private void setId(AnalysisEntity analysis, long id) {
        try {
            var field = AnalysisEntity.class.getDeclaredField("id");
            field.setAccessible(true);
            field.set(analysis, id);
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException("cannot assign an in-memory analysis id", e);
        }
    }
}
