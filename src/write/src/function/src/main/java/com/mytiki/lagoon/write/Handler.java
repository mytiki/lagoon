/*
 * Copyright (c) TIKI Inc.
 * MIT license. See LICENSE file in root directory.
 */

package com.mytiki.lagoon.write;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.SQSBatchResponse;
import com.amazonaws.services.lambda.runtime.events.SQSEvent;
import com.amazonaws.services.lambda.runtime.events.ScheduledEvent;
import com.amazonaws.services.lambda.runtime.serialization.PojoSerializer;
import com.amazonaws.services.lambda.runtime.serialization.events.LambdaEventSerializers;
import com.mytiki.utils.lambda.Initialize;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.List;


public class Handler implements RequestHandler<SQSEvent, SQSBatchResponse> {
    protected static final Logger logger = Initialize.logger(Handler.class);
    private final PojoSerializer<ScheduledEvent> serializer = LambdaEventSerializers.serializerFor
            (ScheduledEvent.class, Thread.currentThread().getContextClassLoader());
    private final List<SQSBatchResponse.BatchItemFailure> failures = new ArrayList<>();
    private final Catalog catalog;
    private final Storage storage;

    public Handler() {
        this(Catalog.load(), Storage.dflt());
    }

    public Handler(Catalog catalog, Storage storage) {
        this.catalog = catalog.initialize();
        this.storage = storage;
        logger.debug("Handler: Initialized");
    }

    public SQSBatchResponse handleRequest(final SQSEvent event, final Context context) {
        logger.debug("SQSEvent: {}", event);
        try {
            Write write = new Write(catalog, storage);
            event.getRecords().forEach(msg -> {
                try {
                    ScheduledEvent scheduledEvent = serializer.fromJson(msg.getBody());
                    Request req = Request.fromEvent(scheduledEvent);
                    write.request(req);
                    logger.debug("Complete: {}", req.toString());
                } catch (Warn ex) {
                    logger.debug(String.format("Skip message: %s", msg), ex);
                } catch (Exception ex) {
                    reportFailure(msg, ex);
                }
            });
        } catch (Exception ex) {
            reportFailure(event, ex);
        }
        catalog.close();
        return SQSBatchResponse.builder()
                .withBatchItemFailures(failures)
                .build();
    }

    private void reportFailure(SQSEvent event, Exception exception) {
        logger.error(String.format("Event: %s", event), exception);
        event.getRecords().forEach(ev -> failures.add(new SQSBatchResponse.BatchItemFailure(ev.getMessageId())));
    }

    private void reportFailure(SQSEvent.SQSMessage message, Exception exception) {
        logger.error(String.format("Message: %s", message), exception);
        failures.add(new SQSBatchResponse.BatchItemFailure(message.getMessageId()));
    }
}
