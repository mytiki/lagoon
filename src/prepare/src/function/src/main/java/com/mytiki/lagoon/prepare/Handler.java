package com.mytiki.lagoon.prepare;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.SQSBatchResponse;
import com.amazonaws.services.lambda.runtime.events.SQSEvent;
import com.amazonaws.services.lambda.runtime.events.ScheduledEvent;
import com.amazonaws.services.lambda.runtime.serialization.PojoSerializer;
import com.amazonaws.services.lambda.runtime.serialization.events.LambdaEventSerializers;
import com.mytiki.lagoon.prepare.prepare.PrepareReq;
import com.mytiki.lagoon.prepare.prepare.PrepareService;
import com.mytiki.lagoon.prepare.utils.S3Facade;
import com.mytiki.utils.lambda.Initialize;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.List;

public class Handler implements RequestHandler<SQSEvent, SQSBatchResponse> {
    protected static final Logger logger = Initialize.logger(Handler.class);
    private final PojoSerializer<ScheduledEvent> serializer = LambdaEventSerializers.serializerFor
            (ScheduledEvent.class, Thread.currentThread().getContextClassLoader());
    private final List<SQSBatchResponse.BatchItemFailure> failures = new ArrayList<>();
    private final S3Facade s3;

    public Handler(S3Facade s3) {
        this.s3 = s3;
    }

    public Handler() {
        this(new S3Facade());
        logger.debug("Initializing");
    }

    public SQSBatchResponse handleRequest(final SQSEvent event, final Context context) {
        try {
            PrepareService prepareService = new PrepareService(s3);
            logger.debug("SQSEvent: {}", event);
            event.getRecords().forEach(msg -> {
                try {
                    ScheduledEvent scheduledEvent = serializer.fromJson(msg.getBody());
                    PrepareReq req = new PrepareReq(scheduledEvent);
                    try {
                        prepareService.prepare(req);
                    } catch (Exception ex) {
                        reportFailure(msg, ex);
                    }
                } catch (Exception ex) {
                    reportFailure(msg, ex);
                }
            });
        } catch (Exception ex) {
            reportFailure(event, ex);
        }
        return SQSBatchResponse.builder()
                .withBatchItemFailures(failures)
                .build();
    }

    private void reportFailure(SQSEvent event, Exception exception) {
        logger.error(exception, exception.fillInStackTrace());
        event.getRecords().forEach(ev -> failures.add(new SQSBatchResponse.BatchItemFailure(ev.getMessageId())));
    }

    private void reportFailure(SQSEvent.SQSMessage message, Exception exception) {
        logger.error(exception, exception.fillInStackTrace());
        failures.add(new SQSBatchResponse.BatchItemFailure(message.getMessageId()));
    }
}
