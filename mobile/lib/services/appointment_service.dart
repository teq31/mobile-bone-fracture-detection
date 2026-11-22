import '../models/appointment.dart';
import 'api_client.dart';

// the server decides what's allowed (State), we just show the 409 message
class AppointmentService {
  final ApiClient _api;

  AppointmentService(this._api);

  Future<List<Appointment>> list() async {
    final json = await _api.get('/api/appointments') as List<dynamic>;
    return json.map((a) => Appointment.fromJson(a as Map<String, dynamic>)).toList();
  }

  Future<Appointment> request(int doctorId, DateTime dateTime) async => Appointment.fromJson(
      await _api.post('/api/appointments', {'doctorId': doctorId, 'dateTime': _localIso(dateTime)})
          as Map<String, dynamic>);

  Future<Appointment> confirm(int id) => _action(id, 'confirm');

  Future<Appointment> cancel(int id) => _action(id, 'cancel');

  Future<Appointment> sendReminder(int id) => _action(id, 'reminder');

  Future<Appointment> _action(int id, String action) async =>
      Appointment.fromJson(await _api.post('/api/appointments/$id/$action') as Map<String, dynamic>);

  // backend wants local time without zone, e.g. 2026-12-01T10:00:00
  static String _localIso(DateTime value) => value.toIso8601String().split('.').first;
}
