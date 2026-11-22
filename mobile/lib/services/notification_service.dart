import '../models/app_notification.dart';
import 'api_client.dart';

class NotificationService {
  final ApiClient _api;

  NotificationService(this._api);

  Future<List<AppNotification>> list() async {
    final json = await _api.get('/api/notifications') as List<dynamic>;
    return json.map((n) => AppNotification.fromJson(n as Map<String, dynamic>)).toList();
  }

  Future<AppNotification> markRead(int id) async =>
      AppNotification.fromJson(await _api.post('/api/notifications/$id/read') as Map<String, dynamic>);
}
