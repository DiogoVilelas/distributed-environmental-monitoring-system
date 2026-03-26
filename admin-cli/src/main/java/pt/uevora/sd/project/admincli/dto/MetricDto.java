package pt.uevora.sd.project.admincli.dto;

import java.time.Instant;

public class MetricDto {
    public long id;
    public String deviceId;
    public double temperature;
    public double humidity;
    public Instant timestamp;
}
