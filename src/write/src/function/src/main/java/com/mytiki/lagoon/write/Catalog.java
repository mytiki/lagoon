/*
 * Copyright (c) TIKI Inc.
 * MIT license. See LICENSE file in root directory.
 */

package com.mytiki.lagoon.write;

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

public class Catalog extends GlueCatalog {
    protected static final Logger logger = LogManager.getLogger(Catalog.class);
    public static final String NAME = "glue_catalog";
    public static final String CATALOG_NAME = "catalog-name";
    public static final String CATALOG_IMPL = "catalog-impl";
    public static final String WAREHOUSE = "warehouse";
    public static final String IO_IMPL = "io-impl";
    public static final String GLUE_SKIP_ARCHIVE = "glue.skip-archive";
    public static final String PROPERTIES = "iceberg.properties";
    public static final String ENV_PREFIX = "ICEBERG_";
    public static final String ETL_LOADED_AT = "_etl_loaded_at";
    public static final Map<String, String> CREATE_PROPERTIES = new HashMap<>() {{
        put("table_type", "ICEBERG");
        put("format", "parquet");
        put("write_compression", "snappy");
        put("optimize_rewrite_delete_file_threshold", "2");
        put("optimize_rewrite_data_file_threshold", "10");
        put("vacuum_min_snapshots_to_keep", "1");
        put("vacuum_max_snapshot_age_seconds", "432000");
        put("vacuum_max_metadata_files_to_keep", "100");
    }};
    private final Map<String, String> properties;

    public Catalog(Map<String, String> properties) {
        super();
        logger.debug("iceberg load: {}", properties);
        this.properties = properties;
    }

    public Catalog initialize() {
        super.initialize(NAME, properties);
        logger.debug("Catalog: {}", NAME);
        return this;
    }

    public static Catalog load() {
        return load(new Env());
    }

    public static Catalog load(Env env) {
        Properties properties = Initialize.properties(PROPERTIES);

        String catalogName = env.get(env.name(ENV_PREFIX + CATALOG_NAME));
        String catalogImpl = env.get(env.name(ENV_PREFIX + CATALOG_IMPL));
        String warehouse = env.get(env.name(ENV_PREFIX + WAREHOUSE));
        String ioImpl = env.get(env.name(ENV_PREFIX + IO_IMPL));
        String glueSkipArchive = env.get(env.name(ENV_PREFIX + GLUE_SKIP_ARCHIVE));

        Map<String, String> cfg = new HashMap<>() {{
            put(CATALOG_NAME, catalogName != null ? catalogName : properties.getProperty(CATALOG_NAME));
            put(CATALOG_IMPL, catalogImpl != null ? catalogImpl : properties.getProperty(CATALOG_IMPL));
            put(WAREHOUSE, warehouse != null ? warehouse : properties.getProperty(WAREHOUSE));
            put(IO_IMPL, ioImpl != null ? ioImpl : properties.getProperty(IO_IMPL));
            put(GLUE_SKIP_ARCHIVE, glueSkipArchive != null ? glueSkipArchive : properties.getProperty(GLUE_SKIP_ARCHIVE));
        }};

        return new Catalog(cfg);
    }

    public Namespace getDatabase(String name) {
        return Namespace.of(name);
    }

    public Namespace getDatabaseSafe(String name) {
        Namespace database = getDatabase(name);
        if (!this.namespaceExists(database)) {
            logger.debug("create database: {}", database);
            this.createNamespace(database);
        }
        return database;
    }

    @Override
    public void close() {
        try {
            logger.debug("closing Iceberg connection");
            super.close();
        } catch (IOException e) {
            logger.error(e);
        }
    }

    public Map<String, String> getProperties() {
        return properties;
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
