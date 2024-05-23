/*
 * Copyright (c) TIKI Inc.
 * MIT license. See LICENSE file in root directory.
 */

package com.mytiki.lagoon.write;
import com.mytiki.lagoon.write.utils.Iceberg;
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
            put(Iceberg.CATALOG_NAME, CATALOG_NAME);
            put(Iceberg.CATALOG_IMPL, CATALOG_IMPL);
            put(Iceberg.WAREHOUSE, WAREHOUSE);
            put(Iceberg.IO_IMPL, IO_IMPL);
            put(Iceberg.GLUE_SKIP_ARCHIVE, GLUE_SKIP_ARCHIVE);
            put(Iceberg.DATABASE_NAME, DATABASE_NAME);
        }};

        Iceberg iceberg = Mockito.spy(new Iceberg(config));
        Mockito.doNothing().when(iceberg).initialize(Mockito.any(), Mockito.any());

        iceberg = iceberg.initialize();
        Assertions.assertEquals(WAREHOUSE, iceberg.getWarehouse());
        Assertions.assertEquals(CATALOG_IMPL, iceberg.getCatalogImpl());
        Assertions.assertEquals(IO_IMPL, iceberg.getIoImpl());
        Assertions.assertEquals(CATALOG_NAME, iceberg.getCatalogName());
        Assertions.assertEquals(GLUE_SKIP_ARCHIVE, iceberg.getGlueSkipArchive());
        Assertions.assertEquals(DATABASE_NAME, iceberg.getDatabase().toString());
    }

    @Test
    public void Test_Load_No_Env_Success() {
        Iceberg iceberg = Mockito.spy(Iceberg.load());
        Mockito.doNothing().when(iceberg).initialize(Mockito.any(), Mockito.any());

        iceberg = iceberg.initialize();
        Assertions.assertEquals(WAREHOUSE, iceberg.getWarehouse());
        Assertions.assertEquals(CATALOG_IMPL, iceberg.getCatalogImpl());
        Assertions.assertEquals(IO_IMPL, iceberg.getIoImpl());
        Assertions.assertEquals(CATALOG_NAME, iceberg.getCatalogName());
        Assertions.assertEquals(GLUE_SKIP_ARCHIVE, iceberg.getGlueSkipArchive());
        Assertions.assertEquals(DATABASE_NAME, iceberg.getDatabase().toString());
    }

    @Test
    public void Test_Load_Env_Success() {
        String override = "override";
        Env env = Mockito.spy(new Env());
        String envVar = env.name(Iceberg.ENV_PREFIX + Iceberg.WAREHOUSE);
        Mockito.doReturn(override).when(env).get(envVar);

        Iceberg iceberg = Mockito.spy(Iceberg.load(env));
        Mockito.doNothing().when(iceberg).initialize(Mockito.any(), Mockito.any());

        iceberg = iceberg.initialize();
        Assertions.assertEquals(override, iceberg.getWarehouse());
        Assertions.assertEquals(CATALOG_IMPL, iceberg.getCatalogImpl());
        Assertions.assertEquals(IO_IMPL, iceberg.getIoImpl());
        Assertions.assertEquals(CATALOG_NAME, iceberg.getCatalogName());
        Assertions.assertEquals(GLUE_SKIP_ARCHIVE, iceberg.getGlueSkipArchive());
        Assertions.assertEquals(DATABASE_NAME, iceberg.getDatabase().toString());

    }
}
