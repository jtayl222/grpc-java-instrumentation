package greeting.server;

import io.grpc.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class LogInterceptor implements ServerInterceptor {
    private static Logger logger = LoggerFactory.getLogger(GreetingServer.class);

    @Override
    public <ReqT, RespT> ServerCall.Listener<ReqT> interceptCall(ServerCall<ReqT, RespT> call, Metadata headers, ServerCallHandler<ReqT, RespT> next) {
        return new ForwardingServerCallListener.SimpleForwardingServerCallListener<ReqT>(next.startCall(call, headers)) {
            @Override
            public void onMessage(ReqT message) {
                logger.info("Receive a message");
                logger.info(message.toString());

                logger.info("With headers");
                logger.info(headers.toString());
                super.onMessage(message);
            }
        };
    }
}
