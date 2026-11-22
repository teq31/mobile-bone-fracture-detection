import 'package:bone_fracture_app/models/analysis.dart';
import 'package:bone_fracture_app/models/app_notification.dart';
import 'package:bone_fracture_app/models/appointment.dart';
import 'package:bone_fracture_app/models/user.dart';
import 'package:bone_fracture_app/services/api_client.dart';
import 'package:flutter_test/flutter_test.dart';

// json examples copied from backend-api.md / api-contract.md
void main() {
  test('user from /api/auth/me', () {
    final user = User.fromJson({'id': 2, 'name': 'Dr. Smith', 'email': 'smith@clinic.com', 'role': 'DOCTOR', 'verified': false});

    expect(user.id, 2);
    expect(user.role, Role.doctor);
    expect(user.isDoctor, isTrue);
    expect(user.verified, isFalse);
  });

  test('analysis from POST /api/analyses', () {
    final analysis = Analysis.fromJson({
      'analysisId': 42,
      'model': 'YOLO',
      'detections': [
        {
          'label': 'fracture',
          'confidence': 0.87,
          'boundingBox': {'x': 310, 'y': 520, 'width': 140, 'height': 95},
          'heatmapUrl': '/api/analyses/42/heatmap/0'
        },
        {
          'label': 'fracture',
          'confidence': 0.4,
          'boundingBox': {'x': 10.5, 'y': 20, 'width': 30, 'height': 40}
        }
      ],
      'imageUrl': '/api/analyses/42/image',
      'createdAt': '2026-11-20T10:15:00Z'
    });

    expect(analysis.id, '42');
    expect(analysis.detections, hasLength(2));
    expect(analysis.detections.first.box.width, 140);
    expect(analysis.detections.last.box.x, 10.5);
    expect(analysis.hasFracture, isTrue);
    expect(analysis.highestConfidence, 0.87);
    expect(analysis.createdAt.toUtc(), DateTime.utc(2026, 11, 20, 10, 15));
  });

  test('analysis without detections', () {
    final analysis = Analysis.fromJson({'analysisId': 'a1', 'model': 'YOLO', 'detections': [], 'createdAt': ''});

    expect(analysis.hasFracture, isFalse);
    expect(analysis.highestConfidence, 0);
  });

  test('comparison for a doctor', () {
    final comparison = Comparison.fromJson({
      'analysisId': '42',
      'totalModels': 2,
      'findings': [
        {
          'label': 'fracture',
          'confidence': 0.81,
          'boundingBox': {'x': 0, 'y': 0, 'width': 10, 'height': 10},
          'agreementLevel': 'FULL',
          'agreementCount': 2,
          'models': ['YOLO', 'RF-DETR']
        }
      ]
    });

    expect(comparison.totalModels, 2);
    expect(comparison.findings.single.models, ['YOLO', 'RF-DETR']);
  });

  test('appointment and notification', () {
    final appointment = Appointment.fromJson({
      'id': 3,
      'doctorId': 2,
      'doctorName': 'Dr. Smith',
      'patientId': 1,
      'patientName': 'John Doe',
      'dateTime': '2026-12-01T10:00:00',
      'status': 'CONFIRMED'
    });
    final notification = AppNotification.fromJson({
      'id': 5,
      'type': 'APPOINTMENT_CONFIRMED',
      'message': 'Your appointment on 2026-12-01T10:00 was confirmed',
      'createdAt': '2026-11-20T17:05:12.345',
      'read': false
    });

    expect(appointment.status, AppointmentStatus.confirmed);
    expect(appointment.dateTime, DateTime(2026, 12, 1, 10));
    expect(notification.read, isFalse);
  });

  test('error message from a ProblemDetail body', () {
    expect(
      ApiClient.errorMessage(409, '{"title":"Conflict","status":409,"detail":"Appointment is already confirmed"}'),
      'Appointment is already confirmed',
    );
    expect(ApiClient.errorMessage(401, ''), 'Your session expired, please log in again.');
  });
}
