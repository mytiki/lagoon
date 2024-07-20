package com.mytiki.lagoon.write;

import com.mytiki.utils.lambda.Initialize;
import org.apache.hadoop.conf.Configuration;
import org.apache.iceberg.Schema;
import org.apache.iceberg.avro.AvroSchemaUtil;
import org.apache.iceberg.data.Record;
import org.apache.iceberg.data.parquet.GenericParquetReaders;
import org.apache.iceberg.io.CloseableIterable;
import org.apache.iceberg.parquet.Parquet;
import org.apache.logging.log4j.Logger;
import org.apache.parquet.avro.AvroSchemaConverter;
import org.apache.parquet.hadoop.ParquetFileReader;
import org.apache.parquet.hadoop.metadata.ParquetMetadata;
import org.apache.parquet.schema.MessageType;

import java.io.IOException;

public class Input {
    private static final Logger logger = Initialize.logger(Input.class);
    private final org.apache.parquet.hadoop.util.HadoopInputFile parquet;
    private final org.apache.iceberg.hadoop.HadoopInputFile iceberg;

    public Input(
            org.apache.parquet.hadoop.util.HadoopInputFile parquet,
            org.apache.iceberg.hadoop.HadoopInputFile iceberg) {
        this.parquet = parquet;
        this.iceberg = iceberg;
        logger.debug("Inputs: {} | {}", parquet.getPath().toString(), iceberg.getPath().toString());
    }

    public CloseableIterable<Record> records(Schema schema) {
        return Parquet.read(iceberg)
                .project(schema)
                .createReaderFunc(message -> GenericParquetReaders.buildReader(schema, message))
                .build();
    }

    public Schema schema() {
        try (ParquetFileReader reader = ParquetFileReader.open(parquet)) {
            Configuration configuration = new Configuration();
            configuration.set("parquet.avro.readInt96AsFixed", "true");
            AvroSchemaConverter converter = new AvroSchemaConverter(configuration);
            ParquetMetadata footer = reader.getFooter();
            MessageType parquetSchema = footer.getFileMetaData().getSchema();
            return AvroSchemaUtil.toIceberg(converter.convert(parquetSchema));
        } catch (IOException e) {
            throw new Warn(parquet.getPath().toString(), "failed to read file", e);
        }
    }
}
