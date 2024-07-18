package com.mytiki.lagoon.write;

import com.mytiki.utils.lambda.ApiExceptionBuilder;
import com.mytiki.utils.lambda.Initialize;
import org.apache.iceberg.*;
import org.apache.iceberg.catalog.Namespace;
import org.apache.iceberg.catalog.TableIdentifier;
import org.apache.iceberg.data.Record;
import org.apache.iceberg.data.parquet.GenericParquetWriter;
import org.apache.iceberg.io.CloseableIterable;
import org.apache.iceberg.io.FileAppender;
import org.apache.iceberg.io.FileIO;
import org.apache.iceberg.io.OutputFile;
import org.apache.iceberg.parquet.Parquet;
import org.apache.iceberg.types.Types;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.function.Function;

public class Write {
    protected static final Logger logger = Initialize.logger(Write.class);
    private final Storage storage;
    private final Catalog catalog;

    public Write(Storage storage, Catalog catalog) {
        this.storage = storage;
        this.catalog = catalog;
    }

    public void request(Request req) {
        if (!req.getType().equals("parquet"))
            throw new ApiExceptionBuilder(404)
                    .message("Bad Request")
                    .detail("Unsupported file type")
                    .properties("path", req.getPath())
                    .build();

        Namespace database = catalog.getDatabaseSafe(req.getDatabase());
        TableIdentifier tableId = TableIdentifier.of(database, req.getTable());
        String tableKey = String.format("%s/%s/%s", "stg", req.getDatabase(), req.getTable());

        Input input = storage.read(req.getBucket(), req.getKey());
        if (!catalog.tableExists(tableId))
            createTable(tableId, input, String.format("s3a://%s/%s", req.getBucket(), tableKey));
        try (CloseableIterable<Record> records = addEtlLoadedAt(input)) {
            writeData(req.getBucket(), tableId, tableKey, records);
        } catch (IOException e) {
            logger.error("failed to add _etl_loaded_at to: {}", req.getPath());
            throw new ApiExceptionBuilder(404)
                    .message("Bad Request")
                    .detail("Failed to add _etl_loaded_at to")
                    .properties("path", req.getPath())
                    .build();
        }
    }

    private void createTable(TableIdentifier tableId, Input input, String location) {
        logger.debug("Create table: {}", tableId);
        Schema inferred = input.getSchema();
        List<Types.NestedField> fields = new ArrayList<>(inferred.columns());
        fields.add(Types.NestedField.optional(
                fields.size() + 1, Catalog.ETL_LOADED_AT, Types.TimestampType.withoutZone()));
        Schema schema = new Schema(fields);
        PartitionSpec partition = PartitionSpec.builderFor(schema)
                .day(Catalog.ETL_LOADED_AT)
                .build();
        catalog.createTable(tableId, schema, partition, location, Catalog.CREATE_PROPERTIES);
    }

    private CloseableIterable<Record> addEtlLoadedAt(Input input) {
        String now = LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
        Function<Record, Record> setEtlLoadedAt = record -> {
            record.setField(Catalog.ETL_LOADED_AT, now);
            return record;
        };
        return input.transform(setEtlLoadedAt);
    }

    private void writeData(String bucket, TableIdentifier tableId, String tableKey, CloseableIterable<Record> records) {
        Table table = catalog.loadTable(tableId);
        String dataLocation = String.format("s3://%s/%s/data/%s.parquet", bucket, tableKey, UUID.randomUUID());
        try (FileIO io = table.io()) {
            OutputFile outputFile = io.newOutputFile(dataLocation);
            try (FileAppender<Record> appender = Parquet.write(outputFile)
                    .schema(table.schema())
                    .createWriterFunc(GenericParquetWriter::buildWriter)
                    .build()) {
                records.forEach(appender::add);
                AppendFiles append = table.newAppend();
                append.appendFile(DataFiles.builder(table.spec())
                        .withInputFile(io.newInputFile(dataLocation))
                        .withMetrics(appender.metrics())
                        .build());
                append.commit();
            } catch (IOException e) {
                logger.error("failed to append records to: {}", dataLocation);
                throw new ApiExceptionBuilder(404)
                        .message("Bad Request")
                        .detail("Failed to append records")
                        .properties("location", dataLocation)
                        .build();
            }
        }
    }
}
