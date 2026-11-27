// one line of GET /api/performance/stats (PerformanceMonitoringAspect)
class OperationStats {
  final String operation;
  final int calls;
  final int failures;
  final double averageMillis;
  final double maxMillis;

  const OperationStats({
    required this.operation,
    required this.calls,
    required this.failures,
    required this.averageMillis,
    required this.maxMillis,
  });

  factory OperationStats.fromJson(Map<String, dynamic> json) => OperationStats(
        operation: json['operation'] as String? ?? '',
        calls: (json['calls'] as num? ?? 0).toInt(),
        failures: (json['failures'] as num? ?? 0).toInt(),
        averageMillis: (json['averageMillis'] as num? ?? 0).toDouble(),
        maxMillis: (json['maxMillis'] as num? ?? 0).toDouble(),
      );
}
