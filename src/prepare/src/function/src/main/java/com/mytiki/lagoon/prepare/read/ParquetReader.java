package com.mytiki.lagoon.prepare.read;

import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;

import java.net.URI;
import java.util.List;

public class ParquetReader extends GenericSparkReader {
    @Override
    public List<URI> read() {
        logger.debug("Reading Parquet file from {}/{}", bucket, key);
        Dataset<Row> df = spark.read()
                .option("header", "true")
                .parquet(String.format("s3a://%s/%s", bucket, key));
        return convert(df);
    }
}
