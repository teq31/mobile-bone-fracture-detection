import 'dart:math';
import 'dart:ui';

import '../models/analysis.dart';

/// Where an image of [imageSize] is drawn inside [area] with BoxFit.contain (centred),
/// and where a box from the image's pixels lands on the screen.
class ImageFit {
  final double scale;
  final Offset offset;

  const ImageFit._(this.scale, this.offset);

  factory ImageFit.contain(Size imageSize, Size area) {
    if (imageSize.isEmpty || area.isEmpty) {
      return const ImageFit._(0, Offset.zero);
    }
    final scale = min(area.width / imageSize.width, area.height / imageSize.height);
    final drawn = Size(imageSize.width * scale, imageSize.height * scale);
    return ImageFit._(scale, Offset((area.width - drawn.width) / 2, (area.height - drawn.height) / 2));
  }

  Rect toScreen(BoundingBox box) =>
      Rect.fromLTWH(offset.dx + box.x * scale, offset.dy + box.y * scale, box.width * scale, box.height * scale);
}
