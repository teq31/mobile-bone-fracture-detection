package com.bonefracture.analysis;

import com.bonefracture.detection.AnalysisResult;
import com.bonefracture.detection.Detection;
import com.bonefracture.detection.composite.DetectionGroup;
import com.bonefracture.persistence.AnalysisEntity;
import com.bonefracture.persistence.DetectionEntity;
import com.bonefracture.persistence.UserEntity;
import com.bonefracture.persistence.UserRepository;
import com.bonefracture.service.ModelComparisonService;
import com.bonefracture.user.Role;
import com.bonefracture.user.User;
import com.bonefracture.user.UserMapper;
import com.bonefracture.xray.BodyPart;
import com.bonefracture.xray.FileFormat;
import com.bonefracture.xray.adapter.DicomFile;
import com.bonefracture.xray.adapter.DicomXRayAdapter;
import com.bonefracture.xray.adapter.ImageFileAdapter;
import com.bonefracture.xray.adapter.PdfDocument;
import com.bonefracture.xray.adapter.PdfXRayAdapter;
import com.bonefracture.xray.adapter.XRayImageSource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.awt.Rectangle;
import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;

@Service
public class AnalysisStorageService {

    private final XRayAnalysisService analysisService;
    private final ModelComparisonService modelComparisonService;
    private final AnalysisHistoryService historyService;
    private final UserRepository userRepository;
    private final Path uploadDir;

    public AnalysisStorageService(XRayAnalysisService analysisService,
                                  ModelComparisonService modelComparisonService,
                                  AnalysisHistoryService historyService,
                                  UserRepository userRepository,
                                  @Value("${app.analysis.upload-dir:uploads}") String uploadDir) {
        this.analysisService = analysisService;
        this.modelComparisonService = modelComparisonService;
        this.historyService = historyService;
        this.userRepository = userRepository;
        this.uploadDir = Path.of(uploadDir);
    }

    public AnalysisEntity analyze(Long userId, MultipartFile image, BodyPart bodyPart) {
        if (image == null || image.isEmpty()) {
            throw new IllegalArgumentException("image is required");
        }
        if (bodyPart == null) {
            throw new IllegalArgumentException("bodyPart is required");
        }
        File file = store(image);
        FileFormat format = formatOf(image.getOriginalFilename());
        XRayImageSource source = adapterFor(file, format);

        User user = toDomainUser(userId);
        AnalysisResult result = analysisService.analyzeXRay(user, source, bodyPart);

        AnalysisEntity entity = new AnalysisEntity(
                userId,
                file.getPath(),
                format,
                bodyPart,
                result.getModel() == null ? null : result.getModel().getName());
        for (Detection detection : result.getDetections()) {
            entity.addDetection(toDetectionEntity(detection));
        }
        return historyService.save(entity);
    }

    public List<AnalysisEntity> history(Long userId) {
        return historyService.findByUser(userId);
    }

    public AnalysisEntity get(Long userId, Long analysisId) {
        AnalysisEntity entity = historyService.findById(analysisId)
                .orElseThrow(() -> new NoSuchElementException("analysis not found: " + analysisId));
        if (!entity.getUserId().equals(userId)) {
            throw new NoSuchElementException("analysis not found: " + analysisId);
        }
        return entity;
    }

    public DetectionGroup compare(Long userId, Long analysisId) {
        AnalysisEntity entity = get(userId, analysisId);
        UserEntity account = userRepository.findById(userId)
                .orElseThrow(() -> new NoSuchElementException("user not found: " + userId));
        if (account.getRole() != Role.DOCTOR || !account.isVerified()) {
            throw new NotAllowedToCompareException("only a verified doctor can compare models");
        }
        XRayImageSource source = adapterFor(new File(entity.getImagePath()), entity.getFormat());
        return modelComparisonService.compareModels(source.toXRayImage(entity.getBodyPart()));
    }

    public ModelComparisonService modelComparison() {
        return modelComparisonService;
    }

    public byte[] readImage(AnalysisEntity entity) {
        try {
            return Files.readAllBytes(Path.of(entity.getImagePath()));
        } catch (IOException e) {
            throw new UncheckedIOException("cannot read analysis image", e);
        }
    }

    private File store(MultipartFile image) {
        try {
            Files.createDirectories(uploadDir);
            String name = UUID.randomUUID() + extensionOf(image.getOriginalFilename());
            Path target = uploadDir.resolve(name);
            image.transferTo(target.toFile());
            return target.toFile();
        } catch (IOException e) {
            throw new UncheckedIOException("cannot store uploaded image", e);
        }
    }

    private XRayImageSource adapterFor(File file, FileFormat format) {
        return switch (format) {
            case JPG, PNG -> new ImageFileAdapter(file, format);
            case PDF -> new PdfXRayAdapter(new PdfDocument(file));
            case DICOM -> new DicomXRayAdapter(new DicomFile(file));
        };
    }

    private User toDomainUser(Long userId) {
        UserEntity entity = userRepository.findById(userId)
                .orElseThrow(() -> new NoSuchElementException("user not found: " + userId));
        return UserMapper.fromAccount(entity.getName(), entity.getEmail(), entity.getRole(), entity.isVerified());
    }

    private DetectionEntity toDetectionEntity(Detection detection) {
        Rectangle box = detection.getBoundingBox();
        if (box == null) {
            return new DetectionEntity(detection.getLabel(), detection.getConfidence(), 0, 0, 0, 0);
        }
        return new DetectionEntity(detection.getLabel(), detection.getConfidence(),
                box.x, box.y, box.width, box.height);
    }

    private FileFormat formatOf(String name) {
        String ext = extensionOf(name).toLowerCase();
        return switch (ext) {
            case ".jpg", ".jpeg" -> FileFormat.JPG;
            case ".png" -> FileFormat.PNG;
            case ".pdf" -> FileFormat.PDF;
            case ".dcm", ".dicom" -> FileFormat.DICOM;
            default -> throw new IllegalArgumentException("unsupported image type: " + name);
        };
    }

    private String extensionOf(String name) {
        if (name == null) {
            throw new IllegalArgumentException("image name is missing");
        }
        int dot = name.lastIndexOf('.');
        if (dot < 0) {
            throw new IllegalArgumentException("image has no file extension: " + name);
        }
        return name.substring(dot);
    }
}
