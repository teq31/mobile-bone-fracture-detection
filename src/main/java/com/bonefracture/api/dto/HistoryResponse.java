package com.bonefracture.api.dto;

import com.bonefracture.persistence.AnalysisEntity;

import java.util.List;

public record HistoryResponse(List<ItemDto> items) {

    public static HistoryResponse from(List<AnalysisEntity> analyses) {
        List<ItemDto> items = analyses.stream()
                .map(ItemDto::from)
                .toList();
        return new HistoryResponse(items);
    }

    public record ItemDto(Long analysisId, String model, String imageUrl, String createdAt) {
        public static ItemDto from(AnalysisEntity entity) {
            return new ItemDto(
                    entity.getId(),
                    entity.getModel(),
                    AnalysisResponse.imageUrl(entity.getId()),
                    String.valueOf(entity.getCreatedAt()));
        }
    }
}
