
export type DeviceType = 'light' | 'thermostat' | 'smart-plug' | 'appliance';

export interface Device {
  id: string;
  name: string;
  type: DeviceType;
  location: string;
  isOn: boolean;
  powerConsumption: number; // in Watts
  icon: 'lightbulb' | 'thermometer' | 'bolt' | 'server';
}

export interface EnergyDataPoint {
    time: number; // timestamp
    usage: number; // in kWh
}
