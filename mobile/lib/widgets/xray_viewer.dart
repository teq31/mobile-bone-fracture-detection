import 'dart:typed_data';

import 'package:flutter/material.dart';

import '../models/analysis.dart';
import 'image_fit.dart';

/// The X-ray with the detections drawn over it: a box with the confidence for each fracture and,
/// optionally, a heatmap (strongest in the centre of the box, like HeatmapGenerator on the server).
class XRayViewer extends StatefulWidget {
  final Uint8List imageBytes;
  final List<BoundingBox> boxes;
  final List<String> labels;
  final List<double> confidences;
  final bool showBoxes;
  final bool showHeatmap;

  const XRayViewer({
    super.key,
    required this.imageBytes,
    required this.boxes,
    required this.labels,
    required this.confidences,
    this.showBoxes = true,
    this.showHeatmap = false,
  });

  factory XRayViewer.forDetections(Uint8List bytes, List<Detection> detections,
          {bool showBoxes = true, bool showHeatmap = false}) =>
      XRayViewer(
        imageBytes: bytes,
        boxes: detections.map((d) => d.box).toList(),
        labels: detections.map((d) => d.label).toList(),
        confidences: detections.map((d) => d.confidence).toList(),
        showBoxes: showBoxes,
        showHeatmap: showHeatmap,
      );

  @override
  State<XRayViewer> createState() => _XRayViewerState();
}

class _XRayViewerState extends State<XRayViewer> {
  late Future<Size> _imageSize;

  @override
  void initState() {
    super.initState();
    _imageSize = _decodeSize(widget.imageBytes);
  }

  @override
  void didUpdateWidget(XRayViewer oldWidget) {
    super.didUpdateWidget(oldWidget);
    if (!identical(oldWidget.imageBytes, widget.imageBytes)) {
      _imageSize = _decodeSize(widget.imageBytes);
    }
  }

  static Future<Size> _decodeSize(Uint8List bytes) async {
    final image = await decodeImageFromList(bytes);
    final size = Size(image.width.toDouble(), image.height.toDouble());
    image.dispose();
    return size;
  }

  @override
  Widget build(BuildContext context) {
    return ClipRRect(
      borderRadius: BorderRadius.circular(12),
      child: ColoredBox(
        color: Colors.black,
        child: FutureBuilder<Size>(
          future: _imageSize,
          builder: (context, snapshot) {
            final size = snapshot.data;
            return LayoutBuilder(
              builder: (context, constraints) {
                final area = Size(constraints.maxWidth, constraints.maxHeight);
                return Stack(
                  fit: StackFit.expand,
                  children: [
                    Image.memory(widget.imageBytes, fit: BoxFit.contain, gaplessPlayback: true),
                    if (size != null)
                      CustomPaint(
                        painter: _DetectionPainter(
                          fit: ImageFit.contain(size, area),
                          boxes: widget.boxes,
                          labels: widget.labels,
                          confidences: widget.confidences,
                          showBoxes: widget.showBoxes,
                          showHeatmap: widget.showHeatmap,
                        ),
                      ),
                  ],
                );
              },
            );
          },
        ),
      ),
    );
  }
}

class _DetectionPainter extends CustomPainter {
  final ImageFit fit;
  final List<BoundingBox> boxes;
  final List<String> labels;
  final List<double> confidences;
  final bool showBoxes;
  final bool showHeatmap;

  _DetectionPainter({
    required this.fit,
    required this.boxes,
    required this.labels,
    required this.confidences,
    required this.showBoxes,
    required this.showHeatmap,
  });

  static const _boxColor = Color(0xFFFF3B30);

  @override
  void paint(Canvas canvas, Size size) {
    for (var i = 0; i < boxes.length; i++) {
      final rect = fit.toScreen(boxes[i]);
      if (showHeatmap) {
        _paintHeat(canvas, rect, confidences[i]);
      }
      if (showBoxes) {
        _paintBox(canvas, rect, '${labels[i]} ${(confidences[i] * 100).round()}%');
      }
    }
  }

  void _paintHeat(Canvas canvas, Rect rect, double confidence) {
    final radius = rect.longestSide * 1.1;
    final strength = (confidence.clamp(0.0, 1.0) * 200).round();
    final paint = Paint()
      ..shader = RadialGradient(colors: [
        Color.fromARGB(strength, 255, 40, 0),
        Color.fromARGB((strength * 0.75).round(), 255, 220, 0),
        Color.fromARGB((strength * 0.45).round(), 0, 200, 120),
        Color.fromARGB((strength * 0.2).round(), 0, 90, 255),
        const Color(0x000000FF),
      ], stops: const [0, 0.3, 0.55, 0.8, 1]).createShader(Rect.fromCircle(center: rect.center, radius: radius));
    canvas.drawCircle(rect.center, radius, paint);
  }

  void _paintBox(Canvas canvas, Rect rect, String text) {
    canvas.drawRect(
      rect,
      Paint()
        ..color = _boxColor
        ..style = PaintingStyle.stroke
        ..strokeWidth = 2.5,
    );
    final label = TextPainter(
      text: TextSpan(
        text: ' $text ',
        style: const TextStyle(color: Colors.white, fontSize: 12, fontWeight: FontWeight.w600),
      ),
      textDirection: TextDirection.ltr,
    )..layout();
    final top = rect.top - label.height < 0 ? rect.bottom : rect.top - label.height;
    canvas.drawRect(Rect.fromLTWH(rect.left, top, label.width, label.height), Paint()..color = _boxColor);
    label.paint(canvas, Offset(rect.left, top));
  }

  @override
  bool shouldRepaint(_DetectionPainter old) =>
      old.fit.scale != fit.scale ||
      old.fit.offset != fit.offset ||
      old.boxes != boxes ||
      old.showBoxes != showBoxes ||
      old.showHeatmap != showHeatmap;
}
