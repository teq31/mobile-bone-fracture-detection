import '../models/audit_entry.dart';
import '../models/operation_stats.dart';
import '../models/user.dart';
import 'api_client.dart';

// /api/admin/* - the server only lets administrators in (@RequiresRole)
class AdminService {
  final ApiClient _api;

  AdminService(this._api);

  Future<List<User>> unverifiedDoctors() async {
    final json = await _api.get('/api/admin/doctors/unverified') as List<dynamic>;
    return json.map((u) => User.fromJson(u as Map<String, dynamic>)).toList();
  }

  Future<User> verifyDoctor(String email) async =>
      User.fromJson(await _api.post('/api/admin/doctors/verify', {'email': email}) as Map<String, dynamic>);

  Future<List<OperationStats>> performance() async {
    final json = await _api.get('/api/performance/stats') as List<dynamic>;
    return json.map((s) => OperationStats.fromJson(s as Map<String, dynamic>)).toList();
  }

  Future<List<AuditEntry>> audit() async {
    final json = await _api.get('/api/admin/audit') as List<dynamic>;
    return json.map((e) => AuditEntry.fromJson(e as Map<String, dynamic>)).toList();
  }
}
