package pt.uevora.sd.project.clientgrpc;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;

import java.time.Instant;
import java.util.Locale;
import java.util.Random;

public class GrpcMetricClient {

    private static final String HOST = "localhost";
    private static final int PORT = 50051;

    public static void main(String[] args) throws Exception {

        String deviceId = (args.length >= 1) ? args[0] : "dev-grpc01";
        int intervalMs  = (args.length >= 2) ? Integer.parseInt(args[1]) : 2000;

        ManagedChannel channel = ManagedChannelBuilder
                .forAddress(HOST, PORT)
                .usePlaintext()
                .build();

        System.out.println("Client gRPC ligado a " + HOST + ":" + PORT);

        MetricsServiceGrpc.MetricsServiceBlockingStub stub =
                MetricsServiceGrpc.newBlockingStub(channel);

        Random rnd = new Random();
        double temp = 20.0 + rnd.nextDouble() * 5.0;
        double hum  = 45.0 + rnd.nextDouble() * 10.0;

        try {
            while (true) {
                temp = clamp(temp + (rnd.nextDouble() - 0.5) * 1.0, 15.0, 30.0);
                hum  = clamp(hum  + (rnd.nextDouble() - 0.5) * 2.0, 30.0, 80.0);

                String ts = Instant.now().toString();

                MetricsProto.MetricRequest req = MetricsProto.MetricRequest.newBuilder()
                        .setDeviceId(deviceId)
                        .setTemperature(Double.parseDouble(String.format(Locale.US, "%.2f", temp)))
                        .setHumidity(Double.parseDouble(String.format(Locale.US, "%.2f", hum)))
                        .setTimestamp(ts)
                        .build();

                MetricsProto.MetricReply reply = stub.ingestMetric(req);

                System.out.println("gRPC ingest -> stored=" + reply.getStored() + " msg=" + reply.getMessage());

                Thread.sleep(intervalMs);
            }
        } finally {
            channel.shutdown();
        }
    }

    private static double clamp(double v, double min, double max) {
        return Math.max(min, Math.min(max, v));
    }
}
