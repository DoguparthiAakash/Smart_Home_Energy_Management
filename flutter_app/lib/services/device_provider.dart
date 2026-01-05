import 'dart:convert';
import 'package:flutter/material.dart';
import 'package:http/http.dart' as http;
import '../models/device.dart';

class DeviceProvider with ChangeNotifier {
  List<Device> _devices = [];
  List<Device> get devices => _devices;

  final String baseUrl = 'http://localhost:8080/api/devices';

  Future<void> fetchDevices(String token) async {
    try {
      final response = await http.get(
        Uri.parse(baseUrl),
        headers: {'Authorization': 'Bearer $token'},
      );

      if (response.statusCode == 200) {
        final List<dynamic> data = jsonDecode(response.body);
        _devices = data.map((json) => Device.fromJson(json)).toList();
        notifyListeners();
      }
    } catch (e) {
      print(e);
    }
  }

  Future<void> toggleDevice(String token, int id) async {
    try {
      await http.put(
        Uri.parse('$baseUrl/$id/status'),
        headers: {'Authorization': 'Bearer $token'},
      );
      await fetchDevices(token); // Refresh list
    } catch (e) {
      print(e);
    }
  }
}
