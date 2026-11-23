package com.bonefracture.api.dto;

import com.bonefracture.persistence.AnalysisEntity;
import com.bonefracture.persistence.DetectionEntity;

import java.util.List;

public record AnalysisResponse(Long analysisId,
                               String model,
                               List<DetectionDto> detections,
                               String imageUrl,
                               String createdAt) {

    public static AnalysisResponse from(AnalysisEntity entity) {
        List<DetectionDto> detections = entity.getDetections().stream()
                .map(DetectionDto::from)
                .toList();
        return new AnalysisResponse(
                entity.getId(),
                entity.getModel(),
                detections,
                imageUrl(entity.getId()),
                String.valueOf(entity.getCreatedAt()));
    }

    public static String imageUrl(Long analysisId) {
        return "/api/analyses/" + analysisId + "/image";
    }

    public record DetectionDto(String label, float confidence, BoundingBoxDto boundingBox) {
        public static DetectionDto from(DetectionEntity detection) {
            return new DetectionDto(
                    detection.getLabel(),
                    detection.getConfidence(),
                    new BoundingBoxDto(detection.getBoxX(), detection.getBoxY(),
                            detection.getBoxWidth(), detection.getBoxHeight()));
        }
    }

    public record BoundingBoxDto(int x, int y, int width, int height) {
    }
}
