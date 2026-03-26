package pt.uevora.sd.project.server.dto;

public class AverageMetricsResponse {

    private String level;      
    private String key;        
    private double avgTemperature;
    private double avgHumidity;
    private long count;

    public AverageMetricsResponse(String level, String key, double avgTemperature, double avgHumidity, long count) {
        this.level = level;
        this.key = key;
        this.avgTemperature = avgTemperature;
        this.avgHumidity = avgHumidity;
        this.count = count;
    }

    public String getLevel() { return level; }
    public String getKey() { return key; }
    public double getAvgTemperature() { return avgTemperature; }
    public double getAvgHumidity() { return avgHumidity; }
    public long getCount() { return count; }
}
