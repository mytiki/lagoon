package com.mytiki.lagoon.prepare.utils;

import com.mytiki.utils.lambda.Initialize;
import org.apache.logging.log4j.Logger;
import software.amazon.awssdk.auth.credentials.AwsCredentialsProvider;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.regions.providers.DefaultAwsRegionProviderChain;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.CopyObjectRequest;
import software.amazon.awssdk.services.s3.model.CopyObjectResponse;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.DeleteObjectResponse;

public class S3Facade {
    protected static final Logger logger = Initialize.logger(S3Facade.class);
    private static final String TRASH_PREFIX = "trash";
    private final S3Client s3;

    public S3Facade() {
        this(DefaultCredentialsProvider.builder().build());
    }

    public S3Facade(S3Client s3) {
        this.s3 = s3;
    }

    public S3Facade(AwsCredentialsProvider provider) {
        this(S3Client.builder()
                .credentialsProvider(provider)
                .region(DefaultAwsRegionProviderChain.builder().build().getRegion())
                .build());
    }

    public void trash(String bucket, String key) {
        logger.debug("trash file: {}/{}", bucket, key);
        CopyObjectRequest copyReq = CopyObjectRequest.builder()
                .sourceBucket(bucket)
                .sourceKey(key)
                .destinationBucket(bucket)
                .destinationKey(String.format("%s/%s", TRASH_PREFIX, key))
                .build();
        CopyObjectResponse copyRsp = s3.copyObject(copyReq);
        logger.debug("trash file: {}", copyRsp);
        DeleteObjectRequest deleteReq = DeleteObjectRequest.builder()
                .bucket(bucket)
                .key(key)
                .build();
        DeleteObjectResponse deleteRsp = s3.deleteObject(deleteReq);
        logger.debug("trash file: {}", deleteRsp);
    }

    public void moveFile(String bucket, String src, String dest) {
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
}
