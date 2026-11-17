package com.bonefracture.aspect;

import com.bonefracture.service.ModelComparisonService;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;

@Aspect
public class ModelComparisonValidationAspect {

    private static final int MINIMUM_MODELS = 2;

    @Before("execution(* com.bonefracture.service.ModelComparisonService.compareModels(..)) && target(service) && args(image)")
    public void validateComparison(JoinPoint joinPoint, ModelComparisonService service, Object image) {
        if (image == null) {
            throw new ModelComparisonValidationException("Cannot compare models: the X-ray image is null");
        }
        int modelCount = service.getRegisteredModelCount();
        if (modelCount < MINIMUM_MODELS) {
            throw new ModelComparisonValidationException(
                    "Cannot compare models: at least " + MINIMUM_MODELS
                            + " registered models are required, but found " + modelCount);
        }
    }
}
