package com.mytiki.lagoon.write.mock;

import com.mytiki.lagoon.write.fixture.FixtureParquet;
import com.mytiki.lagoon.write.utils.AthenaFacade;
import com.mytiki.lagoon.write.utils.StorageFacade;
import org.apache.iceberg.Schema;
import org.apache.parquet.hadoop.util.HadoopInputFile;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import java.io.IOException;

public class MockAthena {
    private final AthenaFacade athena;

    public MockAthena() {
        athena = Mockito.mock(AthenaFacade.class);
        Mockito.lenient().doReturn("dummy").when(athena).setEtlLoadedAt(Mockito.any());
        try(MockedStatic<AthenaFacade> dfltMock = Mockito.mockStatic(AthenaFacade.class)) {
            dfltMock.when(AthenaFacade::dflt).thenReturn(athena);
        }
    }

    public AthenaFacade athena() {
        return athena;
    }
}
