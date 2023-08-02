package greeting.client;

import io.grpc.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static greeting.server.HeaderCheckInterceptor.CUSTOM_HEADER_KEY;

public final class AddHeaderInterceptor implements ClientInterceptor {
    private static Logger logger = LoggerFactory.getLogger(AddHeaderInterceptor.class);

    @Override
    public <ReqT, RespT> ClientCall<ReqT, RespT> interceptCall(MethodDescriptor<ReqT, RespT> method, CallOptions callOptions, Channel next) {
        return new ForwardingClientCall.SimpleForwardingClientCall<ReqT, RespT>(next.newCall(method, callOptions)) {
            @Override
            public void start(Listener<RespT> responseListener, Metadata headers) {
                headers.put(CUSTOM_HEADER_KEY, "customRequestValue");
                logger.info("Adding headers {}", headers);
                super.start(responseListener, headers);
            }
        };
    }
}
