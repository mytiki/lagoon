package com.mytiki.lagoon.write.fixture;

import org.apache.avro.Schema;
import org.apache.avro.generic.GenericData;
import org.apache.avro.generic.GenericRecord;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.iceberg.avro.AvroSchemaUtil;
import org.apache.parquet.avro.AvroParquetWriter;
import org.apache.parquet.hadoop.ParquetFileWriter;
import org.apache.parquet.hadoop.ParquetWriter;
import org.apache.parquet.hadoop.util.HadoopInputFile;

import java.io.IOException;
import java.nio.file.Files;

public class FixtureParquet {
    private static final String SCHEMA_STR = "{\"type\":\"record\",\"name\":\"Test\",\"fields\":[{\"name\":\"field1\",\"type\":\"string\"}]}";

    public static org.apache.iceberg.Schema schema() {
        Schema schema = new Schema.Parser().parse(SCHEMA_STR);
        return AvroSchemaUtil.toIceberg(schema);
    }

    public static HadoopInputFile inputFile() throws IOException {
        Schema schema = new Schema.Parser().parse(SCHEMA_STR);
        String filename = String.format("hadoop_input_file_%s", System.currentTimeMillis());
        java.nio.file.Path tempPath = Files.createTempFile(filename, ".parquet");
        Path hadoopPath = new Path(tempPath.toString());
        ParquetWriter<GenericRecord> writer = AvroParquetWriter
                .<GenericRecord>builder(hadoopPath)
                .withSchema(schema)
                .withWriteMode(ParquetFileWriter.Mode.OVERWRITE)
                .build();
        GenericRecord record = new GenericData.Record(schema);
        record.put("field1", "test data");
        writer.write(record);
        writer.close();
        Configuration configuration = new Configuration();
        return HadoopInputFile.fromPath(hadoopPath, configuration);
    }
}
