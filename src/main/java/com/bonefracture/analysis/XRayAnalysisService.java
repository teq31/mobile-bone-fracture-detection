package com.bonefracture.analysis;

import com.bonefracture.detection.AnalysisResult;
import com.bonefracture.user.Doctor;
import com.bonefracture.user.User;
import com.bonefracture.xray.BodyPart;
import com.bonefracture.xray.XRayImage;
import com.bonefracture.xray.adapter.XRayImageSource;

import java.util.List;

// SOA - the API / app only talk to this, impl is FractureAnalysisFacade
public interface XRayAnalysisService {

    // student / patient -> main model only
    AnalysisResult analyzeXRay(User user, XRayImageSource source, BodyPart bodyPart);

    // verified doctor -> all models
    List<AnalysisResult> compareModels(Doctor doctor, XRayImageSource source, BodyPart bodyPart);

    List<XRayImage> getHistory(User user);
}
