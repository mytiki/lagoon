package com.mytiki.lagoon.write.utils;

import com.mytiki.lagoon.write.write.WriteReq;
import com.mytiki.utils.lambda.Initialize;
import org.apache.hadoop.conf.Configuration;
import org.apache.logging.log4j.Logger;
import software.amazon.awssdk.auth.credentials.AwsCredentialsProvider;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.regions.providers.DefaultAwsRegionProviderChain;
import software.amazon.awssdk.services.athena.AthenaClient;
import software.amazon.awssdk.services.athena.model.QueryExecutionContext;
import software.amazon.awssdk.services.athena.model.ResultConfiguration;
import software.amazon.awssdk.services.athena.model.StartQueryExecutionRequest;
import software.amazon.awssdk.services.athena.model.StartQueryExecutionResponse;
import software.amazon.awssdk.services.s3.S3Client;

public class AthenaFacade {
    protected static final Logger logger = Initialize.logger(StorageFacade.class);
    private final AthenaClient athena;
    private static final String CATALOG = "AwsDataCatalog";
    private static final String WORKGROUP = "lagoon";

    public AthenaFacade(AthenaClient athena) {
        this.athena = athena;
    }

    public static AthenaFacade dflt() {
        AwsCredentialsProvider provider = DefaultCredentialsProvider.builder().build();
        return AthenaFacade.withProvider(provider);
    }

    public static AthenaFacade withProvider(AwsCredentialsProvider provider) {
        AthenaClient athena = AthenaClient.builder()
                .credentialsProvider(provider)
                .region(DefaultAwsRegionProviderChain.builder().build().getRegion())
                .build();
        return new AthenaFacade(athena);
    }

    public String setEtlLoadedAt(WriteReq req) {
        String query = String.format(
                "UPDATE %1$s SET %2$s = now() WHERE %2$s IS NULL",
                req.getTable(),
                IcebergFacade.ETL_LOADED_AT);
        StartQueryExecutionRequest execReq = StartQueryExecutionRequest.builder()
                .queryString(query)
                .workGroup(WORKGROUP)
                .queryExecutionContext(QueryExecutionContext.builder()
                        .catalog(CATALOG)
                        .database(req.getDatabase())
                        .build())
                .build();
        StartQueryExecutionResponse execRsp = athena.startQueryExecution(execReq);
        String execId = execRsp.queryExecutionId();
        logger.info("setEtlLoadedAt execution id: {}", execId);
        return execId;
    }
}
