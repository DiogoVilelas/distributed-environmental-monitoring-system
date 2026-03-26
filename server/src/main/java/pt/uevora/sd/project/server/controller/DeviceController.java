package pt.uevora.sd.project.server.controller;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pt.uevora.sd.project.server.dto.DeviceRequest;
import pt.uevora.sd.project.server.model.Device;
import pt.uevora.sd.project.server.model.Protocol;
import pt.uevora.sd.project.server.model.Status;
import pt.uevora.sd.project.server.repository.DeviceRepository;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/devices")
public class DeviceController {

    private final DeviceRepository repo;

    public DeviceController(DeviceRepository repo) {
        this.repo = repo;
    }

    @PostMapping
    public ResponseEntity<?> create(@Valid @RequestBody DeviceRequest req) {
        if (repo.existsById(req.getId())) {
            return ResponseEntity.badRequest().body("Device already exists: " + req.getId());
        }

        Device d = toEntity(req);
        repo.save(d);
        return ResponseEntity.created(URI.create("/api/devices/" + d.getId())).body(d);
    }

    @GetMapping
    public List<Device> list() {
        return repo.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Device> get(@PathVariable String id) {
        return repo.findById(id).map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> update(@PathVariable String id, @Valid @RequestBody DeviceRequest req) {
        if (!id.equals(req.getId())) {
            return ResponseEntity.badRequest().body("Path id and body id must match");
        }
        if (!repo.existsById(id)) {
            return ResponseEntity.notFound().build();
        }

        Device d = toEntity(req);
        repo.save(d);
        return ResponseEntity.ok(d);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable String id) {
        if (!repo.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        repo.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    private Device toEntity(DeviceRequest req) {
        Device d = new Device();
        d.setId(req.getId());
        d.setProtocol(Protocol.valueOf(req.getProtocol().toUpperCase()));
        d.setBuilding(req.getBuilding());
        d.setFloor(req.getFloor());
        d.setDepartment(req.getDepartment());
        d.setRoom(req.getRoom());
        d.setStatus(Status.valueOf(req.getStatus().toUpperCase()));
        return d;
    }
}
