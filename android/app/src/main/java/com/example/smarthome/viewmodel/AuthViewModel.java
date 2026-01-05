package com.example.smarthome.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import com.example.smarthome.model.AuthRequest;
import com.example.smarthome.model.AuthResponse;
import com.example.smarthome.network.RetrofitClient;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AuthViewModel extends ViewModel {
    private MutableLiveData<AuthResponse> authResponse = new MutableLiveData<>();
    private MutableLiveData<String> error = new MutableLiveData<>();

    public LiveData<AuthResponse> getAuthResponse() {
        return authResponse;
    }

    public LiveData<String> getError() {
        return error;
    }

    public void login(String email, String password) {
        RetrofitClient.getService().login(new AuthRequest(email, password)).enqueue(new Callback<AuthResponse>() {
            @Override
            public void onResponse(Call<AuthResponse> call, Response<AuthResponse> response) {
                if (response.isSuccessful()) {
                    authResponse.setValue(response.body());
                } else {
                    error.setValue("Login Failed");
                }
            }

            @Override
            public void onFailure(Call<AuthResponse> call, Throwable t) {
                error.setValue(t.getMessage());
            }
        });
    }
}
