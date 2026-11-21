/// Body returned by /api/notifications (created by NotificationAspect on the backend).
class AppNotification {
  final int id;
  final String type;
  final String message;
  final DateTime createdAt;
  final bool read;

  const AppNotification({
    required this.id,
    required this.type,
    required this.message,
    required this.createdAt,
    required this.read,
  });

  factory AppNotification.fromJson(Map<String, dynamic> json) => AppNotification(
        id: (json['id'] as num).toInt(),
        type: json['type'] as String? ?? '',
        message: json['message'] as String? ?? '',
        createdAt: DateTime.tryParse(json['createdAt'] as String? ?? '') ?? DateTime.now(),
        read: json['read'] as bool? ?? false,
      );
}
