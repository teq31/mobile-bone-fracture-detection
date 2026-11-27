import 'package:flutter/material.dart';

import '../app_scope.dart';
import '../models/audit_entry.dart';
import '../models/operation_stats.dart';
import '../models/user.dart';
import '../widgets/common.dart';

// what an administrator sees instead of the patient / doctor tabs
class AdminHomeScreen extends StatefulWidget {
  const AdminHomeScreen({super.key});

  @override
  State<AdminHomeScreen> createState() => _AdminHomeScreenState();
}

class _AdminHomeScreenState extends State<AdminHomeScreen> {
  int _tab = 0;

  @override
  Widget build(BuildContext context) {
    final state = AppScope.of(context);
    return Scaffold(
      appBar: AppBar(
        title: Text(const ['Doctors to verify', 'Account activity', 'Analysis speed'][_tab]),
        actions: [
          IconButton(tooltip: 'Log out', icon: const Icon(Icons.logout), onPressed: state.logout),
        ],
      ),
      body: IndexedStack(
        index: _tab,
        children: [
          _UnverifiedDoctors(visible: _tab == 0),
          _AuditLog(visible: _tab == 1),
          _Performance(visible: _tab == 2),
        ],
      ),
      bottomNavigationBar: NavigationBar(
        selectedIndex: _tab,
        onDestinationSelected: (index) => setState(() => _tab = index),
        destinations: const [
          NavigationDestination(icon: Icon(Icons.verified_user_outlined), selectedIcon: Icon(Icons.verified_user), label: 'Doctors'),
          NavigationDestination(icon: Icon(Icons.receipt_long_outlined), selectedIcon: Icon(Icons.receipt_long), label: 'Audit'),
          NavigationDestination(icon: Icon(Icons.speed_outlined), selectedIcon: Icon(Icons.speed), label: 'Speed'),
        ],
      ),
    );
  }
}

class _UnverifiedDoctors extends StatefulWidget {
  final bool visible;

  const _UnverifiedDoctors({required this.visible});

  @override
  State<_UnverifiedDoctors> createState() => _UnverifiedDoctorsState();
}

class _UnverifiedDoctorsState extends State<_UnverifiedDoctors> {
  Future<List<User>>? _doctors;
  String? _verifying;

  @override
  void didChangeDependencies() {
    super.didChangeDependencies();
    if (widget.visible) _doctors ??= _load();
  }

  @override
  void didUpdateWidget(_UnverifiedDoctors oldWidget) {
    super.didUpdateWidget(oldWidget);
    if (widget.visible && !oldWidget.visible) setState(() => _doctors = _load());
  }

  Future<List<User>> _load() => AppScope.of(context).admin.unverifiedDoctors();

  Future<void> _verify(User doctor) async {
    setState(() => _verifying = doctor.email);
    try {
      await AppScope.of(context).admin.verifyDoctor(doctor.email);
      if (!mounted) return;
      ScaffoldMessenger.of(context).showSnackBar(SnackBar(content: Text('${doctor.name} is verified')));
      setState(() => _doctors = _load());
    } catch (e) {
      if (mounted) showError(context, e);
    } finally {
      if (mounted) setState(() => _verifying = null);
    }
  }

  @override
  Widget build(BuildContext context) {
    final doctors = _doctors;
    if (doctors == null) return const SizedBox.shrink();
    return FutureBuilder<List<User>>(
      future: doctors,
      builder: (context, snapshot) {
        if (snapshot.connectionState != ConnectionState.done) {
          return const Center(child: CircularProgressIndicator());
        }
        if (snapshot.hasError) {
          return MessageView(
            icon: Icons.cloud_off,
            message: snapshot.error.toString(),
            onRetry: () => setState(() => _doctors = _load()),
          );
        }
        final list = snapshot.data!;
        if (list.isEmpty) {
          return const MessageView(icon: Icons.verified_user, message: 'All doctors are verified');
        }
        return ListView.separated(
          itemCount: list.length,
          separatorBuilder: (_, __) => const Divider(height: 1),
          itemBuilder: (context, index) {
            final doctor = list[index];
            final busy = _verifying == doctor.email;
            return ListTile(
              leading: const CircleAvatar(child: Icon(Icons.medical_services)),
              title: Text(doctor.name),
              subtitle: Text(doctor.email),
              trailing: FilledButton.tonal(
                onPressed: _verifying != null ? null : () => _verify(doctor),
                child: busy
                    ? const SizedBox(height: 16, width: 16, child: CircularProgressIndicator(strokeWidth: 2))
                    : const Text('Verify'),
              ),
            );
          },
        );
      },
    );
  }
}

class _AuditLog extends StatefulWidget {
  final bool visible;

  const _AuditLog({required this.visible});

  @override
  State<_AuditLog> createState() => _AuditLogState();
}

class _AuditLogState extends State<_AuditLog> {
  Future<List<AuditEntry>>? _entries;

  @override
  void didChangeDependencies() {
    super.didChangeDependencies();
    if (widget.visible) _entries ??= _load();
  }

  @override
  void didUpdateWidget(_AuditLog oldWidget) {
    super.didUpdateWidget(oldWidget);
    if (widget.visible && !oldWidget.visible) setState(() => _entries = _load());
  }

  Future<List<AuditEntry>> _load() => AppScope.of(context).admin.audit();

  static IconData _icon(String action) {
    switch (action) {
      case 'REGISTER':
        return Icons.person_add_alt;
      case 'LOGIN':
        return Icons.login;
      case 'VERIFY_DOCTOR':
        return Icons.verified_user;
      default:
        return Icons.history;
    }
  }

  @override
  Widget build(BuildContext context) {
    final entries = _entries;
    if (entries == null) return const SizedBox.shrink();
    return FutureBuilder<List<AuditEntry>>(
      future: entries,
      builder: (context, snapshot) {
        if (snapshot.connectionState != ConnectionState.done) {
          return const Center(child: CircularProgressIndicator());
        }
        if (snapshot.hasError) {
          return MessageView(
            icon: Icons.cloud_off,
            message: snapshot.error.toString(),
            onRetry: () => setState(() => _entries = _load()),
          );
        }
        final list = snapshot.data!;
        if (list.isEmpty) {
          return const MessageView(icon: Icons.receipt_long, message: 'No account activity yet');
        }
        return RefreshIndicator(
          onRefresh: () async {
            final reload = _load();
            setState(() => _entries = reload);
            await reload;
          },
          child: ListView.separated(
            itemCount: list.length,
            separatorBuilder: (_, __) => const Divider(height: 1),
            itemBuilder: (context, index) {
              final entry = list[index];
              return ListTile(
                leading: Icon(_icon(entry.action)),
                title: Text('${entry.action.replaceAll('_', ' ').toLowerCase()} · ${entry.target}'),
                subtitle: Text('${entry.actor} · ${formatDateTime(entry.time)}'),
                trailing: Icon(
                  entry.success ? Icons.check_circle_outline : Icons.error_outline,
                  color: entry.success ? Colors.green : Theme.of(context).colorScheme.error,
                ),
              );
            },
          ),
        );
      },
    );
  }
}

// how long each step of the analysis takes on the server, slowest first
class _Performance extends StatefulWidget {
  final bool visible;

  const _Performance({required this.visible});

  @override
  State<_Performance> createState() => _PerformanceState();
}

class _PerformanceState extends State<_Performance> {
  Future<List<OperationStats>>? _stats;

  @override
  void didChangeDependencies() {
    super.didChangeDependencies();
    if (widget.visible) _stats ??= _load();
  }

  @override
  void didUpdateWidget(_Performance oldWidget) {
    super.didUpdateWidget(oldWidget);
    if (widget.visible && !oldWidget.visible) setState(() => _stats = _load());
  }

  Future<List<OperationStats>> _load() => AppScope.of(context).admin.performance();

  @override
  Widget build(BuildContext context) {
    final stats = _stats;
    if (stats == null) return const SizedBox.shrink();
    return FutureBuilder<List<OperationStats>>(
      future: stats,
      builder: (context, snapshot) {
        if (snapshot.connectionState != ConnectionState.done) {
          return const Center(child: CircularProgressIndicator());
        }
        if (snapshot.hasError) {
          return MessageView(
            icon: Icons.cloud_off,
            message: snapshot.error.toString(),
            onRetry: () => setState(() => _stats = _load()),
          );
        }
        final list = snapshot.data!;
        if (list.isEmpty) {
          return const MessageView(icon: Icons.speed, message: 'No analyses since the server started');
        }
        final slowest = list.first.averageMillis;
        return RefreshIndicator(
          onRefresh: () async {
            final reload = _load();
            setState(() => _stats = reload);
            await reload;
          },
          child: ListView.separated(
            padding: const EdgeInsets.all(16),
            itemCount: list.length,
            separatorBuilder: (_, __) => const SizedBox(height: 14),
            itemBuilder: (context, index) {
              final s = list[index];
              return Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  Row(
                    children: [
                      Expanded(child: Text(s.operation, style: const TextStyle(fontWeight: FontWeight.w600))),
                      Text('${s.averageMillis.toStringAsFixed(1)} ms'),
                    ],
                  ),
                  const SizedBox(height: 4),
                  LinearProgressIndicator(value: slowest == 0 ? 0 : s.averageMillis / slowest, minHeight: 6),
                  const SizedBox(height: 4),
                  Text(
                    '${s.calls} calls · max ${s.maxMillis.toStringAsFixed(1)} ms'
                    '${s.failures > 0 ? ' · ${s.failures} failed' : ''}',
                    style: Theme.of(context).textTheme.bodySmall,
                  ),
                ],
              );
            },
          ),
        );
      },
    );
  }
}
