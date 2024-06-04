package com.mytiki.lagoon.prepare.read;

import java.net.URI;
import java.util.List;

public class ParquetReader extends GenericSparkReader {
    @Override
    public List<URI> read() {
        return List.of(URI.create(String.format("s3a://%s/%s", bucket, key)));
    }
}
