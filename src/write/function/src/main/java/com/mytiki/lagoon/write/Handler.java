/*
 * Copyright (c) TIKI Inc.
 * MIT license. See LICENSE file in root directory.
 */

package com.mytiki.lagoon.write;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.SQSBatchResponse;
import com.amazonaws.services.lambda.runtime.events.SQSEvent;
import com.amazonaws.services.lambda.runtime.events.models.s3.S3EventNotification;
import com.amazonaws.services.lambda.runtime.serialization.PojoSerializer;
import com.amazonaws.services.lambda.runtime.serialization.events.LambdaEventSerializers;
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
    private final IcebergFacade iceberg = IcebergFacade.load();
    private final StorageFacade storage = StorageFacade.dflt();
    private final PojoSerializer<S3EventNotification> serializer =
            LambdaEventSerializers.serializerFor(S3EventNotification.class, ClassLoader.getSystemClassLoader());
    private final List<SQSBatchResponse.BatchItemFailure> failures = new ArrayList<>();

    public SQSBatchResponse handleRequest(final SQSEvent event, final Context context) {
        try {
            iceberg.initialize();
            WriteService writeService = new WriteService(iceberg, storage);
            event.getRecords().forEach(ev -> {
                try {
                    S3EventNotification s3Event = serializer.fromJson(ev.getBody());
                    s3Event.getRecords().forEach(record -> {
                        WriteReq req = new WriteReq(record);
                        try{ writeService.write(req); } catch (Exception ex){ reportFailure(ev, ex); }
                    });
                }catch (Exception ex){ reportFailure(ev, ex); }
            });
        } catch (Exception ex) {reportFailure(event, ex); }
        finally { iceberg.close(); }
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
