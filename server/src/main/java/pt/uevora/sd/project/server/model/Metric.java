package pt.uevora.sd.project.server.model;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "metrics", indexes = {
        @Index(name = "idx_metrics_device_time", columnList = "device_id,timestamp")
})
public class Metric {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "device_id", nullable = false, length = 64)
    private String deviceId;

    @Column(nullable = false)
    private double temperature;

    @Column(nullable = false)
    private double humidity;

    @Column(nullable = false)
    private Instant timestamp;

    public Metric() {}

    public Long getId() { return id; }

    public String getDeviceId() { return deviceId; }
    public void setDeviceId(String deviceId) { this.deviceId = deviceId; }

    public double getTemperature() { return temperature; }
    public void setTemperature(double temperature) { this.temperature = temperature; }

    public double getHumidity() { return humidity; }
    public void setHumidity(double humidity) { this.humidity = humidity; }

    public Instant getTimestamp() { return timestamp; }
    public void setTimestamp(Instant timestamp) { this.timestamp = timestamp; }
}
