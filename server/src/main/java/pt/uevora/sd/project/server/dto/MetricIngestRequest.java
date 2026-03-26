package pt.uevora.sd.project.server.dto;

import jakarta.validation.constraints.*;
import java.time.Instant;

public class MetricIngestRequest {

    @NotBlank
    private String deviceId;

    @DecimalMin("15.0")
    @DecimalMax("30.0")
    private double temperature;

    @DecimalMin("30.0")
    @DecimalMax("80.0")
    private double humidity;

    @NotNull
    private Instant timestamp;

    public MetricIngestRequest() {}

    public String getDeviceId() { return deviceId; }
    public void setDeviceId(String deviceId) { this.deviceId = deviceId; }

    public double getTemperature() { return temperature; }
    public void setTemperature(double temperature) { this.temperature = temperature; }

    public double getHumidity() { return humidity; }
    public void setHumidity(double humidity) { this.humidity = humidity; }

    public Instant getTimestamp() { return timestamp; }
    public void setTimestamp(Instant timestamp) { this.timestamp = timestamp; }
}
