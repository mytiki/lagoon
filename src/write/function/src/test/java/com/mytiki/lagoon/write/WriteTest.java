/*
 * Copyright (c) TIKI Inc.
 * MIT license. See LICENSE file in root directory.
 */

package com.mytiki.lagoon.write;

import com.amazonaws.services.lambda.runtime.events.SQSBatchResponse;
import com.amazonaws.services.lambda.runtime.events.SQSEvent;
import com.amazonaws.services.lambda.runtime.tests.annotations.Event;
import com.mytiki.lagoon.write.mock.MockAthena;
import com.mytiki.lagoon.write.mock.MockIceberg;
import com.mytiki.lagoon.write.mock.MockStorage;
import com.mytiki.lagoon.write.utils.AthenaFacade;
import com.mytiki.lagoon.write.utils.IcebergFacade;
import com.mytiki.lagoon.write.utils.StorageFacade;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;

public class WriteTest {

    @ParameterizedTest
    @Event(value = "events/sqs-event.json", type = SQSEvent.class)
    public void HandleRequest_Batch(SQSEvent event) {
        StorageFacade storageMock = new MockStorage().storage();
        AthenaFacade athenaMock = new MockAthena().athena();
        try (IcebergFacade icebergMock = new MockIceberg().iceberg()){
            Handler handler = new Handler(icebergMock, storageMock, athenaMock);
            SQSBatchResponse response = handler.handleRequest(event, null);
            Assertions.assertEquals(1, response.getBatchItemFailures().size());
        }catch (Exception e){ throw new RuntimeException(e); }
    }
}
