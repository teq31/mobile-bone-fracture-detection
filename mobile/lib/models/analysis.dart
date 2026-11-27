import 'dart:typed_data';

// in pixels of the analysed image, not screen pixels
class BoundingBox {
  final double x;
  final double y;
  final double width;
  final double height;

  const BoundingBox({required this.x, required this.y, required this.width, required this.height});

  factory BoundingBox.fromJson(Map<String, dynamic> json) => BoundingBox(
        x: (json['x'] as num).toDouble(),
        y: (json['y'] as num).toDouble(),
        width: (json['width'] as num).toDouble(),
        height: (json['height'] as num).toDouble(),
      );
}

class Detection {
  final String label;
  final double confidence;
  final BoundingBox box;

  const Detection({required this.label, required this.confidence, required this.box});

  factory Detection.fromJson(Map<String, dynamic> json) => Detection(
        label: json['label'] as String? ?? 'fracture',
        confidence: (json['confidence'] as num? ?? 0).toDouble(),
        box: BoundingBox.fromJson(json['boundingBox'] as Map<String, dynamic>),
      );
}

class Analysis {
  final String id;
  final String model;
  final List<Detection> detections;
  final String? imageUrl;
  final DateTime createdAt;

  // filled in by the app, not part of the json
  final Uint8List? imageBytes;

  const Analysis({
    required this.id,
    required this.model,
    required this.detections,
    required this.createdAt,
    this.imageUrl,
    this.imageBytes,
  });

  factory Analysis.fromJson(Map<String, dynamic> json) => Analysis(
        id: json['analysisId'].toString(),
        model: json['model'] as String? ?? '',
        detections: (json['detections'] as List<dynamic>? ?? [])
            .map((d) => Detection.fromJson(d as Map<String, dynamic>))
            .toList(),
        imageUrl: json['imageUrl'] as String?,
        createdAt: DateTime.tryParse(json['createdAt'] as String? ?? '') ?? DateTime.now(),
      );

  Analysis withImage(Uint8List bytes) => Analysis(
        id: id,
        model: model,
        detections: detections,
        imageUrl: imageUrl,
        createdAt: createdAt,
        imageBytes: bytes,
      );

  bool get hasFracture => detections.isNotEmpty;

  double get highestConfidence =>
      detections.fold<double>(0, (best, d) => d.confidence > best ? d.confidence : best);
}

class HistoryItem {
  final String analysisId;
  final String model;
  final String? imageUrl;
  final DateTime createdAt;

  const HistoryItem({required this.analysisId, required this.model, required this.createdAt, this.imageUrl});

  factory HistoryItem.fromJson(Map<String, dynamic> json) => HistoryItem(
        analysisId: json['analysisId'].toString(),
        model: json['model'] as String? ?? '',
        imageUrl: json['imageUrl'] as String?,
        createdAt: DateTime.tryParse(json['createdAt'] as String? ?? '') ?? DateTime.now(),
      );
}

class ComparisonFinding {
  final String label;
  final double confidence;
  final BoundingBox? box;
  final String agreementLevel;
  final int agreementCount;
  final List<String> models;

  const ComparisonFinding({
    required this.label,
    required this.confidence,
    required this.box,
    required this.agreementLevel,
    required this.agreementCount,
    required this.models,
  });

  factory ComparisonFinding.fromJson(Map<String, dynamic> json) => ComparisonFinding(
        label: json['label'] as String? ?? 'fracture',
        confidence: (json['confidence'] as num? ?? 0).toDouble(),
        box: json['boundingBox'] == null ? null : BoundingBox.fromJson(json['boundingBox'] as Map<String, dynamic>),
        agreementLevel: json['agreementLevel'] as String? ?? 'NONE',
        agreementCount: (json['agreementCount'] as num? ?? 0).toInt(),
        models: (json['models'] as List<dynamic>? ?? []).map((m) => m.toString()).toList(),
      );
}

class Comparison {
  final String analysisId;
  final int totalModels;
  final List<ComparisonFinding> findings;

  const Comparison({required this.analysisId, required this.totalModels, required this.findings});

  // the server doesn't send the id back, so we pass the one we asked for
  factory Comparison.fromJson(Map<String, dynamic> json, {required String analysisId}) => Comparison(
        analysisId: analysisId,
        totalModels: (json['totalModels'] as num? ?? 0).toInt(),
        findings: (json['findings'] as List<dynamic>? ?? [])
            .map((f) => ComparisonFinding.fromJson(f as Map<String, dynamic>))
            .toList(),
      );
}
