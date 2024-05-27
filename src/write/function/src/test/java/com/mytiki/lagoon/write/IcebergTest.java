/*
 * Copyright (c) TIKI Inc.
 * MIT license. See LICENSE file in root directory.
 */

package com.mytiki.lagoon.write;
import com.mytiki.lagoon.write.utils.IcebergFacade;
import com.mytiki.utils.lambda.Env;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.HashMap;
import java.util.Map;

public class IcebergTest {
    private static String CATALOG_IMPL = "org.apache.iceberg.aws.glue.GlueCatalog";
    private static String WAREHOUSE = "dummy-warehouse";
    private static String IO_IMPL = "org.apache.iceberg.aws.s3.S3FileIO";
    private static String GLUE_SKIP_ARCHIVE = "true";
    private static String DATABASE_NAME = "dummy-database";
    private static String CATALOG_NAME = "iceberg";

    @Test
    public void Test_Initialize_Success() {
        Map<String, String> config = new HashMap<>(){{
            put(IcebergFacade.CATALOG_NAME, CATALOG_NAME);
            put(IcebergFacade.CATALOG_IMPL, CATALOG_IMPL);
            put(IcebergFacade.WAREHOUSE, WAREHOUSE);
            put(IcebergFacade.IO_IMPL, IO_IMPL);
            put(IcebergFacade.GLUE_SKIP_ARCHIVE, GLUE_SKIP_ARCHIVE);
        }};

        IcebergFacade iceberg = Mockito.spy(new IcebergFacade(config));
        Mockito.doNothing().when(iceberg).initialize(Mockito.any(), Mockito.any());

        iceberg = iceberg.initialize();
        Assertions.assertEquals(WAREHOUSE, iceberg.getWarehouse());
        Assertions.assertEquals(CATALOG_IMPL, iceberg.getCatalogImpl());
        Assertions.assertEquals(IO_IMPL, iceberg.getIoImpl());
        Assertions.assertEquals(CATALOG_NAME, iceberg.getCatalogName());
        Assertions.assertEquals(GLUE_SKIP_ARCHIVE, iceberg.getGlueSkipArchive());
    }

    @Test
    public void Test_Load_No_Env_Success() {
        IcebergFacade iceberg = Mockito.spy(IcebergFacade.load());
        Mockito.doNothing().when(iceberg).initialize(Mockito.any(), Mockito.any());

        iceberg = iceberg.initialize();
        Assertions.assertEquals(WAREHOUSE, iceberg.getWarehouse());
        Assertions.assertEquals(CATALOG_IMPL, iceberg.getCatalogImpl());
        Assertions.assertEquals(IO_IMPL, iceberg.getIoImpl());
        Assertions.assertEquals(CATALOG_NAME, iceberg.getCatalogName());
        Assertions.assertEquals(GLUE_SKIP_ARCHIVE, iceberg.getGlueSkipArchive());
    }

    @Test
    public void Test_Load_Env_Success() {
        String override = "override";
        Env env = Mockito.spy(new Env());
        String envVar = env.name(IcebergFacade.ENV_PREFIX + IcebergFacade.WAREHOUSE);
        Mockito.doReturn(override).when(env).get(envVar);

        IcebergFacade iceberg = Mockito.spy(IcebergFacade.load(env));
        Mockito.doNothing().when(iceberg).initialize(Mockito.any(), Mockito.any());

        iceberg = iceberg.initialize();
        Assertions.assertEquals(override, iceberg.getWarehouse());
        Assertions.assertEquals(CATALOG_IMPL, iceberg.getCatalogImpl());
        Assertions.assertEquals(IO_IMPL, iceberg.getIoImpl());
        Assertions.assertEquals(CATALOG_NAME, iceberg.getCatalogName());
        Assertions.assertEquals(GLUE_SKIP_ARCHIVE, iceberg.getGlueSkipArchive());
    }
}
