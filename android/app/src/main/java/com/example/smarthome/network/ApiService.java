package com.example.smarthome.network;

import com.example.smarthome.model.AuthRequest;
import com.example.smarthome.model.AuthResponse;
import com.example.smarthome.model.Device;

import java.util.List;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;
import retrofit2.http.Header;

public interface ApiService {
    @POST("api/auth/login")
    Call<AuthResponse> login(@Body AuthRequest request);

    @GET("api/devices")
    Call<List<Device>> getDevices(@Header("Authorization") String token);

    @POST("api/devices")
    Call<Device> addDevice(@Header("Authorization") String token, @Body Device device);

    @PUT("api/devices/{id}/status")
    Call<Device> toggleDevice(@Header("Authorization") String token, @Path("id") Long id);
}
