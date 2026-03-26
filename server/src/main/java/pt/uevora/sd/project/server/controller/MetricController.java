package pt.uevora.sd.project.server.controller;

import jakarta.validation.Valid;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pt.uevora.sd.project.server.dto.AverageMetricsResponse;
import pt.uevora.sd.project.server.dto.MetricIngestRequest;
import pt.uevora.sd.project.server.model.Metric;
import pt.uevora.sd.project.server.repository.DeviceRepository;
import pt.uevora.sd.project.server.repository.MetricRepository;
import pt.uevora.sd.project.server.service.MetricIngestService;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

@RestController
@RequestMapping("/api/metrics")
public class MetricController {

    private final MetricIngestService ingestService;
    private final MetricRepository metricRepo;
    private final DeviceRepository deviceRepo;

    public MetricController(MetricIngestService ingestService, MetricRepository metricRepo, DeviceRepository deviceRepo) {
        this.ingestService = ingestService;
        this.metricRepo = metricRepo;
        this.deviceRepo = deviceRepo;
    }

    @PostMapping("/ingest")
    public ResponseEntity<?> ingest(@Valid @RequestBody MetricIngestRequest req) {
        boolean stored = ingestService.ingest(req);
        if (!stored) {
            return ResponseEntity.accepted()
                    .body("Discarded (device not registered or inactive): " + req.getDeviceId());
        }
        return ResponseEntity.ok("Stored");
    }

    @GetMapping("/raw")
    public ResponseEntity<List<Metric>> raw(
            @RequestParam String deviceId,
            @RequestParam(required = false) Instant from,
            @RequestParam(required = false) Instant to
    ) {
        Instant toI = (to != null) ? to : Instant.now();
        Instant fromI = (from != null) ? from : toI.minus(24, ChronoUnit.HOURS);
        return ResponseEntity.ok(metricRepo.findByDeviceIdAndTimestampBetween(deviceId, fromI, toI));
    }

    @GetMapping("/all")
    public ResponseEntity<List<Metric>> all(
            @RequestParam(required = false) Instant from,
            @RequestParam(required = false) Instant to
    ) {
        if (from == null && to == null) {
            return ResponseEntity.ok(metricRepo.findAll(Sort.by(Sort.Direction.ASC, "timestamp")));
        }

        Instant fromI = (from != null) ? from : Instant.EPOCH;
        Instant toI = (to != null) ? to : Instant.now();

        return ResponseEntity.ok(metricRepo.findByTimestampBetween(fromI, toI, Sort.by(Sort.Direction.ASC, "timestamp")));
    }

    @GetMapping("/average")
    public ResponseEntity<AverageMetricsResponse> average(
            @RequestParam String level,
            @RequestParam String id,
            @RequestParam(required = false) Instant from,
            @RequestParam(required = false) Instant to
    ) {
        Instant toI = (to != null) ? to : Instant.now();
        Instant fromI = (from != null) ? from : toI.minus(24, ChronoUnit.HOURS);

        String lvl = normalizeLevel(level);
        String key = id;

        List<String> deviceIds;

        switch (lvl) {
            case "ROOM" -> deviceIds = deviceRepo.findIdsByRoomIgnoreCase(id);
            case "DEPARTMENT" -> deviceIds = deviceRepo.findIdsByDepartmentIgnoreCase(id);
            case "BUILDING" -> deviceIds = deviceRepo.findIdsByBuildingIgnoreCase(id);
            case "FLOOR" -> {
                String[] parts = id.split(":", 2);
                if (parts.length != 2) {
                    return ResponseEntity.ok(new AverageMetricsResponse("FLOOR", id, 0.0, 0.0, 0L));
                }

                String building = parts[0].trim();
                String floorStr = parts[1].trim();

                int floor;
                try {
                    floor = Integer.parseInt(floorStr);
                } catch (Exception e) {
                    return ResponseEntity.ok(new AverageMetricsResponse("FLOOR", id, 0.0, 0.0, 0L));
                }

                key = building + ":" + floor;
                deviceIds = deviceRepo.findIdsByBuildingAndFloor(building, floor);
            }
            default -> {
                return ResponseEntity.badRequest().build();
            }
        }

        if (deviceIds == null || deviceIds.isEmpty()) {
            return ResponseEntity.ok(new AverageMetricsResponse(lvl, key, 0.0, 0.0, 0L));
        }

        List<Metric> rows = metricRepo.findByDeviceIdInAndTimestampBetween(deviceIds, fromI, toI);

        long count = rows.size();
        if (count == 0) {
            return ResponseEntity.ok(new AverageMetricsResponse(lvl, key, 0.0, 0.0, 0L));
        }

        double sumT = 0.0;
        double sumH = 0.0;
        for (Metric m : rows) {
            sumT += m.getTemperature();
            sumH += m.getHumidity();
        }

        double avgTemp = sumT / count;
        double avgHum = sumH / count;

        return ResponseEntity.ok(new AverageMetricsResponse(lvl, key, avgTemp, avgHum, count));
    }

    
    private static String normalizeLevel(String level) {
        if (level == null) return "";
        String v = level.trim().toLowerCase();
        return switch (v) {
            case "room", "sala" -> "ROOM";
            case "department", "departamento" -> "DEPARTMENT";
            case "floor", "piso" -> "FLOOR";
            case "building", "edificio", "edifício" -> "BUILDING";
            default -> level.trim().toUpperCase();
        };
    }
}
