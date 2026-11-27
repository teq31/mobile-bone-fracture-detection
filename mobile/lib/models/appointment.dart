enum AppointmentStatus { requested, confirmed, cancelled }

AppointmentStatus statusFromJson(String value) {
  switch (value.toUpperCase()) {
    case 'CONFIRMED':
      return AppointmentStatus.confirmed;
    case 'CANCELLED':
      return AppointmentStatus.cancelled;
    default:
      return AppointmentStatus.requested;
  }
}

class Appointment {
  final int id;
  final int doctorId;
  final String doctorName;
  final int patientId;
  final String patientName;
  final DateTime dateTime;
  final AppointmentStatus status;

  const Appointment({
    required this.id,
    required this.doctorId,
    required this.doctorName,
    required this.patientId,
    required this.patientName,
    required this.dateTime,
    required this.status,
  });

  factory Appointment.fromJson(Map<String, dynamic> json) => Appointment(
        id: (json['id'] as num).toInt(),
        doctorId: (json['doctorId'] as num).toInt(),
        doctorName: json['doctorName'] as String? ?? '',
        patientId: (json['patientId'] as num).toInt(),
        patientName: json['patientName'] as String? ?? '',
        dateTime: DateTime.parse(json['dateTime'] as String),
        status: statusFromJson(json['status'] as String? ?? 'REQUESTED'),
      );
}
