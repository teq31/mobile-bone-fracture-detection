package com.bonefracture.analysis;

import com.bonefracture.detection.AnalysisResult;
import com.bonefracture.user.Doctor;
import com.bonefracture.user.User;
import com.bonefracture.xray.BodyPart;
import com.bonefracture.xray.XRayImage;
import com.bonefracture.xray.adapter.XRayImageSource;

import java.util.List;

/**
 * SOA: service for analysing X-rays. The app screens (and later the REST API) use only this interface.
 * Implemented by FractureAnalysisFacade (FACADE pattern).
 */
public interface XRayAnalysisService {

    // Student and Patient: one analysis with the main model
    AnalysisResult analyzeXRay(User user, XRayImageSource source, BodyPart bodyPart);

    // Verified Doctor: the same X-ray analysed by every available model
    List<AnalysisResult> compareModels(Doctor doctor, XRayImageSource source, BodyPart bodyPart);

    List<XRayImage> getHistory(User user);
}
