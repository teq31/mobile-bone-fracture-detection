package com.bonefracture.api;

import com.bonefracture.analysis.AnalysisStorageService;
import com.bonefracture.api.dto.AnalysisResponse;
import com.bonefracture.api.dto.HistoryResponse;
import com.bonefracture.persistence.AnalysisEntity;
import com.bonefracture.xray.BodyPart;
import com.bonefracture.xray.FileFormat;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.util.NoSuchElementException;

@RestController
@RequestMapping("/api/analyses")
public class AnalysisController {

    private final AnalysisStorageService storageService;

    public AnalysisController(AnalysisStorageService storageService) {
        this.storageService = storageService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public AnalysisResponse analyze(@AuthenticationPrincipal Jwt jwt,
                                    @RequestParam("image") MultipartFile image,
                                    @RequestParam("bodyPart") BodyPart bodyPart) {
        AnalysisEntity entity = storageService.analyze(userId(jwt), image, bodyPart);
        return AnalysisResponse.from(entity);
    }

    @GetMapping("/history")
    public HistoryResponse history(@AuthenticationPrincipal Jwt jwt) {
        return HistoryResponse.from(storageService.history(userId(jwt)));
    }

    @GetMapping("/{id}")
    public AnalysisResponse get(@AuthenticationPrincipal Jwt jwt, @PathVariable Long id) {
        return AnalysisResponse.from(find(jwt, id));
    }

    @GetMapping("/{id}/image")
    public ResponseEntity<Resource> image(@AuthenticationPrincipal Jwt jwt, @PathVariable Long id) {
        AnalysisEntity entity = find(jwt, id);
        byte[] bytes = storageService.readImage(entity);
        return ResponseEntity.ok()
                .contentType(mediaType(entity.getFormat()))
                .body(new ByteArrayResource(bytes));
    }

    private AnalysisEntity find(Jwt jwt, Long id) {
        try {
            return storageService.get(userId(jwt), id);
        } catch (NoSuchElementException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
        }
    }

    private MediaType mediaType(FileFormat format) {
        return switch (format) {
            case JPG -> MediaType.IMAGE_JPEG;
            case PNG -> MediaType.IMAGE_PNG;
            case PDF -> MediaType.APPLICATION_PDF;
            case DICOM -> MediaType.APPLICATION_OCTET_STREAM;
        };
    }

    private Long userId(Jwt jwt) {
        return Long.valueOf(jwt.getSubject());
    }
}
