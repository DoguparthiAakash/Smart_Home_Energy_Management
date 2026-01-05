package com.example.smarthome.ui.auth;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.smarthome.R;

public class RegisterActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login); // Reusing login layout for demo speed

        EditText etEmail = findViewById(R.id.etEmail);
        EditText etPassword = findViewById(R.id.etPassword);
        Button btnRegister = findViewById(R.id.btnLogin);
        btnRegister.setText("Create Account");

        btnRegister.setOnClickListener(v -> {
            Toast.makeText(this, "Registration Simulated (API Ready)", Toast.LENGTH_SHORT).show();
            finish();
        });
    }
}
