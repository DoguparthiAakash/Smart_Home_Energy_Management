
import { Component, ChangeDetectionStrategy, input, computed } from '@angular/core';
import { CommonModule } from '@angular/common';
import { EnergyDataPoint } from '../../models/device.model';

@Component({
  selector: 'app-energy-chart',
  templateUrl: './energy-chart.component.html',
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [CommonModule]
})
export class EnergyChartComponent {
  data = input.required<EnergyDataPoint[]>();

  viewBox = '0 0 300 100';
  
  // A computed signal to generate the SVG path string
  path = computed(() => {
    const dataPoints = this.data();
    if (!dataPoints || dataPoints.length < 2) {
      return 'M 0,50 L 300,50'; // Flat line if no data
    }

    const maxUsage = Math.max(...dataPoints.map(p => p.usage), 0);
    const minUsage = Math.min(...dataPoints.map(p => p.usage), 0);
    
    // Add padding to avoid clipping at the top/bottom
    const range = (maxUsage - minUsage) * 1.2;
    const effectiveMax = maxUsage + (range * 0.1);
    const effectiveMin = Math.max(0, minUsage - (range * 0.1));
    const effectiveRange = effectiveMax - effectiveMin;

    if (effectiveRange === 0) {
        return 'M 0,50 L 300,50'; // Flat line if range is zero
    }

    const points = dataPoints.map((point, index) => {
      const x = (index / (dataPoints.length - 1)) * 300;
      const y = 100 - ((point.usage - effectiveMin) / effectiveRange) * 100;
      return `${x.toFixed(2)},${y.toFixed(2)}`;
    });

    return `M ${points.join(' L ')}`;
  });
}
