package pt.uevora.sd.project.server.service;

import org.springframework.stereotype.Service;
import pt.uevora.sd.project.server.dto.MetricIngestRequest;
import pt.uevora.sd.project.server.model.Device;
import pt.uevora.sd.project.server.model.Metric;
import pt.uevora.sd.project.server.model.Status;
import pt.uevora.sd.project.server.repository.DeviceRepository;
import pt.uevora.sd.project.server.repository.MetricRepository;

@Service
public class MetricIngestService {

    private final DeviceRepository deviceRepo;
    private final MetricRepository metricRepo;

    public MetricIngestService(DeviceRepository deviceRepo, MetricRepository metricRepo) {
        this.deviceRepo = deviceRepo;
        this.metricRepo = metricRepo;
    }

    public boolean ingest(MetricIngestRequest req) {
        Device d = deviceRepo.findById(req.getDeviceId()).orElse(null);

        
        if (d == null) return false;

        if (d.getStatus() == Status.INACTIVE) return false;

        Metric m = new Metric();
        m.setDeviceId(req.getDeviceId());
        m.setTemperature(req.getTemperature());
        m.setHumidity(req.getHumidity());
        m.setTimestamp(req.getTimestamp());

        metricRepo.save(m);
        return true;
    }
}
