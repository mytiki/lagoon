package com.mytiki.lagoon.prepare.read;

import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;

import java.net.URI;
import java.util.List;

public class JsonReader extends GenericSparkReader {
    @Override
    public List<URI> read() {
        Dataset<Row> df = spark.read()
                .option("header", "true")
                .json(String.format("s3a://%s/%s", bucket, key));
        return convert(df);
    }
}
