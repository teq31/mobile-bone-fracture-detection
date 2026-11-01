package com.bonefracture.detection.composite;

import com.bonefracture.detection.Detection;
import com.bonefracture.detection.DetectionModel;
import com.bonefracture.detection.AnalysisResult;
import com.bonefracture.xray.XRayImage;
import org.junit.jupiter.api.Test;

import java.awt.Rectangle;

import static org.junit.jupiter.api.Assertions.*;

/**
 * TDD - Iteration 1 (Composite pattern).
 * Tests written BEFORE implementing DetectionGroup's logic.
 */
class DetectionGroupTest {

    private static class FakeModel extends DetectionModel {
        FakeModel(String name) { super(name, "1.0"); }
        @Override public AnalysisResult detect(XRayImage image) { return new AnalysisResult(this); }
    }

    private DetectionLeaf leaf(String label, float confidence, Rectangle box, DetectionModel model) {
        Detection d = new Detection(label, confidence, box, null);
        return new DetectionLeaf(d, model);
    }

    @Test
    void getConfidence_isTheAverageOfChildren() {
        DetectionModel yolo = new FakeModel("YOLO");
        DetectionModel rf = new FakeModel("RF-DETR");

        DetectionGroup group = new DetectionGroup();
        group.add(leaf("fracture", 0.8f, new Rectangle(0, 0, 10, 10), yolo));
        group.add(leaf("fracture", 0.6f, new Rectangle(0, 0, 10, 10), rf));

        assertEquals(0.7f, group.getConfidence(), 0.0001f);
    }

    @Test
    void getAgreementCount_countsDistinctModels() {
        DetectionModel yolo = new FakeModel("YOLO");
        DetectionModel rf = new FakeModel("RF-DETR");

        DetectionGroup group = new DetectionGroup();
        group.add(leaf("fracture", 0.8f, new Rectangle(0, 0, 10, 10), yolo));
        group.add(leaf("fracture", 0.6f, new Rectangle(0, 0, 10, 10), rf));

        assertEquals(2, group.getAgreementCount());
    }

    @Test
    void getAgreementCount_doesNotCountTheSameModelTwice() {
        DetectionModel yolo = new FakeModel("YOLO");

        DetectionGroup group = new DetectionGroup();
        group.add(leaf("fracture", 0.8f, new Rectangle(0, 0, 10, 10), yolo));
        group.add(leaf("fracture", 0.6f, new Rectangle(0, 0, 10, 10), yolo));

        assertEquals(1, group.getAgreementCount());
    }

    @Test
    void getLabel_isTheMostFrequentLabelAmongChildren() {
        DetectionModel a = new FakeModel("A");
        DetectionModel b = new FakeModel("B");
        DetectionModel c = new FakeModel("C");

        DetectionGroup group = new DetectionGroup();
        group.add(leaf("fracture", 0.8f, new Rectangle(0, 0, 10, 10), a));
        group.add(leaf("fracture", 0.7f, new Rectangle(0, 0, 10, 10), b));
        group.add(leaf("healthy", 0.6f, new Rectangle(0, 0, 10, 10), c));

        assertEquals("fracture", group.getLabel());
    }

    @Test
    void getBoundingBox_isTheUnionOfChildrenBoxes() {
        DetectionModel a = new FakeModel("A");
        DetectionModel b = new FakeModel("B");

        DetectionGroup group = new DetectionGroup();
        group.add(leaf("fracture", 0.8f, new Rectangle(0, 0, 10, 10), a));
        group.add(leaf("fracture", 0.7f, new Rectangle(5, 5, 10, 10), b));

        assertEquals(new Rectangle(0, 0, 15, 15), group.getBoundingBox());
    }

    @Test
    void accepts_whenIouIsAboveThreshold() {
        DetectionModel a = new FakeModel("A");
        DetectionModel b = new FakeModel("B");

        DetectionGroup group = new DetectionGroup(0.5f);
        group.add(leaf("fracture", 0.8f, new Rectangle(0, 0, 10, 10), a));

        // Heavily overlapping box -> should be accepted into the group.
        DetectionLeaf overlapping = leaf("fracture", 0.7f, new Rectangle(1, 1, 10, 10), b);
        assertTrue(group.accepts(overlapping));
    }

    @Test
    void accepts_anyComponentWhenGroupIsEmpty() {
        DetectionModel a = new FakeModel("A");

        // An empty group has no reference box yet, so it must accept the first component.
        DetectionGroup group = new DetectionGroup(0.5f);

        DetectionLeaf first = leaf("fracture", 0.8f, new Rectangle(0, 0, 10, 10), a);
        assertTrue(group.accepts(first));
    }

    @Test
    void accepts_rejectsWhenIouIsBelowThreshold() {
        DetectionModel a = new FakeModel("A");
        DetectionModel b = new FakeModel("B");

        DetectionGroup group = new DetectionGroup(0.5f);
        group.add(leaf("fracture", 0.8f, new Rectangle(0, 0, 10, 10), a));

        // Disjoint box -> IoU is 0 -> should be rejected.
        DetectionLeaf far = leaf("fracture", 0.7f, new Rectangle(100, 100, 10, 10), b);
        assertFalse(group.accepts(far));
    }

    @Test
    void getAgreementLevel_isNoneForOneModel() {
        DetectionModel a = new FakeModel("A");
        DetectionGroup group = new DetectionGroup();
        group.add(leaf("fracture", 0.8f, new Rectangle(0, 0, 10, 10), a));

        assertEquals(AgreementLevel.NONE, group.getAgreementLevel(3));
    }

    @Test
    void getAgreementLevel_isPartialForSomeModels() {
        DetectionModel a = new FakeModel("A");
        DetectionModel b = new FakeModel("B");
        DetectionGroup group = new DetectionGroup();
        group.add(leaf("fracture", 0.8f, new Rectangle(0, 0, 10, 10), a));
        group.add(leaf("fracture", 0.7f, new Rectangle(0, 0, 10, 10), b));

        assertEquals(AgreementLevel.PARTIAL, group.getAgreementLevel(3));
    }

    @Test
    void getAgreementLevel_isFullWhenAllModelsAgree() {
        DetectionModel a = new FakeModel("A");
        DetectionModel b = new FakeModel("B");
        DetectionModel c = new FakeModel("C");
        DetectionGroup group = new DetectionGroup();
        group.add(leaf("fracture", 0.8f, new Rectangle(0, 0, 10, 10), a));
        group.add(leaf("fracture", 0.7f, new Rectangle(0, 0, 10, 10), b));
        group.add(leaf("fracture", 0.6f, new Rectangle(0, 0, 10, 10), c));

        assertEquals(AgreementLevel.FULL, group.getAgreementLevel(3));
    }
}
