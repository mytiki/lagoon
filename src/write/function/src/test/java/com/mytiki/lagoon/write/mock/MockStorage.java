package com.mytiki.lagoon.write.mock;

import com.mytiki.lagoon.write.fixture.FixtureParquet;
import com.mytiki.lagoon.write.utils.StorageFacade;
import org.apache.iceberg.Schema;
import org.apache.parquet.hadoop.util.HadoopInputFile;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import java.io.IOException;

import static org.mockito.Mockito.when;

public class MockStorage {
    private final StorageFacade storage;

    public MockStorage() {
        Schema schema = FixtureParquet.schema();
        try {
            HadoopInputFile inputFile = FixtureParquet.inputFile();
            storage = Mockito.mock(StorageFacade.class);
            Mockito.lenient().doNothing().when(storage).moveFile(Mockito.any(), Mockito.any(), Mockito.any());
            Mockito.lenient().doReturn(inputFile).when(storage).openFile(Mockito.any());
            Mockito.lenient().doReturn(schema).when(storage).readSchema(Mockito.any());
            try(MockedStatic<StorageFacade> dfltMock = Mockito.mockStatic(StorageFacade.class)) {
                dfltMock.when(StorageFacade::dflt).thenReturn(storage);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public StorageFacade storage() {
        return storage;
    }
}
