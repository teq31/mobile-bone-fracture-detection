package com.bonefracture.api;

import com.bonefracture.analysis.FractureAnalysisFacade;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import com.bonefracture.xray.BodyPart;

@RestController
@RequestMapping("/api/analyses")
public class AnalysisController {

    private final FractureAnalysisFacade analysisFacade;

    public AnalysisController(FractureAnalysisFacade analysisFacade) {
        this.analysisFacade = analysisFacade;
    }

    @PostMapping
    public Object analyze(@AuthenticationPrincipal Jwt jwt,
                          @RequestParam("image") MultipartFile image,
                          @RequestParam("bodyPart") BodyPart bodyPart) {
        throw notReady("analyze");
    }

    @GetMapping("/history")
    public Object history(@AuthenticationPrincipal Jwt jwt) {
        throw notReady("history");
    }

    @GetMapping("/{id}/comparison")
    public Object comparison(@AuthenticationPrincipal Jwt jwt, @PathVariable String id) {
        throw notReady("comparison");
    }

    private ResponseStatusException notReady(String endpoint) {
        return new ResponseStatusException(HttpStatus.NOT_IMPLEMENTED,
                "analysis endpoint '" + endpoint + "' is waiting on the analysis pipeline (FractureAnalysisFacade)");
    }
}
