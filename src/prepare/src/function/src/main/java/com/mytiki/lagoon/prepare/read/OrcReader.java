package com.mytiki.lagoon.prepare.read;

import com.mytiki.utils.lambda.Initialize;
import org.apache.logging.log4j.Logger;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;

import java.net.URI;
import java.util.List;

public class OrcReader extends GenericSparkReader {
    @Override
    public List<URI> read() {
        logger.debug("Reading ORC file from {}/{}", bucket, key);
        Dataset<Row> df = spark.read()
                .option("header", "true")
                .orc(String.format("s3a://%s/%s", bucket, key));
        return convert(df);
    }
}
