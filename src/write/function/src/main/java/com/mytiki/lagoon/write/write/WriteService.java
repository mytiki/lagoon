package com.mytiki.lagoon.write.write;

import com.mytiki.lagoon.write.utils.IcebergFacade;
import com.mytiki.lagoon.write.utils.StorageFacade;
import com.mytiki.utils.lambda.ApiExceptionBuilder;
import com.mytiki.utils.lambda.Initialize;
import org.apache.iceberg.*;
import org.apache.iceberg.catalog.Namespace;
import org.apache.iceberg.catalog.TableIdentifier;
import org.apache.iceberg.types.Types;
import org.apache.logging.log4j.Logger;
import org.apache.parquet.hadoop.ParquetFileReader;
import org.apache.parquet.hadoop.util.HadoopInputFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class WriteService {
    protected static final Logger logger = Initialize.logger(WriteService.class);
    private final IcebergFacade iceberg;
    private final StorageFacade storage;

    public WriteService(IcebergFacade iceberg, StorageFacade storage) {
        this.iceberg = iceberg;
        this.storage = storage;
    }

    public void write(WriteReq req) {
        if(!req.getType().equals("parquet"))
            throw new ApiExceptionBuilder(404)
                    .message("Bad Request")
                    .detail("Unsupported file type")
                    .properties("path", req.getPath())
                    .build();

        Namespace database = iceberg.getDatabaseSafe(req.getDatabase());
        TableIdentifier tableId = TableIdentifier.of(database, req.getTable());

        String tableKey = String.format("%s/%s/%s", "stg", req.getDatabase(), req.getTable());
        String tableLocation = String.format("s3a://%s/%s", req.getBucket(), tableKey);
        String dataKey = String.format("%s/data/%s.parquet", tableKey, UUID.randomUUID());
        String dataLocation = String.format("s3://%s/%s", req.getBucket(), dataKey);

        HadoopInputFile file = storage.openFile(req.getPath());
        try (ParquetFileReader reader = ParquetFileReader.open(file)) {
            if(!iceberg.tableExists(tableId)) {
                logger.debug("creating table: {}", tableId);
                Schema inferred = storage.readSchema(reader);
                List<Types.NestedField> fields = new ArrayList<>(inferred.columns());
                fields.add(Types.NestedField.optional(
                        fields.size() + 1, IcebergFacade.ETL_LOADED_AT, Types.TimestampType.withoutZone()));
                Schema schema = new Schema(fields);
                PartitionSpec partition = PartitionSpec.builderFor(schema)
                        .day(IcebergFacade.ETL_LOADED_AT)
                        .build();
                iceberg.createTable(tableId, schema, partition, tableLocation, IcebergFacade.CREATE_PROPERTIES);
            }
            logger.debug("updating table: {}", tableId);
            Table table = iceberg.loadTable(tableId);
            Transaction transaction = table.newTransaction();
            DataFile dataFile = DataFiles.builder(table.spec())
                    .withPath(dataLocation)
                    .withFormat(FileFormat.PARQUET)
                    .withFileSizeInBytes(file.getLength())
                    .withRecordCount(reader.getRecordCount())
                    .build();
            transaction.newAppend().appendFile(dataFile).commit();
            transaction.commitTransaction();
        }catch (IOException e) {
            logger.error("failed to read file: {}", req.getPath());
            throw new ApiExceptionBuilder(403)
                    .message("Forbidden")
                    .properties("path", req.getS3Path())
                    .build();
        }
    }
}
