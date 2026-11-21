package com.bonefracture.api.dto;

import com.bonefracture.xray.BodyPart;
import jakarta.validation.constraints.NotNull;

public record AnalysisRequest(@NotNull BodyPart bodyPart) {
}
