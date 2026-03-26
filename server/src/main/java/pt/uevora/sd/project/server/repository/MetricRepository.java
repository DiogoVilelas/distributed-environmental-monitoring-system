package pt.uevora.sd.project.server.repository;

import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import pt.uevora.sd.project.server.model.Metric;

import java.time.Instant;
import java.util.List;

public interface MetricRepository extends JpaRepository<Metric, Long> {

    
    List<Metric> findByDeviceIdAndTimestampBetween(String deviceId, Instant from, Instant to);

    List<Metric> findByTimestampBetween(Instant from, Instant to, Sort sort);

    List<Metric> findByDeviceIdInAndTimestampBetween(List<String> deviceIds, Instant from, Instant to);

   
}
