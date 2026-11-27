import 'package:flutter/material.dart';

import '../app_scope.dart';
import '../models/audit_entry.dart';
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
        title: Text(_tab == 0 ? 'Doctors to verify' : 'Account activity'),
        actions: [
          IconButton(tooltip: 'Log out', icon: const Icon(Icons.logout), onPressed: state.logout),
        ],
      ),
      body: IndexedStack(
        index: _tab,
        children: [
          _UnverifiedDoctors(visible: _tab == 0),
          _AuditLog(visible: _tab == 1),
        ],
      ),
      bottomNavigationBar: NavigationBar(
        selectedIndex: _tab,
        onDestinationSelected: (index) => setState(() => _tab = index),
        destinations: const [
          NavigationDestination(icon: Icon(Icons.verified_user_outlined), selectedIcon: Icon(Icons.verified_user), label: 'Doctors'),
          NavigationDestination(icon: Icon(Icons.receipt_long_outlined), selectedIcon: Icon(Icons.receipt_long), label: 'Audit'),
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
