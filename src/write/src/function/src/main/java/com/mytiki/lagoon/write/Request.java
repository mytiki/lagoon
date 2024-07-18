package com.mytiki.lagoon.write;

import com.amazonaws.services.lambda.runtime.events.ScheduledEvent;
import com.mytiki.utils.lambda.ApiExceptionBuilder;
import com.mytiki.utils.lambda.Initialize;
import org.apache.logging.log4j.Logger;

import java.util.Map;

public class Request {
    private static final Logger logger = Initialize.logger(Request.class);
    private final String bucket;
    private final String key;
    private final String database;
    private final String table;
    private final String file;
    private final String type;

    public Request(String bucket, String key) {
        this.bucket = bucket;
        this.key = key;
        String[] split = key.split("/");
        if (!split[0].equals("load")) {
            throw new ApiExceptionBuilder(400)
                    .message("Invalid key")
                    .help("Expected key to start with 'load/'")
                    .properties("key", key)
                    .build();
        }
        int len = split.length;
        if (len != 4) {
            throw new ApiExceptionBuilder(400)
                    .message("Invalid key")
                    .help("Expected key to look like 'load/[database]/[table]/[file]'")
                    .properties("key", key)
                    .build();
        }
        if (!Character.isLetter(split[1].charAt(0)) || !Character.isLetter(split[2].charAt(0))) {
            throw new ApiExceptionBuilder(400)
                    .message("Invalid key")
                    .detail("Database and table name must start with a letter")
                    .help("Expected key to look like 'load/[database]/[table]/[file]'")
                    .properties("key", key)
                    .build();
        }
        this.database = split[1].toLowerCase();
        this.table = split[2].toLowerCase();
        this.file = split[3];
        String[] fileSplit = this.file.split("\\.");
        this.type = fileSplit.length < 2 ? "unknown" : fileSplit[1];
        logger.debug("Request: '{}/{}'", bucket, key);
    }

    public static Request fromEvent(ScheduledEvent event) {
        Map<String, Object> s3Object = (Map<String, Object>) event.getDetail().get("object");
        Map<String, Object> s3Bucket = (Map<String, Object>) event.getDetail().get("bucket");
        String key = (String) s3Object.get("key");
        String bucket = (String) s3Bucket.get("name");
        return new Request(key, bucket);
    }

    public String getBucket() {
        return bucket;
    }

    public String getDatabase() {
        return database;
    }

    public String getTable() {
        return table;
    }

    public String getFile() {
        return file;
    }

    public String getType() {
        return type;
    }

    public String getKey() {
        return key;
    }

    public String getPath() {
        return String.format("%s/%s", bucket, key);
    }

    public String getS3Path() {
        return String.format("s3://%s/%s", bucket, key);
    }

    public String getS3aPath() {
        return String.format("s3a://%s/%s", bucket, key);
    }

    @Override
    public String toString() {
        return "Request{" +
                "bucket='" + bucket + '\'' +
                ", key='" + key + '\'' +
                ", database='" + database + '\'' +
                ", table='" + table + '\'' +
                ", file='" + file + '\'' +
                ", type='" + type + '\'' +
                '}';
    }
}
