/*
 * Copyright (c) TIKI Inc.
 * MIT license. See LICENSE file in root directory.
 */

package com.mytiki.lagoon.write.fixture;

import com.amazonaws.services.lambda.runtime.ClientContext;
import com.amazonaws.services.lambda.runtime.CognitoIdentity;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.LambdaLogger;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class TestContext implements Context {

    @Override
    public String getAwsRequestId() {
        return "dummy";
    }

    @Override
    public String getLogGroupName() {
        return "dummy";
    }

    @Override
    public String getLogStreamName() {
        return "dummy";
    }

    @Override
    public String getFunctionName() {
        return "dummy";
    }

    @Override
    public String getFunctionVersion() {
        return "dummy";
    }

    @Override
    public String getInvokedFunctionArn() {
        return "dummy";
    }

    @Override
    public CognitoIdentity getIdentity() {
        return null;
    }

    @Override
    public ClientContext getClientContext() {
        return null;
    }

    @Override
    public int getRemainingTimeInMillis() {
        return 0;
    }

    @Override
    public int getMemoryLimitInMB() {
        return 0;
    }

    @Override
    public LambdaLogger getLogger() {
        return new LambdaLogger() {
            private static final Logger logger = LogManager.getLogger(LambdaLogger.class);

            @Override
            public void log(String s) {
                logger.info(s);
            }

            @Override
            public void log(byte[] bytes) {
                logger.info(bytes);
            }
        };
    }
}

