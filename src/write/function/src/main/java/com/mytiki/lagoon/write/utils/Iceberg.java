/*
 * Copyright (c) TIKI Inc.
 * MIT license. See LICENSE file in root directory.
 */

package com.mytiki.lagoon.write.utils;

import com.mytiki.utils.lambda.Env;
import com.mytiki.utils.lambda.Initialize;
import org.apache.iceberg.aws.glue.GlueCatalog;
import org.apache.iceberg.catalog.Namespace;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

public class Iceberg extends GlueCatalog {
    public static final String NAME = "glue_catalog";
    public static final String CATALOG_NAME = "catalog-name";
    public static final String CATALOG_IMPL = "catalog-impl";
    public static final String WAREHOUSE = "warehouse";
    public static final String IO_IMPL = "io-impl";
    public static final String GLUE_SKIP_ARCHIVE = "glue.skip-archive";
    public static final String DATABASE_NAME = "database-name";
    public static final String PROPERTIES = "iceberg.properties";
    public static final String ENV_PREFIX = "ICEBERG_";
    protected static final Logger logger = LogManager.getLogger(Iceberg.class);

    private final Map<String, String> properties;
    private final Namespace database;

    public Iceberg(Map<String, String> properties) {
        super();
        String databaseName = properties.remove(DATABASE_NAME);
        this.properties = properties;
        database = Namespace.of(databaseName);
    }

    public Iceberg initialize() {
        super.initialize(NAME, properties);
        return this;
    }

    public static Iceberg load() { return load(new Env()); }
    public static Iceberg load(Env env) {
        Properties properties = Initialize.properties(PROPERTIES);

        String catalogName = env.get(env.name(ENV_PREFIX+CATALOG_NAME));
        String catalogImpl = env.get(env.name(ENV_PREFIX+CATALOG_IMPL));
        String warehouse = env.get(env.name(ENV_PREFIX+WAREHOUSE));
        String ioImpl = env.get(env.name(ENV_PREFIX+IO_IMPL));
        String glueSkipArchive = env.get(env.name(ENV_PREFIX+GLUE_SKIP_ARCHIVE));
        String databaseName = env.get(env.name(ENV_PREFIX+DATABASE_NAME));

        Map<String, String> cfg = new HashMap<>() {{
            put(CATALOG_NAME, catalogName != null ? catalogName : properties.getProperty(CATALOG_NAME));
            put(CATALOG_IMPL, catalogImpl != null ? catalogImpl : properties.getProperty(CATALOG_IMPL));
            put(WAREHOUSE, warehouse != null ? warehouse : properties.getProperty(WAREHOUSE));
            put(IO_IMPL, ioImpl != null ? ioImpl : properties.getProperty(IO_IMPL));
            put(GLUE_SKIP_ARCHIVE, glueSkipArchive != null ? glueSkipArchive : properties.getProperty(GLUE_SKIP_ARCHIVE));
            put(DATABASE_NAME, databaseName != null ? databaseName : properties.getProperty(DATABASE_NAME));
        }};

        return new Iceberg(cfg);
    }


    @Override
    public void close() {
        try {
            super.close();
        } catch (IOException e) {
            logger.error(e);
        }
    }

    public Map<String, String> getProperties() {
        return properties;
    }

    public Namespace getDatabase() {
        return database;
    }

    public String getCatalogName() {
        return properties.get(CATALOG_NAME);
    }

    public String getCatalogImpl() {
        return properties.get(CATALOG_IMPL);
    }

    public String getWarehouse() {
        return properties.get(WAREHOUSE);
    }

    public String getIoImpl() {
        return properties.get(IO_IMPL);
    }

    public String getGlueSkipArchive() {
        return properties.get(GLUE_SKIP_ARCHIVE);
    }
}
