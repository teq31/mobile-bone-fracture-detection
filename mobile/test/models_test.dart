import 'dart:typed_data';

import 'package:bone_fracture_app/models/analysis.dart';
import 'package:bone_fracture_app/models/app_notification.dart';
import 'package:bone_fracture_app/models/appointment.dart';
import 'package:bone_fracture_app/models/doctor.dart';
import 'package:bone_fracture_app/models/user.dart';
import 'package:bone_fracture_app/services/analysis_service.dart';
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

  test('comparison for a doctor (the server does not send the id back)', () {
    final comparison = Comparison.fromJson({
      'totalModels': 2,
      'findings': [
        {
          'label': 'fracture',
          'confidence': 0.81,
          'boundingBox': {'x': 0, 'y': 0, 'width': 10, 'height': 10},
          'agreementLevel': 'FULL',
          'agreementCount': 2,
          'models': ['YOLO', 'RF-DETR']
        },
        {
          'label': 'fracture',
          'confidence': 0.4,
          'boundingBox': null,
          'agreementLevel': 'NONE',
          'agreementCount': 1,
          'models': ['YOLO']
        }
      ]
    }, analysisId: '42');

    expect(comparison.analysisId, '42');
    expect(comparison.totalModels, 2);
    expect(comparison.findings.last.box, isNull);
    expect(comparison.findings.first.models, ['YOLO', 'RF-DETR']);
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

  test('analysis exactly as the backend sends it (numeric id, local time)', () {
    final analysis = Analysis.fromJson({
      'analysisId': 7,
      'model': 'YOLO',
      'detections': [
        {
          'label': 'fracture',
          'confidence': 0.91,
          'boundingBox': {'x': 412, 'y': 880, 'width': 96, 'height': 74}
        }
      ],
      'imageUrl': '/api/analyses/7/image',
      'createdAt': '2026-11-24T10:15:30.123456'
    });

    expect(analysis.id, '7');
    expect(analysis.imageUrl, '/api/analyses/7/image');
    expect(analysis.createdAt, DateTime(2026, 11, 24, 10, 15, 30, 123, 456));
  });

  test('only png and jpg can be drawn', () {
    expect(isDisplayableImage(Uint8List.fromList([0x89, 0x50, 0x4E, 0x47, 0x0D])), isTrue);
    expect(isDisplayableImage(Uint8List.fromList([0xFF, 0xD8, 0xFF, 0xE0])), isTrue);
    expect(isDisplayableImage(Uint8List.fromList([0x25, 0x50, 0x44, 0x46])), isFalse); // %PDF
    expect(isDisplayableImage(Uint8List(0)), isFalse);
  });

  test('doctor from GET /api/doctors', () {
    final doctor = Doctor.fromJson({'id': 2, 'name': 'Dr. Smith', 'email': 'smith@clinic.com'});

    expect(doctor.id, 2);
    expect(doctor.name, 'Dr. Smith');
  });

  test('error message from a ProblemDetail body', () {
    expect(
      ApiClient.errorMessage(409, '{"title":"Conflict","status":409,"detail":"Appointment is already confirmed"}'),
      'Appointment is already confirmed',
    );
    expect(ApiClient.errorMessage(401, ''), 'Your session expired, please log in again.');
  });
}
