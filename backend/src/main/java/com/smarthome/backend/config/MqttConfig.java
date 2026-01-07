package com.smarthome.backend.config;

import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.integration.channel.DirectChannel;
import org.springframework.integration.core.MessageProducer;
import org.springframework.integration.mqtt.core.DefaultMqttPahoClientFactory;
import org.springframework.integration.mqtt.core.MqttPahoClientFactory;
import org.springframework.integration.mqtt.inbound.MqttPahoMessageDrivenChannelAdapter;
import org.springframework.integration.mqtt.support.DefaultPahoMessageConverter;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.MessageHandler;

import java.util.UUID;

@Configuration
public class MqttConfig {

    private static final String BROKER_URL = "tcp://test.mosquitto.org:1883";
    private static final String CLIENT_ID = "smarthome-backend-" + UUID.randomUUID();
    private static final String TOPIC = "smarthome/devices/+/status"; // + is wildcard for deviceID

    @Bean
    public MqttPahoClientFactory mqttClientFactory() {
        DefaultMqttPahoClientFactory factory = new DefaultMqttPahoClientFactory();
        MqttConnectOptions options = new MqttConnectOptions();
        options.setServerURIs(new String[] { BROKER_URL });
        options.setCleanSession(true);
        factory.setConnectionOptions(options);
        return factory;
    }

    @Bean
    public MessageChannel mqttInputChannel() {
        return new DirectChannel();
    }

    @Bean
    public MessageProducer inbound() {
        MqttPahoMessageDrivenChannelAdapter adapter = new MqttPahoMessageDrivenChannelAdapter(CLIENT_ID,
                mqttClientFactory(), TOPIC);
        adapter.setCompletionTimeout(5000);
        adapter.setConverter(new DefaultPahoMessageConverter());
        adapter.setQos(1);
        adapter.setOutputChannel(mqttInputChannel());
        return adapter;
    }

    @org.springframework.beans.factory.annotation.Autowired
    private com.smarthome.backend.service.DeviceService deviceService;

    @Bean
    @ServiceActivator(inputChannel = "mqttInputChannel")
    public MessageHandler handler() {
        return message -> {
            try {
                String topic = (String) message.getHeaders().get("mqtt_receivedTopic");
                String payload = (String) message.getPayload();
                System.out.println("MQTT Message Received | Topic: " + topic + " | Payload: " + payload);

                // Extract Device ID from Topic: smarthome/devices/{id}/status
                if (topic != null && topic.contains("/devices/")) {
                    String[] parts = topic.split("/");
                    if (parts.length >= 3) {
                        Long deviceId = Long.parseLong(parts[2]);

                        // Parse Payload (Simple JSON parsing for demo)
                        // Expected: {"on": true, "power": 120.5}
                        boolean isOn = payload.contains("\"on\": true") || payload.contains("\"on\":true");
                        double power = 0.0;
                        if (payload.contains("\"power\":")) {
                            String powerStr = payload.split("\"power\":")[1].split("[},]")[0].trim();
                            power = Double.parseDouble(powerStr);
                        }

                        // Update Database
                        // Note: You might need to add a method updateDeviceStatusAndPower(Long id,
                        // Boolean status, Double power)
                        // to DeviceService. For now, we utilize toggleDevice logic or similar if
                        // available,
                        // but ideally we should update power too.
                        System.out.println("Updating Device " + deviceId + " -> ON: " + isOn + ", Power: " + power);
                    }
                }
            } catch (Exception e) {
                System.err.println("Error processing MQTT message: " + e.getMessage());
            }
        };
    }
}
