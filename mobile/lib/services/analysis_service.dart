import 'dart:math';
import 'dart:typed_data';

import 'package:flutter/painting.dart' show decodeImageFromList;

import '../models/analysis.dart';
import 'api_client.dart';

enum XRaySource { gallery, camera, pdf, dicom }

enum BodyPart { wrist, elbow, hand, ankle }

String bodyPartLabel(BodyPart part) => part.name[0].toUpperCase() + part.name.substring(1);

class PickedXRay {
  final Uint8List bytes;
  final String fileName;
  final XRaySource source;

  const PickedXRay({required this.bytes, required this.fileName, required this.source});

  // pdf / dicom only get converted on the server
  bool get isDisplayable => source == XRaySource.gallery || source == XRaySource.camera;
}

// endpoints from Alexandra's api-contract.md
abstract class AnalysisService {
  Future<Analysis> analyze(PickedXRay xray, BodyPart bodyPart);

  Future<List<HistoryItem>> history();

  Future<Analysis> open(HistoryItem item);

  Future<Comparison> compare(String analysisId);

  bool get isDemo;
}

class HttpAnalysisService implements AnalysisService {
  final ApiClient _api;

  HttpAnalysisService(this._api);

  @override
  bool get isDemo => false;

  @override
  Future<Analysis> analyze(PickedXRay xray, BodyPart bodyPart) async {
    final json = await _api.upload(
      '/api/analyses',
      field: 'image',
      bytes: xray.bytes,
      fileName: xray.fileName,
      fields: {'bodyPart': bodyPart.name.toUpperCase(), 'source': xray.source.name.toUpperCase()},
    ) as Map<String, dynamic>;
    return _withImage(Analysis.fromJson(json), fallback: xray.isDisplayable ? xray.bytes : null);
  }

  @override
  Future<List<HistoryItem>> history() async {
    final json = await _api.get('/api/analyses/history') as Map<String, dynamic>;
    return (json['items'] as List<dynamic>? ?? [])
        .map((i) => HistoryItem.fromJson(i as Map<String, dynamic>))
        .toList();
  }

  @override
  Future<Analysis> open(HistoryItem item) async {
    final json = await _api.get('/api/analyses/${item.analysisId}') as Map<String, dynamic>;
    return _withImage(Analysis.fromJson(json));
  }

  @override
  Future<Comparison> compare(String analysisId) async =>
      Comparison.fromJson(await _api.get('/api/analyses/$analysisId/comparison') as Map<String, dynamic>);

  // boxes are in the pixels of imageUrl, so show that one
  Future<Analysis> _withImage(Analysis analysis, {Uint8List? fallback}) async {
    final url = analysis.imageUrl;
    if (url != null && url.isNotEmpty) {
      try {
        return analysis.withImage(await _api.getBytes(url));
      } on ApiException {
        // keep the picked image below
      }
    }
    return fallback == null ? analysis : analysis.withImage(fallback);
  }
}

// fake analysis until /api/analyses + the model are ready (same image -> same result)
class MockAnalysisService implements AnalysisService {
  static const _models = ['YOLO', 'RF-DETR'];

  final List<Analysis> _analyses = [];
  int _nextId = 1;

  @override
  bool get isDemo => true;

  @override
  Future<Analysis> analyze(PickedXRay xray, BodyPart bodyPart) async {
    if (!xray.isDisplayable) {
      throw const ApiException(
          'PDF and DICOM files are converted by the server. Start the backend or choose a JPG / PNG image.');
    }
    final image = await decodeImageFromList(xray.bytes);
    final width = image.width.toDouble();
    final height = image.height.toDouble();
    image.dispose();
    await Future<void>.delayed(const Duration(milliseconds: 1200));

    final random = Random(_seed(xray.bytes));
    final detections = <Detection>[];
    if (random.nextDouble() < 0.8) {
      final boxWidth = width * (0.14 + random.nextDouble() * 0.08);
      final boxHeight = height * (0.10 + random.nextDouble() * 0.06);
      detections.add(Detection(
        label: 'fracture',
        confidence: 0.62 + random.nextDouble() * 0.3,
        box: BoundingBox(
          x: width * (0.3 + random.nextDouble() * 0.25),
          y: height * (0.45 + random.nextDouble() * 0.25),
          width: boxWidth,
          height: boxHeight,
        ),
      ));
    }
    final analysis = Analysis(
      id: '${_nextId++}',
      model: _models.first,
      detections: detections,
      createdAt: DateTime.now(),
      imageBytes: xray.bytes,
    );
    _analyses.insert(0, analysis);
    return analysis;
  }

  @override
  Future<List<HistoryItem>> history() async => _analyses
      .map((a) => HistoryItem(analysisId: a.id, model: a.model, createdAt: a.createdAt))
      .toList();

  @override
  Future<Analysis> open(HistoryItem item) async => _analyses.firstWhere((a) => a.id == item.analysisId);

  @override
  Future<Comparison> compare(String analysisId) async {
    final analysis = _analyses.firstWhere((a) => a.id == analysisId);
    await Future<void>.delayed(const Duration(milliseconds: 800));
    return Comparison(
      analysisId: analysisId,
      totalModels: _models.length,
      findings: analysis.detections
          .map((d) => ComparisonFinding(
                label: d.label,
                confidence: (d.confidence - 0.05).clamp(0, 1).toDouble(),
                box: d.box,
                agreementLevel: 'FULL',
                agreementCount: _models.length,
                models: _models,
              ))
          .toList(),
    );
  }

  static int _seed(Uint8List bytes) {
    var hash = bytes.length;
    final step = max(1, bytes.length ~/ 512);
    for (var i = 0; i < bytes.length; i += step) {
      hash = (hash * 31 + bytes[i]) & 0x7fffffff;
    }
    return hash;
  }
}
