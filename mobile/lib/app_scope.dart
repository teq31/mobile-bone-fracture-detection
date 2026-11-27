import 'package:flutter/widgets.dart';

import 'config.dart';
import 'models/user.dart';
import 'services/admin_service.dart';
import 'services/analysis_service.dart';
import 'services/api_client.dart';
import 'services/appointment_service.dart';
import 'services/auth_service.dart';
import 'services/notification_service.dart';

class AppState extends ChangeNotifier {
  final ApiClient api;
  late final AuthService auth = AuthService(api);
  late final AnalysisService analysis =
      AppConfig.useMockAnalysis ? MockAnalysisService() : HttpAnalysisService(api);
  late final AppointmentService appointments = AppointmentService(api);
  late final NotificationService notifications = NotificationService(api);
  late final AdminService admin = AdminService(api);

  User? _user;

  AppState({ApiClient? api}) : api = api ?? ApiClient();

  User? get user => _user;

  set user(User? value) {
    _user = value;
    notifyListeners();
  }

  Future<void> logout() async {
    await auth.logout();
    user = null;
  }
}

class AppScope extends InheritedNotifier<AppState> {
  const AppScope({super.key, required AppState state, required super.child}) : super(notifier: state);

  static AppState of(BuildContext context) =>
      context.dependOnInheritedWidgetOfExactType<AppScope>()!.notifier!;
}
