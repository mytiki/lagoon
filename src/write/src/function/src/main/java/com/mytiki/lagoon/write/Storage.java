package com.mytiki.lagoon.write;

import com.mytiki.utils.lambda.ApiExceptionBuilder;
import com.mytiki.utils.lambda.Initialize;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.logging.log4j.Logger;
import software.amazon.awssdk.auth.credentials.AwsCredentialsProvider;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.regions.providers.DefaultAwsRegionProviderChain;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.CopyObjectRequest;
import software.amazon.awssdk.services.s3.model.CopyObjectResponse;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.DeleteObjectResponse;

import java.io.IOException;

public class Storage {
    protected static final Logger logger = Initialize.logger(Storage.class);
    private static final String TRASH_PREFIX = "trash";
    private final S3Client s3;
    private final Configuration configuration;

    public Storage(S3Client s3, Configuration configuration) {
        this.s3 = s3;
        this.configuration = configuration;
        logger.debug("Storage: constructed");
    }

    public static Storage withProvider(AwsCredentialsProvider provider) {
        Configuration configuration = new Configuration();
        configuration.set("fs.s3a.aws.credentials.provider", provider.getClass().getName());
        S3Client client = S3Client.builder()
                .credentialsProvider(provider)
                .region(DefaultAwsRegionProviderChain.builder().build().getRegion())
                .build();
        return new Storage(client, configuration);
    }

    public static Storage dflt() {
        AwsCredentialsProvider provider = DefaultCredentialsProvider.builder().build();
        return Storage.withProvider(provider);
    }

    public void move(String bucket, String src, String dest) {
        CopyObjectRequest copyReq = CopyObjectRequest.builder()
                .sourceBucket(bucket)
                .sourceKey(src)
                .destinationBucket(bucket)
                .destinationKey(dest)
                .build();
        CopyObjectResponse copyRsp = s3.copyObject(copyReq);
        logger.debug("copy file: {}", copyRsp);
        trash(bucket, src);
    }

    public void trash(String bucket, String key) {
        logger.debug("Trash: {}/{}", bucket, key);
        CopyObjectRequest copyReq = CopyObjectRequest.builder()
                .sourceBucket(bucket)
                .sourceKey(key)
                .destinationBucket(bucket)
                .destinationKey(String.format("%s/%s", TRASH_PREFIX, key))
                .build();
        CopyObjectResponse copyRsp = s3.copyObject(copyReq);
        logger.debug("Trash: {}", copyRsp);
        DeleteObjectRequest deleteReq = DeleteObjectRequest.builder()
                .bucket(bucket)
                .key(key)
                .build();
        DeleteObjectResponse deleteRsp = s3.deleteObject(deleteReq);
        logger.debug("Trash: {}", deleteRsp);
    }

    public Input read(String bucket, String key) {
        Path path = new Path(String.format("s3a://%s/%s", bucket, key));
        try {
            org.apache.parquet.hadoop.util.HadoopInputFile parquet =
                    org.apache.parquet.hadoop.util.HadoopInputFile.fromPath(path, this.configuration);
            org.apache.iceberg.hadoop.HadoopInputFile iceberg =
                    org.apache.iceberg.hadoop.HadoopInputFile.fromPath(path, this.configuration);
            return new Input(parquet, iceberg);
        } catch (IOException e) {
            logger.error("failed to open file: {}", path.toString());
            throw new ApiExceptionBuilder(403)
                    .message("Forbidden")
                    .properties("path", path.toString())
                    .build();
        }
    }
}
