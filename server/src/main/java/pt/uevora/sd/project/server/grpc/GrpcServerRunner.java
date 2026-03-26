package pt.uevora.sd.project.server.grpc;

import io.grpc.Server;
import io.grpc.ServerBuilder;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.springframework.stereotype.Component;

@Component
public class GrpcServerRunner {

    private final MetricsGrpcService metricsGrpcService;
    private Server server;

    public GrpcServerRunner(MetricsGrpcService metricsGrpcService) {
        this.metricsGrpcService = metricsGrpcService;
    }

    @PostConstruct
    public void start() throws Exception {
        server = ServerBuilder.forPort(50051)
                .addService(metricsGrpcService)
                .build()
                .start();

        System.out.println("[gRPC] Server started on port 50051");
    }

    @PreDestroy
    public void stop() throws Exception {
        if (server != null) {
            server.shutdown();
            System.out.println("[gRPC] Server stopped");
        }
    }
}
