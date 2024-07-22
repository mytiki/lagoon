package com.mytiki.lagoon.write.mock;

import com.mytiki.lagoon.write.Storage;
import com.mytiki.lagoon.write.fixture.FixtureParquet;
import org.apache.iceberg.Schema;
import org.apache.parquet.hadoop.util.HadoopInputFile;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import java.io.IOException;

public class MockStorage {
    private final Storage storage;

    public MockStorage() {
        Schema schema = FixtureParquet.schema();
        try {
            HadoopInputFile inputFile = FixtureParquet.inputFile();
            storage = Mockito.mock(Storage.class);
            Mockito.lenient().doNothing().when(storage).move(Mockito.any(), Mockito.any(), Mockito.any());
            try (MockedStatic<Storage> dfltMock = Mockito.mockStatic(Storage.class)) {
                dfltMock.when(Storage::dflt).thenReturn(storage);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public Storage storage() {
        return storage;
    }
}
