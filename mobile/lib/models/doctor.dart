// GET /api/doctors - only verified doctors are listed
class Doctor {
  final int id;
  final String name;
  final String email;

  const Doctor({required this.id, required this.name, required this.email});

  factory Doctor.fromJson(Map<String, dynamic> json) => Doctor(
        id: (json['id'] as num).toInt(),
        name: json['name'] as String? ?? '',
        email: json['email'] as String? ?? '',
      );
}
