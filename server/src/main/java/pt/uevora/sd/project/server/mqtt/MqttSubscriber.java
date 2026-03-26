package pt.uevora.sd.project.server.mqtt;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.eclipse.paho.client.mqttv3.*;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import pt.uevora.sd.project.server.dto.MetricIngestRequest;
import pt.uevora.sd.project.server.service.MetricIngestService;

@Component
public class MqttSubscriber {

    private final MetricIngestService ingestService;

    private final ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();

    @Value("${mqtt.broker}")
    private String broker;

    @Value("${mqtt.topic}")
    private String topic;

    @Value("${mqtt.clientId}")
    private String clientId;

    @Value("${mqtt.username}")
    private String username;

    @Value("${mqtt.password}")
    private String password;

    private MqttClient client;

    public MqttSubscriber(MetricIngestService ingestService) {
        this.ingestService = ingestService;
    }

    @PostConstruct
    public void start() throws Exception {
        client = new MqttClient(broker, clientId, new MemoryPersistence());

        MqttConnectOptions opts = new MqttConnectOptions();
        opts.setCleanSession(true);

        opts.setUserName(username);
        opts.setPassword(password.toCharArray());

        client.setCallback(new MqttCallback() {
            @Override
            public void connectionLost(Throwable cause) {
                System.out.println("[MQTT] connectionLost: " + cause.getMessage());
            }

            @Override
            public void messageArrived(String t, MqttMessage message) {
                String payload = new String(message.getPayload());
                try {
                    MetricIngestRequest req = objectMapper.readValue(payload, MetricIngestRequest.class);
                    boolean stored = ingestService.ingest(req);
                    System.out.println("[MQTT] topic=" + t + " stored=" + stored + " payload=" + payload);
                } catch (Exception e) {
                    System.out.println("[MQTT] invalid payload: " + payload + " error=" + e.getMessage());
                }
            }

            @Override
            public void deliveryComplete(IMqttDeliveryToken token) {
                
            }
        });

        System.out.println("[MQTT] Connecting to broker: " + broker);
        client.connect(opts);

        System.out.println("[MQTT] Connected. Subscribing to: " + topic);
        client.subscribe(topic, 1); 
        System.out.println("[MQTT] Subscribed OK");
    }

    @PreDestroy
    public void stop() throws Exception {
        if (client != null) {
            if (client.isConnected()) client.disconnect();
            client.close();
        }
    }
}
