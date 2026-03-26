package pt.uevora.sd.project.admincli;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import pt.uevora.sd.project.admincli.dto.*;

import java.net.URI;
import java.net.http.*;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.List;

public class ApiClient {

    private final String baseUrl;
    private final HttpClient http;
    private final ObjectMapper om;

    public ApiClient(String baseUrl) {
        this.baseUrl = baseUrl;
        this.http = HttpClient.newHttpClient();
        this.om = new ObjectMapper().registerModule(new JavaTimeModule());
    }

    public List<DeviceDto> listDevices() throws Exception {
        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + "/api/devices"))
                .GET().build();
        HttpResponse<String> resp = http.send(req, HttpResponse.BodyHandlers.ofString());
        ensure2xx(resp);
        return om.readValue(resp.body(), new TypeReference<List<DeviceDto>>() {});
    }

    public DeviceDto getDevice(String id) throws Exception {
        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + "/api/devices/" + encode(id)))
                .GET().build();
        HttpResponse<String> resp = http.send(req, HttpResponse.BodyHandlers.ofString());
        ensure2xx(resp);
        return om.readValue(resp.body(), DeviceDto.class);
    }

    public String createDevice(DeviceDto d) throws Exception {
        String json = om.writeValueAsString(d);
        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + "/api/devices"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(json, StandardCharsets.UTF_8))
                .build();
        HttpResponse<String> resp = http.send(req, HttpResponse.BodyHandlers.ofString());
        return resp.body();
    }

    public String updateDevice(String id, DeviceDto d) throws Exception {
        String json = om.writeValueAsString(d);
        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + "/api/devices/" + encode(id)))
                .header("Content-Type", "application/json")
                .PUT(HttpRequest.BodyPublishers.ofString(json, StandardCharsets.UTF_8))
                .build();
        HttpResponse<String> resp = http.send(req, HttpResponse.BodyHandlers.ofString());
        return resp.body();
    }

    public String deleteDevice(String id) throws Exception {
        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + "/api/devices/" + encode(id)))
                .DELETE().build();
        HttpResponse<String> resp = http.send(req, HttpResponse.BodyHandlers.ofString());
        return resp.body();
    }

    public List<MetricDto> rawMetrics(String deviceId, Instant from, Instant to) throws Exception {
        String url = baseUrl + "/api/metrics/raw?deviceId=" + encode(deviceId)
                + (from != null ? "&from=" + encode(from.toString()) : "")
                + (to != null ? "&to=" + encode(to.toString()) : "");
        HttpRequest req = HttpRequest.newBuilder().uri(URI.create(url)).GET().build();
        HttpResponse<String> resp = http.send(req, HttpResponse.BodyHandlers.ofString());
        ensure2xx(resp);
        return om.readValue(resp.body(), new TypeReference<List<MetricDto>>() {});
    }

    public List<MetricDto> allMetrics(Instant from, Instant to) throws Exception {
        String url = baseUrl + "/api/metrics/all"
                + (from != null ? "?from=" + encode(from.toString()) : "?")
                + (to != null ? (from != null ? "&" : "") + "to=" + encode(to.toString()) : "");

        if (url.endsWith("?")) url = url.substring(0, url.length() - 1);

        HttpRequest req = HttpRequest.newBuilder().uri(URI.create(url)).GET().build();
        HttpResponse<String> resp = http.send(req, HttpResponse.BodyHandlers.ofString());
        ensure2xx(resp);
        return om.readValue(resp.body(), new TypeReference<List<MetricDto>>() {});
    }

    public AverageDto averageSingle(String level, String id, Instant from, Instant to) throws Exception {
        String url = baseUrl + "/api/metrics/average?level=" + encode(level)
                + "&id=" + encode(id)
                + (from != null ? "&from=" + encode(from.toString()) : "")
                + (to != null ? "&to=" + encode(to.toString()) : "");

        HttpRequest req = HttpRequest.newBuilder().uri(URI.create(url)).GET().build();
        HttpResponse<String> resp = http.send(req, HttpResponse.BodyHandlers.ofString());
        ensure2xx(resp);
        return om.readValue(resp.body(), AverageDto.class);
    }

    public List<AverageDto> averageGrouped(String level, Instant from, Instant to) throws Exception {
        String url = baseUrl + "/api/metrics/average/grouped?level=" + encode(level)
                + (from != null ? "&from=" + encode(from.toString()) : "")
                + (to != null ? "&to=" + encode(to.toString()) : "");

        HttpRequest req = HttpRequest.newBuilder().uri(URI.create(url)).GET().build();
        HttpResponse<String> resp = http.send(req, HttpResponse.BodyHandlers.ofString());
        ensure2xx(resp);
        return om.readValue(resp.body(), new TypeReference<List<AverageDto>>() {});
    }

    public MetricDto latestMetric(String deviceId) throws Exception {
        List<MetricDto> rows = rawMetrics(deviceId, null, null);
        if (rows == null || rows.isEmpty()) return null;

        MetricDto best = rows.get(0);
        for (MetricDto m : rows) {
            if (m.timestamp != null && best.timestamp != null) {
                if (m.timestamp.isAfter(best.timestamp)) best = m;
            } else if (m.id > best.id) {
                best = m;
            }
        }
        return best;
    }

    private static String encode(String s) {
        return java.net.URLEncoder.encode(s, StandardCharsets.UTF_8);
    }

    private static void ensure2xx(HttpResponse<String> resp) {
        int sc = resp.statusCode();
        if (sc < 200 || sc >= 300) {
            throw new RuntimeException("HTTP " + sc + " -> " + resp.body());
        }
    }
}
