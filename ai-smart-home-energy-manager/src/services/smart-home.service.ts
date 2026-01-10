
import { Injectable, signal, computed, effect } from '@angular/core';
import { Device, EnergyDataPoint } from '../models/device.model';

@Injectable({
  providedIn: 'root',
})
export class SmartHomeService {
  private readonly initialDevices: Device[] = [
    { id: '1', name: 'Living Room Lamp', type: 'light', location: 'Living Room', isOn: true, powerConsumption: 60, icon: 'lightbulb' },
    { id: '2', name: 'Main Thermostat', type: 'thermostat', location: 'Hallway', isOn: true, powerConsumption: 1500, icon: 'thermometer' },
    { id: '3', name: 'TV Smart Plug', type: 'smart-plug', location: 'Living Room', isOn: false, powerConsumption: 120, icon: 'bolt' },
    { id: '4', name: 'Kitchen Lights', type: 'light', location: 'Kitchen', isOn: true, powerConsumption: 75, icon: 'lightbulb' },
    { id: '5', name: 'Refrigerator', type: 'appliance', location: 'Kitchen', isOn: true, powerConsumption: 200, icon: 'server' },
    { id: '6', name: 'Bedroom AC', type: 'thermostat', location: 'Bedroom', isOn: false, powerConsumption: 2500, icon: 'thermometer' },
  ];

  devices = signal<Device[]>(this.initialDevices);

  activeDevicesCount = computed(() => this.devices().filter(d => d.isOn).length);
  totalPowerConsumption = computed(() => {
    return this.devices()
      .filter(d => d.isOn)
      .reduce((sum, device) => sum + device.powerConsumption, 0);
  });
  
  todayCost = computed(() => {
    // Assuming ₹10 per kWh and the current consumption rate for an hour
    const powerInKW = this.totalPowerConsumption() / 1000;
    const costPerHour = powerInKW * 10;
    // Simulate cost over a few hours for a more dynamic number
    return (costPerHour * this.hoursElapsed()); 
  });

  private hoursElapsed = signal(1);

  // Historical data for charts
  energyHistory = signal<EnergyDataPoint[]>(this.generateInitialHistory());

  // AI Recommendation
  recommendation = signal<string>('Turn off Bedroom AC during the day to save up to ₹1200/month.');

  constructor() {
    // Simulate real-time power fluctuation
    setInterval(() => {
      this.devices.update(devices =>
        devices.map(device => {
          if (device.isOn) {
            // Fluctuate power by +/- 5%
            const fluctuation = (Math.random() - 0.5) * 0.1 * device.powerConsumption;
            return { ...device, powerConsumption: Math.max(0, Math.round(device.powerConsumption + fluctuation)) };
          }
          return device;
        })
      );
      
      this.hoursElapsed.update(h => h + 1/3600); // Increment hours every second for simulation

      // Add new data point to history
      const newPoint = {
          time: Date.now(),
          usage: this.totalPowerConsumption() / 1000, // convert W to kW
      };
      this.energyHistory.update(history => [...history.slice(-59), newPoint]);

    }, 3000);

    // Simulate new recommendations
    setInterval(() => {
        const recommendations = [
            'Your TV Smart Plug is using 5W on standby. Consider turning it off completely.',
            'Shift AC usage to after 9 PM to save 12% on your bill.',
            'Living Room lights have been on for 8 hours. Automate them to turn off when not in use.',
            'Overall usage is 10% lower than yesterday. Keep it up!'
        ];
        const randomIndex = Math.floor(Math.random() * recommendations.length);
        this.recommendation.set(recommendations[randomIndex]);
    }, 15000);
  }

  toggleDevice(deviceId: string): void {
    this.devices.update(devices =>
      devices.map(device =>
        device.id === deviceId ? { ...device, isOn: !device.isOn } : device
      )
    );
  }

  private generateInitialHistory(): EnergyDataPoint[] {
      const now = Date.now();
      const data: EnergyDataPoint[] = [];
      for (let i = 60; i > 0; i--) {
          data.push({
              time: now - i * 3000,
              usage: 1.5 + Math.random() * 1.5, // Random usage between 1.5 and 3.0 kWh
          });
      }
      return data;
  }
}
