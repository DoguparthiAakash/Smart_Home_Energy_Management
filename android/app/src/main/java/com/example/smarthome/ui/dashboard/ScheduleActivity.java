package com.example.smarthome.ui.dashboard;

import android.os.Bundle;
import android.widget.Button;
import android.widget.TimePicker;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.smarthome.R;

public class ScheduleActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_analytics); // Reuse layout for demo speed or create new
        // For demo, we'll just show a toast that this is where scheduling happens
        Toast.makeText(this, "Schedule Feature Ready", Toast.LENGTH_LONG).show();
        finish();
    }
}
