// set with --dart-define, e.g. --dart-define=API_BASE_URL=http://192.168.1.20:8080
class AppConfig {
  AppConfig._();

  // 10.0.2.2 = your pc, seen from the android emulator
  static const String apiBaseUrl =
      String.fromEnvironment('API_BASE_URL', defaultValue: 'http://10.0.2.2:8080');

  // switch to false once POST /api/analyses works
  static const bool useMockAnalysis =
      bool.fromEnvironment('USE_MOCK_ANALYSIS', defaultValue: true);

  static const Duration requestTimeout = Duration(seconds: 30);
}
