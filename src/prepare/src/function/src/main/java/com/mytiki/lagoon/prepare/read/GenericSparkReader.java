package com.mytiki.lagoon.prepare.read;

import com.mytiki.utils.lambda.ApiExceptionBuilder;
import org.apache.hadoop.fs.FileStatus;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.spark.SparkConf;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SparkSession;
import software.amazon.awssdk.auth.credentials.AwsCredentialsProvider;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;

import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public abstract class GenericSparkReader implements GenericReader {
    private final SparkConf config;
    protected SparkSession spark;
    protected String bucket;
    protected String key;

    public GenericSparkReader() { this(DefaultCredentialsProvider.builder().build()); }
    public GenericSparkReader(SparkConf config) { this.config = config; }
    public GenericSparkReader(AwsCredentialsProvider provider) {
        this.config = new SparkConf()
                .set("fs.s3a.aws.credentials.provider", provider.getClass().getName())
                .set("spark.sql.parquet.compression.codec", "uncompressed")
                .set("spark.sql.files.maxPartitionBytes", Long.toString(128 * 1024 * 1024));
    }

    @Override
    public GenericReader open(String bucket, String key) {
        this.spark = SparkSession.builder()
                .appName("GenericSparkReader-" + UUID.randomUUID())
                .master("local")
                .config(config)
                .getOrCreate();
        this.bucket = bucket;
        this.key = key;
        return this;
    }

    @Override
    public void close() throws IOException { this.spark.close(); }

    protected List<URI> convert(Dataset<Row> dataset) {
        return convert(URI.create(String.format("s3a://%s/tmp/prepare/%s", bucket, UUID.randomUUID())), dataset);
    }

    protected List<URI> convert(URI location, Dataset<Row> dataset) {
        dataset.write().parquet(location.toString());
        List<URI> parts = new ArrayList<>();
        try {
            FileSystem fs = FileSystem.get(location, spark.sparkContext().hadoopConfiguration());
            FileStatus[] fileStatuses = fs.listStatus(new Path(location));
            for (FileStatus fileStatus : fileStatuses) {
                if (fileStatus.getPath().getName().startsWith("part")) {
                    parts.add(fileStatus.getPath().toUri());
                }
            }
        } catch (Exception e) {
            throw new ApiExceptionBuilder(424)
                    .message("Failed to list parquet files")
                    .cause(e)
                    .build();
        }
        return parts;
    }
}
