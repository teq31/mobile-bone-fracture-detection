import 'package:flutter/material.dart';

import '../app_scope.dart';
import '../models/appointment.dart';
import '../widgets/common.dart';

class AppointmentsScreen extends StatefulWidget {
  final bool visible;

  const AppointmentsScreen({super.key, required this.visible});

  @override
  State<AppointmentsScreen> createState() => _AppointmentsScreenState();
}

class _AppointmentsScreenState extends State<AppointmentsScreen> {
  Future<List<Appointment>>? _items;

  @override
  void didChangeDependencies() {
    super.didChangeDependencies();
    if (widget.visible) _items ??= _load();
  }

  @override
  void didUpdateWidget(AppointmentsScreen oldWidget) {
    super.didUpdateWidget(oldWidget);
    if (widget.visible && !oldWidget.visible) {
      setState(() => _items = _load());
    }
  }

  Future<List<Appointment>> _load() => AppScope.of(context).appointments.list();

  void _reload() => setState(() => _items = _load());

  Future<void> _run(Future<Appointment> Function() action, String done) async {
    try {
      await action();
      if (!mounted) return;
      ScaffoldMessenger.of(context).showSnackBar(SnackBar(content: Text(done)));
      _reload();
    } catch (e) {
      if (mounted) showError(context, e);
    }
  }

  @override
  Widget build(BuildContext context) {
    final state = AppScope.of(context);
    final user = state.user!;
    final items = _items;
    return Scaffold(
      floatingActionButton: user.isPatient
          ? FloatingActionButton.extended(
              onPressed: () async {
                if (await showRequestAppointmentSheet(context)) _reload();
              },
              icon: const Icon(Icons.add),
              label: const Text('Request'),
            )
          : null,
      body: items == null
          ? const SizedBox.shrink()
          : FutureBuilder<List<Appointment>>(
              future: items,
              builder: (context, snapshot) {
                if (snapshot.connectionState != ConnectionState.done) {
                  return const Center(child: CircularProgressIndicator());
                }
                if (snapshot.hasError) {
                  return MessageView(icon: Icons.cloud_off, message: snapshot.error.toString(), onRetry: _reload);
                }
                final list = snapshot.data!;
                if (list.isEmpty) {
                  return MessageView(
                    icon: Icons.event_note,
                    message: user.isPatient
                        ? 'No appointments yet. Tap "Request" to ask a doctor for one.'
                        : 'No appointments yet.',
                  );
                }
                return RefreshIndicator(
                  onRefresh: () async {
                    final reload = _load();
                    setState(() => _items = reload);
                    await reload;
                  },
                  child: ListView.builder(
                    padding: const EdgeInsets.fromLTRB(12, 12, 12, 88),
                    itemCount: list.length,
                    itemBuilder: (context, index) => _AppointmentCard(
                      appointment: list[index],
                      isDoctor: user.isDoctor,
                      onConfirm: (a) => _run(() => state.appointments.confirm(a.id), 'Appointment confirmed'),
                      onCancel: (a) => _run(() => state.appointments.cancel(a.id), 'Appointment cancelled'),
                      onRemind: (a) => _run(() => state.appointments.sendReminder(a.id), 'Reminder sent'),
                    ),
                  ),
                );
              },
            ),
    );
  }
}

class _AppointmentCard extends StatelessWidget {
  final Appointment appointment;
  final bool isDoctor;
  final void Function(Appointment) onConfirm;
  final void Function(Appointment) onCancel;
  final void Function(Appointment) onRemind;

  const _AppointmentCard({
    required this.appointment,
    required this.isDoctor,
    required this.onConfirm,
    required this.onCancel,
    required this.onRemind,
  });

  @override
  Widget build(BuildContext context) {
    final status = appointment.status;
    final (label, color) = switch (status) {
      AppointmentStatus.requested => ('Requested', Colors.orange),
      AppointmentStatus.confirmed => ('Confirmed', Colors.green),
      AppointmentStatus.cancelled => ('Cancelled', Colors.grey),
    };
    return Card(
      child: Padding(
        padding: const EdgeInsets.fromLTRB(16, 12, 8, 4),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            Row(
              children: [
                Expanded(
                  child: Text(
                    isDoctor ? appointment.patientName : appointment.doctorName,
                    style: Theme.of(context).textTheme.titleMedium,
                  ),
                ),
                Chip(
                  label: Text(label),
                  side: BorderSide(color: color),
                  labelStyle: TextStyle(color: color),
                  visualDensity: VisualDensity.compact,
                ),
              ],
            ),
            Text(formatDateTime(appointment.dateTime)),
            Row(
              mainAxisAlignment: MainAxisAlignment.end,
              children: [
                if (isDoctor && status == AppointmentStatus.requested)
                  TextButton(onPressed: () => onConfirm(appointment), child: const Text('Confirm')),
                if (isDoctor && status == AppointmentStatus.confirmed)
                  TextButton(onPressed: () => onRemind(appointment), child: const Text('Send reminder')),
                if (status != AppointmentStatus.cancelled)
                  TextButton(onPressed: () => onCancel(appointment), child: const Text('Cancel')),
              ],
            ),
          ],
        ),
      ),
    );
  }
}

// returns true if an appointment was created
Future<bool> showRequestAppointmentSheet(BuildContext context) async {
  final created = await showModalBottomSheet<bool>(
    context: context,
    isScrollControlled: true,
    builder: (_) => const _RequestAppointmentSheet(),
  );
  return created ?? false;
}

class _RequestAppointmentSheet extends StatefulWidget {
  const _RequestAppointmentSheet();

  @override
  State<_RequestAppointmentSheet> createState() => _RequestAppointmentSheetState();
}

class _RequestAppointmentSheetState extends State<_RequestAppointmentSheet> {
  final _doctorId = TextEditingController();
  DateTime _dateTime = DateTime.now().add(const Duration(days: 1)).copyWith(hour: 10, minute: 0, second: 0, millisecond: 0, microsecond: 0);
  bool _busy = false;

  @override
  void dispose() {
    _doctorId.dispose();
    super.dispose();
  }

  Future<void> _pickDateTime() async {
    final now = DateTime.now();
    final date = await showDatePicker(
      context: context,
      initialDate: _dateTime,
      firstDate: now,
      lastDate: now.add(const Duration(days: 365)),
    );
    if (date == null || !mounted) return;
    final time = await showTimePicker(context: context, initialTime: TimeOfDay.fromDateTime(_dateTime));
    if (time == null) return;
    setState(() => _dateTime = DateTime(date.year, date.month, date.day, time.hour, time.minute));
  }

  Future<void> _submit() async {
    final doctorId = int.tryParse(_doctorId.text.trim());
    if (doctorId == null) {
      showError(context, 'Enter the doctor ID');
      return;
    }
    setState(() => _busy = true);
    try {
      await AppScope.of(context).appointments.request(doctorId, _dateTime);
      if (mounted) Navigator.of(context).pop(true);
    } catch (e) {
      if (mounted) {
        setState(() => _busy = false);
        showError(context, e);
      }
    }
  }

  @override
  Widget build(BuildContext context) {
    return Padding(
      padding: EdgeInsets.fromLTRB(24, 24, 24, 24 + MediaQuery.of(context).viewInsets.bottom),
      child: Column(
        mainAxisSize: MainAxisSize.min,
        crossAxisAlignment: CrossAxisAlignment.stretch,
        children: [
          Text('Request an appointment', style: Theme.of(context).textTheme.titleLarge),
          const SizedBox(height: 16),
          TextField(
            controller: _doctorId,
            keyboardType: TextInputType.number,
            decoration: const InputDecoration(labelText: 'Doctor ID', helperText: 'Ask your doctor for the ID'),
          ),
          const SizedBox(height: 12),
          OutlinedButton.icon(
            onPressed: _busy ? null : _pickDateTime,
            icon: const Icon(Icons.schedule),
            label: Text(formatDateTime(_dateTime)),
          ),
          const SizedBox(height: 20),
          FilledButton(
            onPressed: _busy ? null : _submit,
            child: _busy
                ? const SizedBox(height: 20, width: 20, child: CircularProgressIndicator(strokeWidth: 2))
                : const Text('Send request'),
          ),
        ],
      ),
    );
  }
}
