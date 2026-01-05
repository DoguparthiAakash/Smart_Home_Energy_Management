package com.example.smarthome.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import com.example.smarthome.model.Device;
import com.example.smarthome.network.ApiService;
import com.example.smarthome.network.RetrofitClient;
import java.util.List;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class DeviceViewModel extends ViewModel {
    private MutableLiveData<List<Device>> devices = new MutableLiveData<>();
    private MutableLiveData<String> error = new MutableLiveData<>();

    public LiveData<List<Device>> getDevices() {
        return devices;
    }

    public LiveData<String> getError() {
        return error;
    }

    public void fetchDevices(String token) {
        RetrofitClient.getService().getDevices(token).enqueue(new Callback<List<Device>>() {
            @Override
            public void onResponse(Call<List<Device>> call, Response<List<Device>> response) {
                if (response.isSuccessful()) {
                    devices.setValue(response.body());
                } else {
                    error.setValue("Failed to fetch devices");
                }
            }

            @Override
            public void onFailure(Call<List<Device>> call, Throwable t) {
                error.setValue(t.getMessage());
            }
        });
    }

    public void toggleDevice(String token, Long deviceId) {
        RetrofitClient.getService().toggleDevice(token, deviceId).enqueue(new Callback<Device>() {
            @Override
            public void onResponse(Call<Device> call, Response<Device> response) {
                if (response.isSuccessful()) {
                    fetchDevices(token); // Refresh list
                }
            }

            @Override
            public void onFailure(Call<Device> call, Throwable t) {
                error.setValue(t.getMessage());
            }
        });
    }
}
