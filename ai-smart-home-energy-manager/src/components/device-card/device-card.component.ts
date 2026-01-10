
import { Component, ChangeDetectionStrategy, input, output } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Device } from '../../models/device.model';

@Component({
  selector: 'app-device-card',
  templateUrl: './device-card.component.html',
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [CommonModule]
})
export class DeviceCardComponent {
  device = input.required<Device>();
  toggle = output<string>();

  onToggle(): void {
    this.toggle.emit(this.device().id);
  }

  getIconPath(icon: Device['icon']): string {
    switch (icon) {
      case 'lightbulb':
        return 'M9.049 2.927c.3-.921 1.603-.921 1.902 0l1.07 3.292a1 1 0 00.95.69h3.462c.969 0 1.371 1.24.588 1.81l-2.8 2.034a1 1 0 00-.364 1.118l1.07 3.292c.3.921-.755 1.688-1.54 1.118l-2.8-2.034a1 1 0 00-1.175 0l-2.8 2.034c-.784.57-1.838-.197-1.539-1.118l1.07-3.292a1 1 0 00-.364-1.118L2.98 8.72c-.783-.57-.38-1.81.588-1.81h3.461a1 1 0 00.951-.69l1.07-3.292z';
      case 'thermometer':
        return 'M9 12.75a.75.75 0 100-1.5.75.75 0 000 1.5z M9 8.25a.75.75 0 100-1.5.75.75 0 000 1.5z M9 17.25a.75.75 0 100-1.5.75.75 0 000 1.5z M12.75 4.5v11.5a3 3 0 01-3 3h-1.5a3 3 0 01-3-3V4.5a3 3 0 013-3h1.5a3 3 0 013 3z';
      case 'bolt':
        return 'M3.75 13.5l10.5-11.25L12 10.5h8.25L9.75 21.75 12 13.5H3.75z';
      case 'server':
        return 'M5.25 14.25v-2.625a3.375 3.375 0 00-3.375-3.375h-1.5A1.125 1.125 0 010 7.125v-1.5A3.375 3.375 0 013.375 2.25h1.5a3.375 3.375 0 013.375 3.375v1.5A1.125 1.125 0 017.125 9h-1.5a3.375 3.375 0 00-3.375 3.375z M21.75 11.625v2.625a3.375 3.375 0 01-3.375 3.375h-1.5a1.125 1.125 0 00-1.125 1.125v1.5a3.375 3.375 0 003.375 3.375h1.5a3.375 3.375 0 003.375-3.375v-1.5a1.125 1.125 0 00-1.125-1.125h-1.5a3.375 3.375 0 01-3.375-3.375z';
      default:
        return '';
    }
  }
}
