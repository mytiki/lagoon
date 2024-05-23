/*
 * Copyright (c) TIKI Inc.
 * MIT license. See LICENSE file in root directory.
 */

package com.mytiki.lagoon.write;

import com.mytiki.utils.lambda.Initialize;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Properties;

public class InitializeTest {

    @Test
    public void Test_Logger_Output() {
        Logger logger = Initialize.logger(InitializeTest.class);
        logger.info("TESTING");
    }

    @Test
    public void Initialize_Properties_Success() {
        Properties properties = Initialize.properties("iceberg.properties");
        Assertions.assertEquals(properties.getProperty("catalog-name"), "iceberg");
    }

    @Test
    public void Initialize_Properties_Failure() {
        Assertions.assertThrows(RuntimeException.class, () -> Initialize.properties("DUMMY"));
    }
}
