import 'package:file_picker/file_picker.dart';
import 'package:flutter/material.dart';
import 'package:image_picker/image_picker.dart';

import '../app_scope.dart';
import '../services/analysis_service.dart';
import '../widgets/common.dart';
import 'result_screen.dart';

/// Choose the X-ray (gallery, camera, PDF, DICOM) and the body part, then send it for analysis.
class AnalyzeScreen extends StatefulWidget {
  final VoidCallback? onAnalyzed;

  const AnalyzeScreen({super.key, this.onAnalyzed});

  @override
  State<AnalyzeScreen> createState() => _AnalyzeScreenState();
}

class _AnalyzeScreenState extends State<AnalyzeScreen> {
  PickedXRay? _xray;
  BodyPart _bodyPart = BodyPart.wrist;
  bool _busy = false;

  Future<void> _pickImage(ImageSource source) async {
    try {
      final file = await ImagePicker().pickImage(source: source, imageQuality: 95, maxWidth: 2048);
      if (file == null) return;
      final bytes = await file.readAsBytes();
      setState(() => _xray = PickedXRay(
            bytes: bytes,
            fileName: file.name,
            source: source == ImageSource.camera ? XRaySource.camera : XRaySource.gallery,
          ));
    } catch (e) {
      if (mounted) showError(context, 'Could not open the image: $e');
    }
  }

  Future<void> _pickDocument() async {
    try {
      final result = await FilePicker.platform.pickFiles(
        type: FileType.custom,
        allowedExtensions: const ['pdf', 'dcm', 'jpg', 'jpeg', 'png'],
        withData: true,
      );
      final file = result?.files.single;
      if (file == null || file.bytes == null) return;
      final extension = (file.extension ?? '').toLowerCase();
      final source = extension == 'pdf'
          ? XRaySource.pdf
          : extension == 'dcm'
              ? XRaySource.dicom
              : XRaySource.gallery;
      setState(() => _xray = PickedXRay(bytes: file.bytes!, fileName: file.name, source: source));
    } catch (e) {
      if (mounted) showError(context, 'Could not open the file: $e');
    }
  }

  Future<void> _analyze() async {
    final xray = _xray;
    if (xray == null) return;
    setState(() => _busy = true);
    final state = AppScope.of(context);
    try {
      final analysis = await state.analysis.analyze(xray, _bodyPart);
      if (!mounted) return;
      widget.onAnalyzed?.call();
      await Navigator.of(context).push(MaterialPageRoute<void>(builder: (_) => ResultScreen(analysis: analysis)));
    } catch (e) {
      if (mounted) showError(context, e);
    } finally {
      if (mounted) setState(() => _busy = false);
    }
  }

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);
    final state = AppScope.of(context);
    final xray = _xray;
    return ListView(
      padding: const EdgeInsets.all(16),
      children: [
        if (state.analysis.isDemo)
          Card(
            color: theme.colorScheme.secondaryContainer,
            child: const ListTile(
              leading: Icon(Icons.science_outlined),
              title: Text('Demo analysis'),
              subtitle: Text('The trained model is not connected yet, so the result is simulated on the phone.'),
            ),
          ),
        const SizedBox(height: 8),
        AspectRatio(
          aspectRatio: 1,
          child: DecoratedBox(
            decoration: BoxDecoration(
              color: theme.colorScheme.surfaceContainerHighest,
              borderRadius: BorderRadius.circular(16),
            ),
            child: xray == null
                ? const MessageView(icon: Icons.add_photo_alternate_outlined, message: 'Choose an X-ray of the wrist, elbow, hand or ankle')
                : xray.isDisplayable
                    ? ClipRRect(
                        borderRadius: BorderRadius.circular(16),
                        child: Image.memory(xray.bytes, fit: BoxFit.contain),
                      )
                    : MessageView(
                        icon: xray.source == XRaySource.pdf ? Icons.picture_as_pdf : Icons.medical_information,
                        message: xray.fileName,
                      ),
          ),
        ),
        const SizedBox(height: 16),
        Wrap(
          spacing: 8,
          runSpacing: 8,
          alignment: WrapAlignment.center,
          children: [
            OutlinedButton.icon(
              onPressed: _busy ? null : () => _pickImage(ImageSource.camera),
              icon: const Icon(Icons.photo_camera),
              label: const Text('Camera'),
            ),
            OutlinedButton.icon(
              onPressed: _busy ? null : () => _pickImage(ImageSource.gallery),
              icon: const Icon(Icons.photo_library),
              label: const Text('Gallery'),
            ),
            OutlinedButton.icon(
              onPressed: _busy ? null : _pickDocument,
              icon: const Icon(Icons.upload_file),
              label: const Text('PDF / DICOM'),
            ),
          ],
        ),
        const SizedBox(height: 20),
        Text('Body part', style: theme.textTheme.titleSmall),
        const SizedBox(height: 8),
        Wrap(
          spacing: 8,
          children: [
            for (final part in BodyPart.values)
              ChoiceChip(
                label: Text(bodyPartLabel(part)),
                selected: _bodyPart == part,
                onSelected: _busy ? null : (_) => setState(() => _bodyPart = part),
              ),
          ],
        ),
        const SizedBox(height: 24),
        FilledButton.icon(
          onPressed: xray == null || _busy ? null : _analyze,
          icon: _busy
              ? const SizedBox(height: 18, width: 18, child: CircularProgressIndicator(strokeWidth: 2))
              : const Icon(Icons.search),
          label: Text(_busy ? 'Analyzing...' : 'Analyze'),
        ),
      ],
    );
  }
}
