import 'package:flutter/material.dart';

import '../app_scope.dart';
import '../models/analysis.dart';
import '../widgets/common.dart';
import '../widgets/xray_viewer.dart';

// doctor only
class ComparisonScreen extends StatefulWidget {
  final Analysis analysis;

  const ComparisonScreen({super.key, required this.analysis});

  @override
  State<ComparisonScreen> createState() => _ComparisonScreenState();
}

class _ComparisonScreenState extends State<ComparisonScreen> {
  Future<Comparison>? _comparison;

  @override
  void didChangeDependencies() {
    super.didChangeDependencies();
    _comparison ??= AppScope.of(context).analysis.compare(widget.analysis.id);
  }

  void _retry() => setState(() => _comparison = AppScope.of(context).analysis.compare(widget.analysis.id));

  static Color _agreementColor(String level) {
    switch (level) {
      case 'FULL':
        return Colors.green;
      case 'PARTIAL':
        return Colors.orange;
      default:
        return Colors.grey;
    }
  }

  @override
  Widget build(BuildContext context) {
    final bytes = widget.analysis.imageBytes;
    return Scaffold(
      appBar: AppBar(title: const Text('Model comparison')),
      body: FutureBuilder<Comparison>(
        future: _comparison,
        builder: (context, snapshot) {
          if (snapshot.connectionState != ConnectionState.done) {
            return const Center(child: CircularProgressIndicator());
          }
          if (snapshot.hasError) {
            return MessageView(icon: Icons.error_outline, message: snapshot.error.toString(), onRetry: _retry);
          }
          final comparison = snapshot.data!;
          final drawn = comparison.findings.where((f) => f.box != null).toList();
          return ListView(
            padding: const EdgeInsets.all(16),
            children: [
              if (bytes != null)
                AspectRatio(
                  aspectRatio: 1,
                  child: XRayViewer(
                    imageBytes: bytes,
                    boxes: drawn.map((f) => f.box!).toList(),
                    labels: drawn.map((f) => '${f.agreementCount}/${comparison.totalModels}').toList(),
                    confidences: drawn.map((f) => f.confidence).toList(),
                  ),
                ),
              const SizedBox(height: 12),
              Text('${comparison.totalModels} models compared', style: Theme.of(context).textTheme.titleMedium),
              if (comparison.findings.isEmpty)
                const ListTile(title: Text('No model found a fracture')),
              for (final finding in comparison.findings)
                Card(
                  child: ListTile(
                    leading: Icon(Icons.circle, color: _agreementColor(finding.agreementLevel)),
                    title: Text('${finding.label} · ${(finding.confidence * 100).round()}%'),
                    subtitle: Text('Agreement: ${finding.agreementLevel.toLowerCase()} '
                        '(${finding.agreementCount} of ${comparison.totalModels})\n${finding.models.join(', ')}'),
                    isThreeLine: true,
                  ),
                ),
            ],
          );
        },
      ),
    );
  }
}
