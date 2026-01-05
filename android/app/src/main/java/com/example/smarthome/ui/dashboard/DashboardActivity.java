package com.example.smarthome.ui.dashboard;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.smarthome.R;
import com.example.smarthome.model.Device;
import com.example.smarthome.util.SessionManager;
import com.example.smarthome.viewmodel.DeviceViewModel;
import java.util.Collections;
import java.util.List;

public class DashboardActivity extends AppCompatActivity {
    private DeviceViewModel deviceViewModel;
    private SessionManager sessionManager;
    private DeviceAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        sessionManager = new SessionManager(this);
        deviceViewModel = new ViewModelProvider(this).get(DeviceViewModel.class);

        RecyclerView rvDevices = findViewById(R.id.rvDevices);
        rvDevices.setLayoutManager(new androidx.recyclerview.widget.GridLayoutManager(this, 2));
        adapter = new DeviceAdapter();
        rvDevices.setAdapter(adapter);

        String token = sessionManager.getToken();
        deviceViewModel.fetchDevices(token);

        deviceViewModel.getDevices().observe(this, devices -> {
            adapter.setDevices(devices);
        });

        deviceViewModel.getError().observe(this, error -> {
            Toast.makeText(this, error, Toast.LENGTH_SHORT).show();
        });
    }

    class DeviceAdapter extends RecyclerView.Adapter<DeviceAdapter.DeviceViewHolder> {
        private List<Device> devices = Collections.emptyList();

        public void setDevices(List<Device> devices) {
            this.devices = devices;
            notifyDataSetChanged();
        }

        @NonNull
        @Override
        public DeviceViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_device, parent, false);
            // Fix width for grid
            ViewGroup.LayoutParams lp = view.getLayoutParams();
            if (lp instanceof ViewGroup.MarginLayoutParams) {
                // Ensure margins are respected in Grid
            }
            return new DeviceViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull DeviceViewHolder holder, int position) {
            Device device = devices.get(position);
            holder.tvDeviceName.setText(device.getName());

            boolean isOn = device.getStatus() != null && device.getStatus();
            holder.tvStatus.setText(isOn ? "On" : "Off");
            holder.tvStatus.setTextColor(holder.itemView.getContext().getResources()
                    .getColor(isOn ? R.color.status_on : R.color.status_off));

            // Apple Home style: Icon color changes
            holder.ivIcon.setColorFilter(holder.itemView.getContext().getResources()
                    .getColor(isOn ? R.color.status_on : R.color.secondary_gray));

            holder.itemView.setOnClickListener(v -> {
                deviceViewModel.toggleDevice(sessionManager.getToken(), device.getId());
            });
        }

        @Override
        public int getItemCount() {
            return devices.size();
        }

        class DeviceViewHolder extends RecyclerView.ViewHolder {
            TextView tvDeviceName;
            TextView tvStatus;
            android.widget.ImageView ivIcon;

            DeviceViewHolder(@NonNull View itemView) {
                super(itemView);
                tvDeviceName = itemView.findViewById(R.id.tvDeviceName);
                tvStatus = itemView.findViewById(R.id.tvStatus);
                ivIcon = itemView.findViewById(R.id.ivIcon);
            }
        }
    }
}
