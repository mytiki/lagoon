package com.mytiki.lagoon.prepare.read;


import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import static org.apache.spark.sql.avro.functions.*;

import java.net.URI;
import java.util.List;

public class AvroReader extends GenericSparkReader {
    @Override
    public List<URI> read() {
        Dataset<Row> df = spark.read()
                .option("header", "true")
                .format("avro")
                .load(String.format("s3a://%s/%s", bucket, key));
        return convert(df);
    }
}
