import 'package:flutter/material.dart';
import 'package:provider/provider.dart';
import '../services/auth_provider.dart';
import '../services/device_provider.dart';
import '../models/device.dart';

class DashboardScreen extends StatefulWidget {
  const DashboardScreen({super.key});

  @override
  State<DashboardScreen> createState() => _DashboardScreenState();
}

class _DashboardScreenState extends State<DashboardScreen> {
  @override
  void initState() {
    super.initState();
    final token = Provider.of<AuthProvider>(context, listen: false).token;
    if (token != null) {
      Provider.of<DeviceProvider>(context, listen: false).fetchDevices(token);
    }
  }

  @override
  Widget build(BuildContext context) {
    final devices = Provider.of<DeviceProvider>(context).devices;
    final token = Provider.of<AuthProvider>(context, listen: false).token;

    return Scaffold(
      backgroundColor: const Color(0xFFF2F2F7),
      body: SafeArea(
        child: Padding(
          padding: const EdgeInsets.all(24.0),
          child: Column(
            crossAxisAlignment: CrossAxisAlignment.start,
            children: [
              const Text(
                'My Home',
                style: TextStyle(fontSize: 34, fontWeight: FontWeight.bold),
              ),
              const SizedBox(height: 4),
              Text(
                'Active Devices: ${devices.length} · 24°C',
                style: const TextStyle(fontSize: 17, color: Colors.grey),
              ),
              const SizedBox(height: 24),
              Expanded(
                child: GridView.builder(
                  gridDelegate: const SliverGridDelegateWithFixedCrossAxisCount(
                    crossAxisCount: 2,
                    crossAxisSpacing: 16,
                    mainAxisSpacing: 16,
                    childAspectRatio: 1.0,
                  ),
                  itemCount: devices.length,
                  itemBuilder: (context, index) {
                    final device = devices[index];
                    return _buildDeviceCard(context, device, token!);
                  },
                ),
              ),
            ],
          ),
        ),
      ),
    );
  }

  Widget _buildDeviceCard(BuildContext context, Device device, String token) {
    final isOn = device.status;
    return GestureDetector(
      onTap: () => Provider.of<DeviceProvider>(context, listen: false).toggleDevice(token, device.id),
      child: Container(
        padding: const EdgeInsets.all(16),
        decoration: BoxDecoration(
          color: Colors.white,
          borderRadius: BorderRadius.circular(20),
        ),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          mainAxisAlignment: MainAxisAlignment.spaceBetween,
          children: [
            Container(
              padding: const EdgeInsets.all(8),
              decoration: BoxDecoration(
                shape: BoxShape.circle,
                // Using a shadow/glow effect for 'ON' state
                boxShadow: isOn
                    ? [BoxShadow(color: Colors.yellow.withOpacity(0.5), blurRadius: 10)]
                    : [],
              ),
              child: Icon(
                _getIconForType(device.type),
                color: isOn ? Colors.amber : Colors.grey,
                size: 32,
              ),
            ),
            Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                Text(
                  device.name,
                  style: const TextStyle(fontSize: 16, fontWeight: FontWeight.bold),
                ),
                Text(
                  isOn ? 'On' : 'Off',
                  style: TextStyle(
                    fontSize: 14,
                    color: isOn ? Colors.amber : Colors.grey,
                    fontWeight: FontWeight.w500,
                  ),
                ),
              ],
            ),
          ],
        ),
      ),
    );
  }

  IconData _getIconForType(String type) {
    switch (type.toUpperCase()) {
      case 'LIGHT': return Icons.lightbulb;
      case 'FAN': return Icons.wind_power; // Requires newer material icons, fallback to ac_unit
      case 'AC': return Icons.ac_unit;
      default: return Icons.devices;
    }
  }
}
