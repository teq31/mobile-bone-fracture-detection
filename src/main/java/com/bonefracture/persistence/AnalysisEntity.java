package com.bonefracture.persistence;

import com.bonefracture.xray.BodyPart;
import com.bonefracture.xray.FileFormat;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "analyses")
public class AnalysisEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long userId;

    @Column(nullable = false)
    private String imagePath;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private FileFormat format;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private BodyPart bodyPart;

    private String model;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @OneToMany(mappedBy = "analysis", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<DetectionEntity> detections = new ArrayList<>();

    protected AnalysisEntity() {
    }

    public AnalysisEntity(Long userId, String imagePath, FileFormat format, BodyPart bodyPart, String model) {
        this.userId = userId;
        this.imagePath = imagePath;
        this.format = format;
        this.bodyPart = bodyPart;
        this.model = model;
        this.createdAt = LocalDateTime.now();
    }

    public void addDetection(DetectionEntity detection) {
        detection.setAnalysis(this);
        detections.add(detection);
    }

    public Long getId() {
        return id;
    }

    public Long getUserId() {
        return userId;
    }

    public String getImagePath() {
        return imagePath;
    }

    public FileFormat getFormat() {
        return format;
    }

    public BodyPart getBodyPart() {
        return bodyPart;
    }

    public String getModel() {
        return model;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public List<DetectionEntity> getDetections() {
        return detections;
    }
}
