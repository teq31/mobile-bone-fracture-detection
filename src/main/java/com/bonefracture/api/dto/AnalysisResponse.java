package com.bonefracture.api.dto;

import com.bonefracture.detection.AnalysisResult;
import com.bonefracture.detection.Detection;

import java.util.List;

public record AnalysisResponse(String model, List<DetectionDto> detections) {

    public static AnalysisResponse from(AnalysisResult result) {
        if (result == null) {
            return new AnalysisResponse(null, List.of());
        }
        String model = result.getModel() == null ? null : result.getModel().getName();
        List<DetectionDto> detections = result.getDetections().stream()
                .map(DetectionDto::from)
                .toList();
        return new AnalysisResponse(model, detections);
    }

    public record DetectionDto(String label, float confidence, BoundingBoxDto boundingBox) {
        public static DetectionDto from(Detection detection) {
            return new DetectionDto(
                    detection.getLabel(),
                    detection.getConfidence(),
                    BoundingBoxDto.from(detection.getBoundingBox()));
        }
    }

    public record BoundingBoxDto(int x, int y, int width, int height) {
        public static BoundingBoxDto from(java.awt.Rectangle box) {
            if (box == null) {
                return null;
            }
            return new BoundingBoxDto(box.x, box.y, box.width, box.height);
        }
    }
}
