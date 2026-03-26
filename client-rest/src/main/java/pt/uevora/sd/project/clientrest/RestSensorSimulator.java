package pt.uevora.sd.project.clientrest;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Instant;
import java.util.Locale;
import java.util.Random;

public class RestSensorSimulator {

    private static final String BASE_URL = "http://localhost:8080";
    private static final String DEFAULT_DEVICE_ID = "dev-rest01";

    public static void main(String[] args) throws Exception {
        String deviceId = (args.length >= 1) ? args[0] : DEFAULT_DEVICE_ID;
        int intervalMs = (args.length >= 2) ? Integer.parseInt(args[1]) : 2000;

        HttpClient http = HttpClient.newHttpClient();
        Random rnd = new Random();

        double temp = 20.0 + rnd.nextDouble() * 5.0;
        double hum  = 45.0 + rnd.nextDouble() * 10.0;

        while (true) {
            temp = clamp(temp + (rnd.nextDouble() - 0.5) * 1.0, 15.0, 30.0);
            hum  = clamp(hum  + (rnd.nextDouble() - 0.5) * 2.0, 30.0, 80.0);

            String ts = Instant.now().toString();

            String json = String.format(
                    Locale.US,
                    "{\"deviceId\":\"%s\",\"temperature\":%.2f,\"humidity\":%.2f,\"timestamp\":\"%s\"}",
                    deviceId, temp, hum, ts
            );

            HttpRequest req = HttpRequest.newBuilder()
                    .uri(URI.create(BASE_URL + "/api/metrics/ingest"))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(json))
                    .build();

            HttpResponse<String> resp = http.send(req, HttpResponse.BodyHandlers.ofString());

            System.out.println("POST /api/metrics/ingest -> " + resp.statusCode() + " " + resp.body());
            Thread.sleep(intervalMs);
        }
    }

    private static double clamp(double v, double min, double max) {
        return Math.max(min, Math.min(max, v));
    }
}
