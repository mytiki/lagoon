package com.mytiki.lagoon.write;

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
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class Write {
    protected static final Logger logger = Initialize.logger(Write.class);
    private final Catalog catalog;
    private final Storage storage;

    public Write(Catalog catalog, Storage storage) {
        this.catalog = catalog;
        this.storage = storage;
        this.catalog.initialize();
        logger.debug("Write: constructed");
    }

    public void request(Request req) throws IOException {
        logger.debug("Request: {}", req);
        if (!req.getType().equals("parquet"))
            throw new Warn(req.getKey(), "Unsupported file type. Must be .parquet");
        Input input = storage.read(req.getBucket(), req.getKey());
        Namespace database = catalog.getDatabaseSafe(req.getDatabase());
        TableIdentifier tableId = TableIdentifier.of(database, req.getTable());
        String location = String.format("s3://%s/%s/%s/%s", req.getBucket(), "stg", req.getDatabase(), req.getTable());
        Schema schema = getSchema(input);
        Table table = !catalog.tableExists(tableId) ? createTable(tableId, schema, location) : loadTable(tableId, schema);
        writeData(table, input, location);
        storage.trash(req.getBucket(), req.getKey());
    }

    private Schema getSchema(Input input) {
        logger.debug("Get schema");
        Schema schema = input.schema();
        List<Types.NestedField> fields = new ArrayList<>(schema.columns());
        fields.add(Types.NestedField.optional(
                fields.size() + 1, Catalog.ETL_LOADED_AT, Types.TimestampType.withoutZone()));
        return new Schema(fields);
    }

    private Table createTable(TableIdentifier tableId, Schema schema, String location) {
        logger.debug("Create table: {}", tableId);
        PartitionSpec partition = PartitionSpec.builderFor(schema)
                .hour(Catalog.ETL_LOADED_AT, Catalog.ETL_LOADED_AT_PARTITION)
                .build();
        return catalog.createTable(tableId, schema, partition, location, Catalog.CREATE_PROPERTIES);
    }

    private Table loadTable(TableIdentifier tableId, Schema schema) {
        logger.debug("Load table: {}", tableId);
        Table table = catalog.loadTable(tableId);
        List<String> current = table.schema().columns().stream().map(Types.NestedField::name).toList();
        List<Types.NestedField> input = schema.columns();

        UpdateSchema update = table.updateSchema();
        boolean hasChanges = false;
        for (Types.NestedField col : input) {
            if (!current.contains(col.name())) {
                logger.debug("Add column: {}", col.name());
                update.addColumn(col.name(), col.type()).commit();
                hasChanges = true;
            }
        }
        if (hasChanges) update.commit();
        return table;
    }

    private void writeData(Table table, Input input, String location) throws IOException {
        logger.debug("Write data: {}", location);
        FileIO io = table.io();
        LocalDateTime now = LocalDateTime.now(ZoneOffset.UTC);
        String partition = String.format("%s=%s",
                Catalog.ETL_LOADED_AT_PARTITION,
                now.format(DateTimeFormatter.ofPattern("yyyy-MM-dd-HH")));
        String outputLocation = String.format("%s/data/%s/%s.parquet", location, partition, UUID.randomUUID());
        OutputFile outputFile = io.newOutputFile(outputLocation);
        FileAppender<Record> appender = Parquet.write(outputFile)
                .forTable(table)
                .createWriterFunc(GenericParquetWriter::buildWriter)
                .build();
        try (CloseableIterable<Record> records = input.records(table.schema())) {
            records.forEach(record -> {
                record.setField(Catalog.ETL_LOADED_AT, now);
                appender.add(record);
            });
        }
        appender.close();
        AppendFiles append = table.newAppend();
        append.appendFile(DataFiles.builder(table.spec())
                .withInputFile(outputFile.toInputFile())
                .withMetrics(appender.metrics())
                .build());
        append.commit();
        io.close();
    }
}
