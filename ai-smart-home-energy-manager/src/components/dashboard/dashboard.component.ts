
import { Component, ChangeDetectionStrategy, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { SmartHomeService } from '../../services/smart-home.service';
import { DeviceCardComponent } from '../device-card/device-card.component';
import { EnergyChartComponent } from '../energy-chart/energy-chart.component';

@Component({
  selector: 'app-dashboard',
  templateUrl: './dashboard.component.html',
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [CommonModule, DeviceCardComponent, EnergyChartComponent]
})
export class DashboardComponent {
  smartHomeService = inject(SmartHomeService);

  devices = this.smartHomeService.devices;
  activeDevicesCount = this.smartHomeService.activeDevicesCount;
  totalPowerConsumption = this.smartHomeService.totalPowerConsumption;
  todayCost = this.smartHomeService.todayCost;
  recommendation = this.smartHomeService.recommendation;
  energyHistory = this.smartHomeService.energyHistory;

  onToggleDevice(deviceId: string): void {
    this.smartHomeService.toggleDevice(deviceId);
  }
}
