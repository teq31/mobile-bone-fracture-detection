import 'dart:convert';
import 'dart:typed_data';

import 'package:http/http.dart' as http;

import '../config.dart';

/// An error returned by the backend (Spring ProblemDetail: title, status, detail) or a network error.
class ApiException implements Exception {
  final int? status;
  final String message;

  const ApiException(this.message, {this.status});

  bool get isUnauthorized => status == 401;

  @override
  String toString() => message;
}

/// Small HTTP client for the Spring Boot backend: JSON bodies, the JWT in every request, readable errors.
class ApiClient {
  final String baseUrl;
  final http.Client _http;
  String? token;

  ApiClient({String? baseUrl, http.Client? httpClient})
      : baseUrl = baseUrl ?? AppConfig.apiBaseUrl,
        _http = httpClient ?? http.Client();

  Uri uri(String path) => Uri.parse(path.startsWith('http') ? path : '$baseUrl$path');

  Map<String, String> get _headers => {
        'Accept': 'application/json',
        if (token != null) 'Authorization': 'Bearer $token',
      };

  Future<dynamic> get(String path) => _send(() => _http.get(uri(path), headers: _headers));

  Future<dynamic> post(String path, [Object? body]) => _send(() => _http.post(
        uri(path),
        headers: {..._headers, 'Content-Type': 'application/json'},
        body: body == null ? null : jsonEncode(body),
      ));

  /// multipart/form-data upload, e.g. the X-ray for POST /api/analyses
  Future<dynamic> upload(String path,
      {required String field,
      required Uint8List bytes,
      required String fileName,
      Map<String, String> fields = const {}}) {
    return _send(() async {
      final request = http.MultipartRequest('POST', uri(path))
        ..headers.addAll(_headers)
        ..fields.addAll(fields)
        ..files.add(http.MultipartFile.fromBytes(field, bytes, filename: fileName));
      return http.Response.fromStream(await _http.send(request));
    });
  }

  /// Raw bytes, e.g. an image behind the JWT (Image.network cannot add the header on every platform).
  Future<Uint8List> getBytes(String path) async {
    final response = await _call(() => _http.get(uri(path), headers: _headers));
    _throwIfError(response);
    return response.bodyBytes;
  }

  Future<dynamic> _send(Future<http.Response> Function() request) async {
    final response = await _call(request);
    _throwIfError(response);
    if (response.body.isEmpty) {
      return null;
    }
    return jsonDecode(utf8.decode(response.bodyBytes));
  }

  Future<http.Response> _call(Future<http.Response> Function() request) async {
    try {
      return await request().timeout(AppConfig.requestTimeout);
    } on ApiException {
      rethrow;
    } catch (_) {
      throw ApiException('Cannot reach the server at $baseUrl. Is the backend running?');
    }
  }

  void _throwIfError(http.Response response) {
    if (response.statusCode >= 200 && response.statusCode < 300) {
      return;
    }
    throw ApiException(errorMessage(response.statusCode, response.body), status: response.statusCode);
  }

  /// "detail" from a ProblemDetail body, otherwise a message from the status code.
  static String errorMessage(int status, String body) {
    try {
      final json = jsonDecode(body);
      if (json is Map<String, dynamic>) {
        final detail = json['detail'] ?? json['message'] ?? json['title'];
        if (detail is String && detail.isNotEmpty) {
          return detail;
        }
      }
    } catch (_) {
      // not JSON
    }
    switch (status) {
      case 400:
        return 'The data is not valid.';
      case 401:
        return 'Your session expired, please log in again.';
      case 403:
        return 'You are not allowed to do this.';
      case 404:
        return 'Not found.';
      case 409:
        return 'This is not possible right now.';
      case 413:
        return 'The file is too large.';
      default:
        return 'Server error ($status).';
    }
  }
}
