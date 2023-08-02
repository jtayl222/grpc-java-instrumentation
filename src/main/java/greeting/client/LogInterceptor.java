package greeting.client;

import io.grpc.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class LogInterceptor implements ClientInterceptor {
    private static Logger logger = LoggerFactory.getLogger(LogInterceptor.class);

    @Override
    public <ReqT, RespT> ClientCall<ReqT, RespT> interceptCall(MethodDescriptor<ReqT, RespT> method, CallOptions callOptions, Channel next) {
        return new ForwardingClientCall.SimpleForwardingClientCall<ReqT, RespT>(next.newCall(method, callOptions)) {
            @Override
            public void sendMessage(ReqT message) {
                logger.info("Send a message");
                logger.info(message.toString());

                logger.info("With call options");
                logger.info(callOptions.toString());
                super.sendMessage(message);
            }
        };
    }
}
