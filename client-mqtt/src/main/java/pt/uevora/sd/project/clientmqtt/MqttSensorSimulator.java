package pt.uevora.sd.project.clientmqtt;

import org.eclipse.paho.client.mqttv3.*;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
import java.util.Locale;

import java.time.Instant;
import java.util.Random;

public class MqttSensorSimulator {

    private static final String BROKER = "tcp://localhost:1883";
    private static final String TOPIC = "sdist/metrics";

    private static final String USERNAME = "62347";
    private static final String PASSWORD = "WORKT02SD";

    private static final String DEFAULT_DEVICE_ID = "dev-mqtt01";

    public static void main(String[] args) throws Exception {

        String deviceId = (args.length >= 1) ? args[0] : DEFAULT_DEVICE_ID;
        int intervalMs = (args.length >= 2) ? Integer.parseInt(args[1]) : 2000;

        String clientId = "client-mqtt-" + deviceId + "-" + System.currentTimeMillis();

        @SuppressWarnings("resource")
        MqttClient client = new MqttClient(BROKER, clientId, new MemoryPersistence());

        MqttConnectOptions opts = new MqttConnectOptions();
        opts.setCleanSession(true);
        opts.setUserName(USERNAME);
        opts.setPassword(PASSWORD.toCharArray());

        System.out.println("Connecting to broker: " + BROKER);
        client.connect(opts);
        System.out.println("Connected. Publishing to topic: " + TOPIC);

        Random rnd = new Random();

        double temp = 20.0 + rnd.nextDouble() * 5.0; 
        double hum = 45.0 + rnd.nextDouble() * 10.0; 
        while (true) {
            temp = clamp(temp + (rnd.nextDouble() - 0.5) * 1.0, 15.0, 30.0); 
            hum = clamp(hum + (rnd.nextDouble() - 0.5) * 2.0, 30.0, 80.0); 

            String ts = Instant.now().toString();

            String json = String.format(
                    Locale.US,
                    "{\"deviceId\":\"%s\",\"temperature\":%.2f,\"humidity\":%.2f,\"timestamp\":\"%s\"}",
                    deviceId, temp, hum, ts);

            MqttMessage msg = new MqttMessage(json.getBytes());
            msg.setQos(1);

            client.publish(TOPIC, msg);
            System.out.println("Published: " + json);

            Thread.sleep(intervalMs);
        }
    }

    private static double clamp(double v, double min, double max) {
        return Math.max(min, Math.min(max, v));
    }
}
