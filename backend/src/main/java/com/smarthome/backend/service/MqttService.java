package com.smarthome.backend.service;

import com.smarthome.backend.model.Device;
import com.smarthome.backend.repository.DeviceRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.messaging.Message;
import org.springframework.stereotype.Service;

@Service
public class MqttService {

    @Autowired
    private DeviceRepository deviceRepository;

    @ServiceActivator(inputChannel = "mqttInputChannel")
    public void handleMqttMessage(Message<?> message) {
        try {
            String topic = (String) message.getHeaders().get("mqtt_receivedTopic");
            String payload = (String) message.getPayload();
            System.out.println("MQTT Inbound: " + topic + " -> " + payload);

            // Example Logic: Update device status if topic matches known pattern
            // Pattern: smarthome/devices/{id}/status
            if (topic != null && topic.contains("/status")) {
                // Parse and update logic here if needed
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
