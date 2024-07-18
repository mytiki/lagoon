/*
 * Copyright (c) TIKI Inc.
 * MIT license. See LICENSE file in root directory.
 */

package com.mytiki.lagoon.write;

import com.amazonaws.services.lambda.runtime.events.SQSEvent;
import com.amazonaws.services.lambda.runtime.tests.annotations.Event;
import com.mytiki.lagoon.write.mock.MockCatalog;
import com.mytiki.lagoon.write.mock.MockStorage;
import org.junit.jupiter.params.ParameterizedTest;

public class WriteTest {

    @ParameterizedTest
    @Event(value = "events/sqs-event.json", type = SQSEvent.class)
    public void HandleRequest_Batch(SQSEvent event) {
        Storage storageMock = new MockStorage().storage();
        try (Catalog catalogMock = new MockCatalog().iceberg()) {
            Handler handler = new Handler(catalogMock, storageMock);
//            SQSBatchResponse response = handler.handleRequest(event, null);
//            Assertions.assertEquals(1, response.getBatchItemFailures().size());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
