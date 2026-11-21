/// App settings, given at build time with --dart-define, e.g.
/// flutter run --dart-define=API_BASE_URL=http://192.168.1.20:8080 --dart-define=USE_MOCK_ANALYSIS=false
class AppConfig {
  AppConfig._();

  /// Spring Boot backend. 10.0.2.2 is the computer itself, seen from the Android emulator.
  static const String apiBaseUrl =
      String.fromEnvironment('API_BASE_URL', defaultValue: 'http://10.0.2.2:8080');

  /// true until POST /api/analyses exists on the backend: the analysis runs on the phone with a demo model,
  /// everything else (accounts, appointments, notifications) uses the real backend.
  static const bool useMockAnalysis =
      bool.fromEnvironment('USE_MOCK_ANALYSIS', defaultValue: true);

  static const Duration requestTimeout = Duration(seconds: 30);
}
