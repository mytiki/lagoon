package com.mytiki.lagoon.prepare.prepare;

import com.amazonaws.services.lambda.runtime.events.ScheduledEvent;
import com.mytiki.utils.lambda.ApiExceptionBuilder;

import java.util.Map;

public class PrepareReq {
    private final String bucket;
    private final String key;
    private String database;
    private String table;
    private String file;
    private String type;
    private String compression;

    public PrepareReq(ScheduledEvent event) {
        Map<String, Object> s3Object = (Map<String, Object>) event.getDetail().get("object");
        Map<String, Object> s3Bucket = (Map<String, Object>) event.getDetail().get("bucket");
        this.key = (String) s3Object.get("key");
        this.bucket = (String) s3Bucket.get("name");
        parseKey(this.key);
    }

    public PrepareReq(String bucket, String key) {
        this.bucket = bucket;
        this.key = key;
        parseKey(this.key);
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

    public String getCompression() {
        return compression;
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

    private void parseKey(String key) {
        String[] split = key.split("/");
        if (!split[0].equals("prepare")) {
            throw new ApiExceptionBuilder(400)
                    .message("Invalid key")
                    .help("Expected key to start with 'prepare/'")
                    .properties("key", key)
                    .build();
        }
        int len = split.length;
        if (len != 4) {
            throw new ApiExceptionBuilder(400)
                    .message("Invalid key")
                    .help("Expected key to look like 'prepare/[database]/[table]/[file]'")
                    .properties("key", key)
                    .build();
        }
        if (!Character.isLetter(split[1].charAt(0)) || !Character.isLetter(split[2].charAt(0))) {
            throw new ApiExceptionBuilder(400)
                    .message("Invalid key")
                    .detail("Database and table name must start with a letter")
                    .help("Expected key to look like 'prepare/[database]/[table]/[file]'")
                    .properties("key", key)
                    .build();
        }
        this.database = split[1].toLowerCase();
        this.table = split[2].toLowerCase();
        this.file = split[3];

        String[] fileSplit = this.file.split("\\.");
        if (fileSplit.length < 2) {
            this.type = "unknown";
        } else if (fileSplit.length == 2) {
            this.type = fileSplit[1];
        } else {
            this.compression = fileSplit[1];
            this.type = fileSplit[2];
        }
    }

    @Override
    public String toString() {
        return "PrepareReq{" +
                "bucket='" + bucket + '\'' +
                ", key='" + key + '\'' +
                ", database='" + database + '\'' +
                ", table='" + table + '\'' +
                ", file='" + file + '\'' +
                ", type='" + type + '\'' +
                '}';
    }
}
