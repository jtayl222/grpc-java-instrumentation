package greeting.server;

import io.opentelemetry.instrumentation.grpc.v1_6.GrpcTelemetry;
import io.grpc.Server;
import io.grpc.ServerBuilder;
import io.grpc.protobuf.services.ProtoReflectionService;
import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.trace.propagation.W3CTraceContextPropagator;
import io.opentelemetry.context.propagation.ContextPropagators;
import io.opentelemetry.exporter.otlp.trace.OtlpGrpcSpanExporter;
import io.opentelemetry.sdk.OpenTelemetrySdk;
import io.opentelemetry.sdk.resources.Resource;
import io.opentelemetry.sdk.trace.SdkTracerProvider;
import io.opentelemetry.sdk.trace.export.SimpleSpanProcessor;
import io.opentelemetry.semconv.resource.attributes.ResourceAttributes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


import java.io.IOException;
import java.util.concurrent.TimeUnit;

public final class GreetingServer {
    private static Logger logger = LoggerFactory.getLogger(GreetingServer.class);

    public static void main(String[] args) throws IOException, InterruptedException {
        int port = 50051;

        Resource resource = Resource.getDefault()
                .merge(Resource.create(Attributes.of(ResourceAttributes.SERVICE_NAME, "GreeterProtobufExampleApp")));

        SdkTracerProvider sdkTracerProvider = SdkTracerProvider.builder()
                .addSpanProcessor(SimpleSpanProcessor.create(OtlpGrpcSpanExporter.builder().setTimeout(0,TimeUnit.SECONDS).build()))
                .setResource(resource)
                .build();

        OpenTelemetry openTelemetry = OpenTelemetrySdk.builder()
                .setTracerProvider(sdkTracerProvider)
                .setPropagators(ContextPropagators.create(W3CTraceContextPropagator.getInstance()))
                .buildAndRegisterGlobal();

        GrpcTelemetry grpcTelemetry = GrpcTelemetry.create(openTelemetry);

        Server server = ServerBuilder.forPort(port)
            .addService(new GreetingServiceImpl(new SleeperImpl()))
            .addService(ProtoReflectionService.newInstance())
            .intercept(new LogInterceptor())
            .intercept(new HeaderCheckInterceptor())
            .intercept(grpcTelemetry.newServerInterceptor())
            .build();

        server.start();
        logger.info("Server Started");
        logger.info("Listening on port: " + port);

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            logger.info("Received Shutdown Request");
            server.shutdown();
            logger.info("Server Stopped");
        }));

        server.awaitTermination();
    }
}
