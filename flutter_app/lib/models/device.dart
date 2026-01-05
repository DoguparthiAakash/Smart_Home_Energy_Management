class Device {
  final int id;
  final String name;
  final String type;
  final bool status;

  Device({required this.id, required this.name, required this.type, required this.status});

  factory Device.fromJson(Map<String, dynamic> json) {
    return Device(
      id: json['id'],
      name: json['name'],
      type: json['type'],
      status: json['status'],
    );
  }
}
