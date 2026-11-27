enum Role { patient, doctor, student, administrator }

Role roleFromJson(String value) {
  switch (value.toUpperCase()) {
    case 'DOCTOR':
      return Role.doctor;
    case 'STUDENT':
      return Role.student;
    case 'ADMINISTRATOR':
      return Role.administrator;
    default:
      return Role.patient;
  }
}

String roleToJson(Role role) => role.name.toUpperCase();

String roleLabel(Role role) {
  switch (role) {
    case Role.patient:
      return 'Patient';
    case Role.doctor:
      return 'Doctor';
    case Role.student:
      return 'Student';
    case Role.administrator:
      return 'Administrator';
  }
}

class User {
  final int id;
  final String name;
  final String email;
  final Role role;
  final bool verified;

  const User({
    required this.id,
    required this.name,
    required this.email,
    required this.role,
    required this.verified,
  });

  factory User.fromJson(Map<String, dynamic> json) => User(
        id: (json['id'] as num).toInt(),
        name: json['name'] as String? ?? '',
        email: json['email'] as String? ?? '',
        role: roleFromJson(json['role'] as String? ?? 'PATIENT'),
        verified: json['verified'] as bool? ?? false,
      );

  bool get isDoctor => role == Role.doctor;
  bool get isPatient => role == Role.patient;
  bool get isAdmin => role == Role.administrator;
}
