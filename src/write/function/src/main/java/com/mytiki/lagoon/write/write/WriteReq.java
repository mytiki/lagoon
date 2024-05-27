package com.mytiki.lagoon.write.write;

import com.amazonaws.services.lambda.runtime.events.models.s3.S3EventNotification;
import com.mytiki.utils.lambda.ApiExceptionBuilder;

public class WriteReq {
    private final String bucket;
    private final String key;
    private String database;
    private String table;
    private String file;
    private String type;

    public WriteReq(S3EventNotification.S3EventNotificationRecord record) {
        this.key = record.getS3().getObject().getKey();
        this.bucket = record.getS3().getBucket().getName();
        parseKey(this.key);
    }

    public WriteReq(String bucket, String key) {
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

    public String getPath() {
        return String.format("%s/%s", bucket, key);
    }

    public String getS3Path() {
        return String.format("s3://%s/%s", bucket, key);
    }

    public String getS3aPath() {
        return String.format("s3a://%s/%s", bucket, key);
    }

    private String parseType(String file) {
        String[] split = this.file.split("\\.");
        return split.length < 2 ? "unknown" : split[1];
    }

    private void parseKey(String key) {
        String[] split = key.split("/");
        if(!split[0].equals("load")) {
            throw new ApiExceptionBuilder(400)
                    .message("Invalid key")
                    .help("Expected key to start with 'load/'")
                    .properties("key", key)
                    .build();
        }
        int len = split.length;
        if(len != 4) {
            throw new ApiExceptionBuilder(400)
                    .message("Invalid key")
                    .help("Expected key to look like 'load/[database]/[table]/[file]'")
                    .properties("key", key)
                    .build();
        }
        if(!Character.isLetter(split[1].charAt(0)) || !Character.isLetter(split[2].charAt(0))) {
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
    }
}
