// GET /api/admin/audit (Ianis's AuditLoggingAspect)
class AuditEntry {
  final DateTime time;
  final String actor;
  final String action;
  final String target;
  final bool success;

  const AuditEntry({
    required this.time,
    required this.actor,
    required this.action,
    required this.target,
    required this.success,
  });

  factory AuditEntry.fromJson(Map<String, dynamic> json) => AuditEntry(
        time: DateTime.tryParse(json['time'] as String? ?? '') ?? DateTime.now(),
        actor: json['actor'] as String? ?? '',
        action: json['action'] as String? ?? '',
        target: json['target'] as String? ?? '',
        success: json['success'] as bool? ?? false,
      );
}
