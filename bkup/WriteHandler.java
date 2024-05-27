/*
 * Copyright (c) TIKI Inc.
 * MIT license. See LICENSE file in root directory.
 */

package com.mytiki.lagoon.write.write;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.SQSBatchResponse;
import com.amazonaws.services.lambda.runtime.events.SQSEvent;
import com.mytiki.lagoon.write.utils.Iceberg;
import com.mytiki.utils.lambda.Initialize;
import com.mytiki.utils.lambda.Mapper;
import org.apache.iceberg.*;
import org.apache.iceberg.catalog.TableIdentifier;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.List;

public class WriteHandler implements RequestHandler<SQSEvent, SQSBatchResponse> {
    protected static final Logger logger = Initialize.logger(WriteHandler.class);
    private final Mapper mapper = new Mapper();
    private final Iceberg iceberg;

    public WriteHandler(Iceberg iceberg) {
        this.iceberg = iceberg;
    }

    @Override
    public SQSBatchResponse handleRequest(SQSEvent event, Context context) {
        List<SQSBatchResponse.BatchItemFailure> failures = new ArrayList<>();
        WriteReq first = mapper.readValue(event.getRecords().get(0).getBody(), WriteReq.class);
        TableIdentifier identifier = TableIdentifier.of(iceberg.getDatabase(), first.getTable());
        Table table = iceberg.loadTable(identifier);
        PartitionSpec spec = table.spec();
        Transaction txn = table.newTransaction();
        AppendFiles append = txn.newAppend();

        event.getRecords().forEach(ev -> {
            try {
                WriteReq req = mapper.readValue(ev.getBody(), WriteReq.class);
                append.appendFile(DataFiles.builder(spec)
                        .withPath(req.getUri())
                        .withFormat(FileFormat.PARQUET)
                        .withFileSizeInBytes(req.getSize())
                        .withRecordCount(req.getCount())
                        .build());
            } catch (Exception ex) {
                logger.error(ex, ex.fillInStackTrace());
                failures.add(new SQSBatchResponse.BatchItemFailure(ev.getMessageId()));
            }
        });

        append.commit();
        txn.commitTransaction();
        iceberg.close();
        return SQSBatchResponse.builder()
                .withBatchItemFailures(failures)
                .build();
    }
}
