import 'package:flutter/material.dart';

import '../app_scope.dart';
import '../models/app_notification.dart';
import '../widgets/common.dart';

class NotificationsScreen extends StatefulWidget {
  final bool visible;

  const NotificationsScreen({super.key, required this.visible});

  @override
  State<NotificationsScreen> createState() => _NotificationsScreenState();
}

class _NotificationsScreenState extends State<NotificationsScreen> {
  Future<List<AppNotification>>? _items;

  @override
  void didChangeDependencies() {
    super.didChangeDependencies();
    if (widget.visible) _items ??= _load();
  }

  @override
  void didUpdateWidget(NotificationsScreen oldWidget) {
    super.didUpdateWidget(oldWidget);
    if (widget.visible && !oldWidget.visible) {
      setState(() => _items = _load());
    }
  }

  Future<List<AppNotification>> _load() => AppScope.of(context).notifications.list();

  Future<void> _markRead(AppNotification notification) async {
    if (notification.read) return;
    try {
      await AppScope.of(context).notifications.markRead(notification.id);
      if (mounted) setState(() => _items = _load());
    } catch (e) {
      if (mounted) showError(context, e);
    }
  }

  static IconData _icon(String type) {
    switch (type) {
      case 'APPOINTMENT_CONFIRMED':
        return Icons.event_available;
      case 'APPOINTMENT_CANCELLED':
        return Icons.event_busy;
      case 'APPOINTMENT_REMINDER':
        return Icons.alarm;
      default:
        return Icons.notifications;
    }
  }

  @override
  Widget build(BuildContext context) {
    final items = _items;
    if (items == null) {
      return const SizedBox.shrink();
    }
    return FutureBuilder<List<AppNotification>>(
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
          return const MessageView(icon: Icons.notifications_none, message: 'No notifications');
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
              final notification = list[index];
              return ListTile(
                leading: Icon(_icon(notification.type)),
                title: Text(
                  notification.message,
                  style: TextStyle(fontWeight: notification.read ? FontWeight.normal : FontWeight.bold),
                ),
                subtitle: Text(formatDateTime(notification.createdAt)),
                trailing: notification.read ? null : const Icon(Icons.circle, size: 10, color: Colors.blue),
                onTap: () => _markRead(notification),
              );
            },
          ),
        );
      },
    );
  }
}
