package com.mytiki.lagoon.prepare.read;

import com.mytiki.utils.lambda.Initialize;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.net.URI;
import java.util.List;

public class ParquetReader implements GenericReader {
    protected static final Logger logger = Initialize.logger(ParquetReader.class);
    private String bucket;
    private String key;

    @Override
    public GenericReader open(String bucket, String key) {
        this.bucket = bucket;
        this.key = key;
        return this;
    }

    @Override
    public List<URI> read() {
        URI uri = URI.create(String.format("s3a://%s/%s", bucket, key));
        logger.debug("Reading Parquet file from {}", uri);
        return List.of(uri);
    }

    @Override
    public void close() throws IOException {
    }
}
