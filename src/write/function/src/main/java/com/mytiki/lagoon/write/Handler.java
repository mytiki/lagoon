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
import com.mytiki.lagoon.write.utils.AthenaFacade;
import com.mytiki.lagoon.write.utils.IcebergFacade;
import com.mytiki.lagoon.write.utils.StorageFacade;
import com.mytiki.lagoon.write.write.WriteReq;
import com.mytiki.lagoon.write.write.WriteService;
import com.mytiki.utils.lambda.Initialize;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.List;


public class Handler implements RequestHandler<SQSEvent, SQSBatchResponse> {
    protected static final Logger logger = Initialize.logger(Handler.class);
    private final PojoSerializer<ScheduledEvent> serializer = LambdaEventSerializers.serializerFor
            (ScheduledEvent.class, Thread.currentThread().getContextClassLoader());
    private final List<SQSBatchResponse.BatchItemFailure> failures = new ArrayList<>();
    private final IcebergFacade iceberg;
    private final StorageFacade storage;
    private final AthenaFacade athena;

    public Handler() { this(IcebergFacade.load(), StorageFacade.dflt(), AthenaFacade.dflt()); }

    public Handler(IcebergFacade iceberg, StorageFacade storage, AthenaFacade athena) {
        this.iceberg = iceberg.initialize();
        this.storage = storage;
        this.athena = athena;
    }

    public SQSBatchResponse handleRequest(final SQSEvent event, final Context context) {
        try {
            WriteService writeService = new WriteService(iceberg, storage, athena);
            event.getRecords().forEach(msg -> {
                try {
                    ScheduledEvent scheduledEvent = serializer.fromJson(msg.getBody());
                    WriteReq req = new WriteReq(scheduledEvent);
                    try{ writeService.write(req); } catch (Exception ex){ reportFailure(msg, ex); }
                }catch (Exception ex){ reportFailure(msg, ex); }
            });
        } catch (Exception ex) {reportFailure(event, ex); }
        iceberg.close();
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
