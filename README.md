# Propagate OpenTelemetry Contexts using manual instrumentation in a Java/gRCP application

I am working with an application that is implemented using Java, a microservices architecture, and a gRPC communication layer. For observability, we have instrumented server and clients using OpenTelemetry. We started with automatic instrumentation but pivoted to manual instrumentation to gain finer control of the instrumentation.

During transactions, the OpenTelemetry Context needs to be propagated from the client to the server. The OpenTelemetry examples focus on RestAPI calls where the W3C Trace Context HTTP headers are injected into the HttpURLConnection transportLayer. However, in a Java/gRPC application, the Context should **not** be propagated using TextMapPropagator where the carrier of propagated data on both the client (injector) and server (extractor) side is usually an http request.

Instead, the Context should be propagated using gRPC interceptors which allow the application to interact with protobuf messages before or after they sent or received by the client or server.

Client:

```java
        ManagedChannel channel = ManagedChannelBuilder
                .forAddress("localhost", 50051)
                .intercept(grpcTelemetry.newClientInterceptor())
                .build();
```

Server:
```java
        Server server = ServerBuilder.forPort(port)
            .intercept(grpcTelemetry.newServerInterceptor())
            .build();
```

References:

- https://github.com/open-telemetry/opentelemetry-java-instrumentation/tree/main/instrumentation/grpc-1.6/library#library-instrumentation-for-grpc-160

- Example using HttpURLConnection transportLayer: https://opentelemetry.io/docs/instrumentation/java/manual/#context-propagation If we are going to use this approach for gRPC calls, what should be used for Carrier in Interface TextMapPropagator?

- Trace Context W3C Recommendation: https://www.w3.org/TR/trace-context

This Greeting Client/Server application is based on https://github.com/Clement-Jean/grpc-java-course

Thanks to trask for helping me to find Library Instrumentation for gRPC 1.6.0+
