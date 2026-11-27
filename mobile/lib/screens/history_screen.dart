import 'package:flutter/material.dart';

import '../app_scope.dart';
import '../models/analysis.dart';
import '../widgets/common.dart';
import 'result_screen.dart';

// reloads every time the tab is opened
class HistoryScreen extends StatefulWidget {
  final bool visible;

  const HistoryScreen({super.key, required this.visible});

  @override
  State<HistoryScreen> createState() => _HistoryScreenState();
}

class _HistoryScreenState extends State<HistoryScreen> {
  Future<List<HistoryItem>>? _items;

  @override
  void didChangeDependencies() {
    super.didChangeDependencies();
    if (widget.visible) _items ??= _load();
  }

  @override
  void didUpdateWidget(HistoryScreen oldWidget) {
    super.didUpdateWidget(oldWidget);
    if (widget.visible && !oldWidget.visible) {
      setState(() => _items = _load());
    }
  }

  Future<List<HistoryItem>> _load() => AppScope.of(context).analysis.history();

  Future<void> _open(HistoryItem item) async {
    try {
      final analysis = await AppScope.of(context).analysis.open(item);
      if (!mounted) return;
      await Navigator.of(context).push(MaterialPageRoute<void>(builder: (_) => ResultScreen(analysis: analysis)));
    } catch (e) {
      if (mounted) showError(context, e);
    }
  }

  @override
  Widget build(BuildContext context) {
    final items = _items;
    if (items == null) {
      return const SizedBox.shrink();
    }
    return FutureBuilder<List<HistoryItem>>(
      future: items,
      builder: (context, snapshot) {
        if (snapshot.connectionState != ConnectionState.done) {
          return const Center(child: CircularProgressIndicator());
        }
        if (snapshot.hasError) {
          return MessageView(
            icon: Icons.cloud_off,
            message: snapshot.error.toString(),
            onRetry: () => setState(() => _items = _load()),
          );
        }
        final list = snapshot.data!;
        if (list.isEmpty) {
          return const MessageView(icon: Icons.history, message: 'No analyses yet. Analyze an X-ray to see it here.');
        }
        return RefreshIndicator(
          onRefresh: () async {
            final reload = _load();
            setState(() => _items = reload);
            await reload;
          },
          child: ListView.separated(
            itemCount: list.length,
            separatorBuilder: (_, __) => const Divider(height: 1),
            itemBuilder: (context, index) {
              final item = list[index];
              return ListTile(
                leading: const CircleAvatar(child: Icon(Icons.image_search)),
                title: Text('Analysis #${item.analysisId}'),
                subtitle: Text('${item.model} · ${formatDateTime(item.createdAt)}'),
                trailing: const Icon(Icons.chevron_right),
                onTap: () => _open(item),
              );
            },
          ),
        );
      },
    );
  }
}
