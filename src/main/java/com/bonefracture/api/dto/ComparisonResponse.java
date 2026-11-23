package com.bonefracture.api.dto;

import com.bonefracture.detection.DetectionModel;
import com.bonefracture.detection.composite.AgreementLevel;
import com.bonefracture.detection.composite.DetectionComponent;
import com.bonefracture.detection.composite.DetectionGroup;
import com.bonefracture.service.ModelComparisonService;

import java.awt.Rectangle;
import java.util.List;

public record ComparisonResponse(int totalModels, List<FindingDto> findings) {

    public static ComparisonResponse from(DetectionGroup view, ModelComparisonService service) {
        if (view == null) {
            return new ComparisonResponse(service.getRegisteredModelCount(), List.of());
        }
        List<FindingDto> findings = view.getChildren().stream()
                .map(child -> FindingDto.from(child, service))
                .toList();
        return new ComparisonResponse(service.getRegisteredModelCount(), findings);
    }

    public record FindingDto(String label,
                             float confidence,
                             AnalysisResponse.BoundingBoxDto boundingBox,
                             AgreementLevel agreementLevel,
                             int agreementCount,
                             List<String> models) {

        public static FindingDto from(DetectionComponent finding, ModelComparisonService service) {
            Rectangle box = finding.getBoundingBox();
            List<String> modelNames = finding.getModels().stream()
                    .map(DetectionModel::getName)
                    .toList();
            return new FindingDto(
                    finding.getLabel(),
                    finding.getConfidence(),
                    AnalysisResponse.BoundingBoxDto.from(box),
                    service.getAgreementLevel(finding),
                    finding.getAgreementCount(),
                    modelNames);
        }
    }
}
