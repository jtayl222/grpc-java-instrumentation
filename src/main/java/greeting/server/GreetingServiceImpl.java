package greeting.server;

import com.proto.greeting.GreetingRequest;
import com.proto.greeting.GreetingResponse;
import com.proto.greeting.GreetingServiceGrpc;
import io.grpc.stub.StreamObserver;
import io.opentelemetry.api.GlobalOpenTelemetry;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.context.Scope;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import utils.Sleeper;

public final class GreetingServiceImpl extends GreetingServiceGrpc.GreetingServiceImplBase {
    private static Logger logger = LoggerFactory.getLogger(GreetingServiceImpl.class);

    final Sleeper sleeper;

    GreetingServiceImpl(Sleeper sleeper) {
        this.sleeper = sleeper;
    }

    @Override
    public void greet(GreetingRequest request, StreamObserver<GreetingResponse> responseObserver) {
        Tracer tracer = GlobalOpenTelemetry.getTracer("Greeter", "1.0.0");
        Span span = tracer.spanBuilder("greet.server").startSpan();
        try (Scope ss = span.makeCurrent()) {
            responseObserver.onNext(GreetingResponse.newBuilder().setResult("Hello " + request.getFirstName()).build());
            responseObserver.onCompleted();
        } finally {
            span.end();
        }
    }
}
