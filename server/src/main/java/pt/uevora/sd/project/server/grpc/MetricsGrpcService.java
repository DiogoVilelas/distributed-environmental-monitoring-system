package pt.uevora.sd.project.server.grpc;

import io.grpc.stub.StreamObserver;
import org.springframework.stereotype.Component;
import pt.uevora.sd.project.server.dto.MetricIngestRequest;
import pt.uevora.sd.project.server.service.MetricIngestService;

import java.time.Instant;

@Component
public class MetricsGrpcService extends MetricsServiceGrpc.MetricsServiceImplBase {

    private final MetricIngestService ingestService;

    public MetricsGrpcService(MetricIngestService ingestService) {
        this.ingestService = ingestService;
    }

    @Override
    public void ingestMetric(MetricsProto.MetricRequest request,
                             StreamObserver<MetricsProto.MetricReply> responseObserver) {

        try {
            MetricIngestRequest dto = new MetricIngestRequest();
            dto.setDeviceId(request.getDeviceId());
            dto.setTemperature(request.getTemperature());
            dto.setHumidity(request.getHumidity());
            dto.setTimestamp(Instant.parse(request.getTimestamp()));

            boolean stored = ingestService.ingest(dto);

            String msg = stored ? "Stored" : "Discarded (device not registered or inactive): " + dto.getDeviceId();

            MetricsProto.MetricReply reply = MetricsProto.MetricReply.newBuilder()
                    .setStored(stored)
                    .setMessage(msg)
                    .build();

            responseObserver.onNext(reply);
            responseObserver.onCompleted();

        } catch (Exception e) {
            MetricsProto.MetricReply reply = MetricsProto.MetricReply.newBuilder()
                    .setStored(false)
                    .setMessage("Error: " + e.getMessage())
                    .build();

            responseObserver.onNext(reply);
            responseObserver.onCompleted();
        }
    }
}
