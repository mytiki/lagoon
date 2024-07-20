package com.mytiki.lagoon.write;

import com.mytiki.utils.lambda.Initialize;
import org.apache.logging.log4j.Logger;

public class Warn extends RuntimeException {
    protected static final Logger logger = Initialize.logger(Warn.class);

    public Warn(String id, String message) {
        super(message);
        logger.warn(String.format("Skip: %s - %s", id, message));
    }

    public Warn(String id, String message, Throwable cause) {
        super(message, cause);
        logger.warn(String.format("Skip: %s - %s", id, message), cause);
    }
}
