import 'dart:ui';

import 'package:bone_fracture_app/models/analysis.dart';
import 'package:bone_fracture_app/widgets/image_fit.dart';
import 'package:flutter_test/flutter_test.dart';

/// The boxes come in the pixels of the X-ray; on the screen the X-ray is scaled and centred (BoxFit.contain).
void main() {
  const box = BoundingBox(x: 100, y: 50, width: 200, height: 100);

  test('wide image in a square area: scaled to the width, centred vertically', () {
    final fit = ImageFit.contain(const Size(800, 400), const Size(400, 400));

    expect(fit.scale, 0.5);
    expect(fit.toScreen(box), const Rect.fromLTWH(50, 125, 100, 50));
  });

  test('tall image in a square area: scaled to the height, centred horizontally', () {
    final fit = ImageFit.contain(const Size(300, 600), const Size(400, 400));

    expect(fit.toScreen(const BoundingBox(x: 0, y: 0, width: 300, height: 600)),
        const Rect.fromLTWH(100, 0, 200, 400));
  });

  test('empty area draws nothing', () {
    expect(ImageFit.contain(const Size(10, 10), Size.zero).scale, 0);
  });
}
