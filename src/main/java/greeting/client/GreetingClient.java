package greeting.client;

import io.opentelemetry.instrumentation.grpc.v1_6.GrpcTelemetry;
import com.proto.greeting.GreetingRequest;
import com.proto.greeting.GreetingResponse;
import com.proto.greeting.GreetingServiceGrpc;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.opentelemetry.api.GlobalOpenTelemetry;
import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.api.trace.propagation.W3CTraceContextPropagator;
import io.opentelemetry.context.Scope;
import io.opentelemetry.context.propagation.ContextPropagators;
import io.opentelemetry.exporter.otlp.trace.OtlpGrpcSpanExporter;
import io.opentelemetry.sdk.OpenTelemetrySdk;
import io.opentelemetry.sdk.resources.Resource;
import io.opentelemetry.sdk.trace.SdkTracerProvider;
import io.opentelemetry.sdk.trace.export.SimpleSpanProcessor;
import io.opentelemetry.semconv.resource.attributes.ResourceAttributes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.TimeUnit;

public final class GreetingClient {
    private static Logger logger = LoggerFactory.getLogger(GreetingClient.class);
    private static void doGreet(ManagedChannel channel) {
            Tracer tracer = GlobalOpenTelemetry.getTracer("Greeter", "1.0.0");
            Span span = tracer.spanBuilder("GreetingClient").startSpan();
            try (Scope ss = span.makeCurrent()) {
                GreetingServiceGrpc.GreetingServiceBlockingStub stub = GreetingServiceGrpc.newBlockingStub(channel);
                GreetingResponse response = stub.greet(GreetingRequest.newBuilder().setFirstName("Clement").build());

                logger.info("Greeting: " + response.getResult());
            } finally {
                span.end();
            }
    }

    public static void main(String[] args) throws InterruptedException {
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

        ManagedChannel channel = ManagedChannelBuilder
                .forAddress("localhost", 50051)
                .intercept(new LogInterceptor())
                .intercept(new AddHeaderInterceptor())
                .intercept(grpcTelemetry.newClientInterceptor())
                .usePlaintext()
                .build();

        doGreet(channel);

        channel.shutdown();

        // Todo: Find a way to flush Spans before exiting
        logger.info("Wait a little for Span to drain out...");
        Thread.sleep(4000);
        logger.info("Shutting Down");
    }
}
