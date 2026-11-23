package com.bonefracture.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "detections")
public class DetectionEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "analysis_id")
    private AnalysisEntity analysis;

    @Column(nullable = false)
    private String label;

    @Column(nullable = false)
    private float confidence;

    private int boxX;
    private int boxY;
    private int boxWidth;
    private int boxHeight;

    protected DetectionEntity() {
    }

    public DetectionEntity(String label, float confidence, int boxX, int boxY, int boxWidth, int boxHeight) {
        this.label = label;
        this.confidence = confidence;
        this.boxX = boxX;
        this.boxY = boxY;
        this.boxWidth = boxWidth;
        this.boxHeight = boxHeight;
    }

    public Long getId() {
        return id;
    }

    public AnalysisEntity getAnalysis() {
        return analysis;
    }

    public void setAnalysis(AnalysisEntity analysis) {
        this.analysis = analysis;
    }

    public String getLabel() {
        return label;
    }

    public float getConfidence() {
        return confidence;
    }

    public int getBoxX() {
        return boxX;
    }

    public int getBoxY() {
        return boxY;
    }

    public int getBoxWidth() {
        return boxWidth;
    }

    public int getBoxHeight() {
        return boxHeight;
    }
}
