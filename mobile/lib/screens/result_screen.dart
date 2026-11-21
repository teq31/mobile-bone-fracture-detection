import 'package:flutter/material.dart';

import '../app_scope.dart';
import '../models/analysis.dart';
import '../widgets/common.dart';
import '../widgets/xray_viewer.dart';
import 'appointments_screen.dart';
import 'comparison_screen.dart';

/// The result of one analysis: the X-ray with the boxes (and the heatmap), a summary and the next steps.
class ResultScreen extends StatefulWidget {
  final Analysis analysis;

  const ResultScreen({super.key, required this.analysis});

  @override
  State<ResultScreen> createState() => _ResultScreenState();
}

class _ResultScreenState extends State<ResultScreen> {
  bool _showBoxes = true;
  bool _showHeatmap = false;

  @override
  Widget build(BuildContext context) {
    final analysis = widget.analysis;
    final user = AppScope.of(context).user!;
    final theme = Theme.of(context);
    final bytes = analysis.imageBytes;

    return Scaffold(
      appBar: AppBar(title: const Text('Result')),
      body: ListView(
        padding: const EdgeInsets.all(16),
        children: [
          AspectRatio(
            aspectRatio: 1,
            child: bytes == null
                ? const MessageView(icon: Icons.image_not_supported_outlined, message: 'The image is not available')
                : XRayViewer.forDetections(bytes, analysis.detections,
                    showBoxes: _showBoxes, showHeatmap: _showHeatmap),
          ),
          const SizedBox(height: 8),
          Wrap(
            spacing: 8,
            children: [
              FilterChip(
                label: const Text('Boxes'),
                selected: _showBoxes,
                onSelected: (value) => setState(() => _showBoxes = value),
              ),
              FilterChip(
                label: const Text('Heatmap'),
                selected: _showHeatmap,
                onSelected: (value) => setState(() => _showHeatmap = value),
              ),
            ],
          ),
          const SizedBox(height: 12),
          Card(
            child: ListTile(
              leading: Icon(
                analysis.hasFracture ? Icons.warning_amber_rounded : Icons.check_circle_outline,
                color: analysis.hasFracture ? theme.colorScheme.error : Colors.green,
                size: 36,
              ),
              title: Text(analysis.hasFracture
                  ? '${analysis.detections.length} possible fracture${analysis.detections.length == 1 ? '' : 's'} found'
                  : 'No fracture found'),
              subtitle: Text([
                'Model: ${analysis.model}',
                if (analysis.hasFracture) 'Highest confidence: ${(analysis.highestConfidence * 100).round()}%',
                formatDateTime(analysis.createdAt),
              ].join('\n')),
              isThreeLine: analysis.hasFracture,
            ),
          ),
          for (final (index, detection) in analysis.detections.indexed)
            ListTile(
              dense: true,
              leading: CircleAvatar(radius: 14, child: Text('${index + 1}')),
              title: Text('${detection.label} · ${(detection.confidence * 100).round()}%'),
              subtitle: Text('x ${detection.box.x.round()}, y ${detection.box.y.round()}, '
                  '${detection.box.width.round()} × ${detection.box.height.round()} px'),
            ),
          const SizedBox(height: 12),
          if (!user.isDoctor) const DisclaimerBanner(),
          const SizedBox(height: 16),
          if (user.isDoctor)
            FilledButton.icon(
              onPressed: user.verified
                  ? () => Navigator.of(context).push(MaterialPageRoute<void>(
                      builder: (_) => ComparisonScreen(analysis: analysis)))
                  : null,
              icon: const Icon(Icons.compare),
              label: Text(user.verified ? 'Compare all models' : 'Compare models (account not verified yet)'),
            ),
          if (user.isPatient)
            FilledButton.tonalIcon(
              onPressed: () => showRequestAppointmentSheet(context),
              icon: const Icon(Icons.event_available),
              label: const Text('Ask a doctor for an appointment'),
            ),
        ],
      ),
    );
  }
}
