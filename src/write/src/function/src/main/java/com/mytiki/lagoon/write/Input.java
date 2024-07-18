package com.mytiki.lagoon.write;

import com.mytiki.utils.lambda.ApiExceptionBuilder;
import com.mytiki.utils.lambda.Initialize;
import org.apache.hadoop.conf.Configuration;
import org.apache.iceberg.Schema;
import org.apache.iceberg.avro.AvroSchemaUtil;
import org.apache.iceberg.data.Record;
import org.apache.iceberg.io.CloseableIterable;
import org.apache.iceberg.parquet.Parquet;
import org.apache.logging.log4j.Logger;
import org.apache.parquet.avro.AvroSchemaConverter;
import org.apache.parquet.hadoop.ParquetFileReader;
import org.apache.parquet.hadoop.metadata.ParquetMetadata;
import org.apache.parquet.schema.MessageType;

import java.io.IOException;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public class Input {
    private static final Logger logger = Initialize.logger(Input.class);
    private final long count;
    private final long length;
    private final Schema schema;
    private final org.apache.iceberg.hadoop.HadoopInputFile iceberg;

    public Input(
            org.apache.parquet.hadoop.util.HadoopInputFile parquet,
            org.apache.iceberg.hadoop.HadoopInputFile iceberg) {
        this.length = parquet.getLength();
        this.iceberg = iceberg;
        try (ParquetFileReader reader = ParquetFileReader.open(parquet)) {
            Configuration configuration = new Configuration();
            configuration.set("parquet.avro.readInt96AsFixed", "true");
            AvroSchemaConverter converter = new AvroSchemaConverter(configuration);
            ParquetMetadata footer = reader.getFooter();
            MessageType parquetSchema = footer.getFileMetaData().getSchema();
            this.schema = AvroSchemaUtil.toIceberg(converter.convert(parquetSchema));
            this.count = reader.getRecordCount();
        } catch (IOException e) {
            String path = parquet.getPath().toString();
            logger.error("failed to read file: {}", path);
            throw new ApiExceptionBuilder(403)
                    .message("Forbidden")
                    .properties("path", path)
                    .build();
        }
        logger.debug("Input: {}", parquet.getPath().toString());
    }

    public CloseableIterable<Record> transform(Function<Record, Record> tfn) {
        CloseableIterable<Record> records = records();
        Stream<Record> stream = StreamSupport.stream(records.spliterator(), false);
        List<Record> response = stream.map(tfn).collect(Collectors.toList());
        return CloseableIterable.withNoopClose(response);
    }

    public CloseableIterable<Record> records() {
        return Parquet.read(iceberg).project(this.schema).build();
    }

    public long getCount() {
        return count;
    }

    public long getLength() {
        return length;
    }

    public Schema getSchema() {
        return schema;
    }
}
