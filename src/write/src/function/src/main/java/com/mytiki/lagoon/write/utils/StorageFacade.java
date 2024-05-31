package com.mytiki.lagoon.write.utils;

import com.mytiki.utils.lambda.ApiExceptionBuilder;
import com.mytiki.utils.lambda.Initialize;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.iceberg.Schema;
import org.apache.iceberg.avro.AvroSchemaUtil;
import org.apache.logging.log4j.Logger;
import org.apache.parquet.avro.AvroSchemaConverter;
import org.apache.parquet.hadoop.ParquetFileReader;
import org.apache.parquet.hadoop.metadata.ParquetMetadata;
import org.apache.parquet.hadoop.util.HadoopInputFile;
import org.apache.parquet.schema.MessageType;
import software.amazon.awssdk.auth.credentials.AwsCredentials;
import software.amazon.awssdk.auth.credentials.AwsCredentialsProvider;
import software.amazon.awssdk.auth.credentials.AwsSessionCredentials;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.regions.providers.DefaultAwsRegionProviderChain;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.CopyObjectRequest;
import software.amazon.awssdk.services.s3.model.CopyObjectResponse;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.DeleteObjectResponse;

import java.io.IOException;

public class StorageFacade {
    protected static final Logger logger = Initialize.logger(StorageFacade.class);
    private final S3Client s3;
    private final Configuration clientConfig;

    public StorageFacade(S3Client s3, Configuration clientConfig) {
        this.s3 = s3;
        this.clientConfig = clientConfig;
        logger.debug("storage initialize");
    }

    public static StorageFacade dflt() {
        AwsCredentialsProvider provider = DefaultCredentialsProvider.builder().build();
        return StorageFacade.withProvider(provider);
    }

    public static StorageFacade withProvider(AwsCredentialsProvider provider) {
        Configuration clientConfig = new Configuration();
        clientConfig.set("fs.s3a.aws.credentials.provider", provider.getClass().getName());
        S3Client client = S3Client.builder()
                .credentialsProvider(provider)
                .region(DefaultAwsRegionProviderChain.builder().build().getRegion())
                .build();
        return new StorageFacade(client, clientConfig);
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
        DeleteObjectRequest deleteReq = DeleteObjectRequest.builder()
                .bucket(bucket)
                .key(src)
                .build();
        DeleteObjectResponse deleteRsp = s3.deleteObject(deleteReq);
        logger.debug("delete file: {}", deleteRsp);
    }

    public HadoopInputFile openFile(String path) {
        try {
            Path s3Path = new Path("s3a://" + path);
            return HadoopInputFile.fromPath(s3Path, clientConfig);
        }catch (IOException e) {
            logger.error("failed to open file: {}", path);
            throw new ApiExceptionBuilder(403)
                    .message("Forbidden")
                    .properties("path", path)
                    .build();
        }
    }

    public Schema readSchema(ParquetFileReader reader) {
        AvroSchemaConverter converter = new AvroSchemaConverter();
        ParquetMetadata footer = reader.getFooter();
        MessageType parquetSchema = footer.getFileMetaData().getSchema();
        return AvroSchemaUtil.toIceberg(converter.convert(parquetSchema));
    }
}
