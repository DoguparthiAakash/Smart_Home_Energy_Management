
import { Component, ChangeDetectionStrategy } from '@angular/core';
import { DashboardComponent } from './components/dashboard/dashboard.component';

@Component({
  selector: 'app-root',
  templateUrl: './app.component.html',
  styleUrls: [],
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [DashboardComponent]
})
export class AppComponent {
  title = 'smart-home-energy-manager';
}
