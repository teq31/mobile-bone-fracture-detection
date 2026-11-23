import 'package:shared_preferences/shared_preferences.dart';

import '../models/user.dart';
import 'api_client.dart';

/// Accounts: POST /api/auth/register, POST /api/auth/login, GET /api/auth/me.
/// The JWT is kept on the phone, so the user stays logged in after the app is closed.
class AuthService {
  static const _tokenKey = 'jwt';

  final ApiClient _api;

  AuthService(this._api);

  Future<User> login(String email, String password) async {
    final json = await _api.post('/api/auth/login', {'email': email.trim(), 'password': password});
    _api.token = (json as Map<String, dynamic>)['token'] as String;
    final user = await me();
    await _saveToken(_api.token);
    return user;
  }

  Future<User> register(String name, String email, String password, Role role) async {
    await _api.post('/api/auth/register', {
      'name': name.trim(),
      'email': email.trim(),
      'password': password,
      'role': roleToJson(role),
    });
    return login(email, password);
  }

  Future<User> me() async => User.fromJson(await _api.get('/api/auth/me') as Map<String, dynamic>);

  /// The user from the saved token, or null if there is none or it expired.
  Future<User?> restoreSession() async {
    final prefs = await SharedPreferences.getInstance();
    final saved = prefs.getString(_tokenKey);
    if (saved == null) {
      return null;
    }
    _api.token = saved;
    try {
      return await me();
    } on ApiException catch (e) {
      if (e.isUnauthorized) {
        await logout();
      }
      return null;
    }
  }

  Future<void> logout() async {
    _api.token = null;
    await _saveToken(null);
  }

  Future<void> _saveToken(String? token) async {
    final prefs = await SharedPreferences.getInstance();
    if (token == null) {
      await prefs.remove(_tokenKey);
    } else {
      await prefs.setString(_tokenKey, token);
    }
  }
}
