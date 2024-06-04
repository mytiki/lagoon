package com.mytiki.lagoon.prepare.prepare;

import com.mytiki.lagoon.prepare.read.*;
import com.mytiki.lagoon.prepare.utils.S3Facade;
import com.mytiki.utils.lambda.ApiExceptionBuilder;
import com.mytiki.utils.lambda.Initialize;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.net.URI;
import java.util.List;
import java.util.UUID;

public class PrepareService {
    protected static final Logger logger = Initialize.logger(PrepareService.class);
    private final S3Facade s3;

    public PrepareService(S3Facade s3) { this.s3 = s3; }

    public void prepare(PrepareReq req) {
        logger.debug("PrepareReq: {}", req);
        List<URI> files = read(req.getBucket(), req.getKey(), req.getType());
        files.forEach(file -> {
            String[] split = file.getPath().split("/",1);
            String destination = String.format("load/%s/%s/%s.parquet", req.getDatabase(), req.getTable(), UUID.randomUUID());
            s3.moveFile(split[0], split[1], destination);
        });
    }

    private List<URI> read(String bucket, String key, String type) {
        GenericReader reader = null;
        try {
            reader = switch (type) {
                case "csv" -> new CsvReader();
                case "avro" -> new AvroReader();
                case "orc" -> new OrcReader();
                case "json" -> new JsonReader();
                case "parquet" -> new ParquetReader();
                default -> throw new ApiExceptionBuilder(404)
                        .message("Bad Request")
                        .detail("Unsupported file type")
                        .properties("type", type)
                        .build();
            };
            return reader.open(bucket, key).read();
        } finally {
            try { if (reader != null) reader.close(); }
            catch (IOException e) { logger.error(e, e.fillInStackTrace()); }
        }
    }
}
